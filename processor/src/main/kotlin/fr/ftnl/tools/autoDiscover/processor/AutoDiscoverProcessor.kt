/*
 * auto-discover - auto-discover.processor.main
 * Copyright (C) 2025 ocelus_ftnl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.ftnl.tools.autoDiscover.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

/**
 *
 */
class AutoDiscoverProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /**
     * Maintains a mapping between service interface names and their respective implementing classes.
     *
     * Each key in the map represents the fully qualified name of an interface, while the associated value is a mutable set
     * containing the fully qualified names of classes that implement the interface.
     *
     * This variable is used during the annotation processing to collect and store information about the relationships
     * between interfaces and their implementers, subsequently facilitating the generation of service files.
     */
    private val services = mutableMapOf<String, MutableSet<String>>()

    /**
     * Processes symbols annotated with a specific annotation and organizes them by the interfaces they implement.
     *
     * @param resolver The resolver used to find symbols annotated with `@AutoDiscover`.
     * @return A list of annotated symbols. Currently, this implementation always returns an empty list.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotationName = "fr.ftnl.tools.autoDiscover.api.AutoDiscover"

        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }

        symbols.forEach { classDeclaration ->
            val serviceImplementer = classDeclaration.qualifiedName?.asString()
            if (serviceImplementer == null) {
                logger.error("Impossible d'obtenir le nom qualifié de la classe annotée.", classDeclaration)
                return@forEach
            }

            if (classDeclaration.classKind != ClassKind.CLASS) {
                logger.warn("L'annotation @AutoDiscover sur '$serviceImplementer' sera ignorée car ce n'est pas une classe concrète.", classDeclaration)
                return@forEach
            }

            val allInterfaces = getAllSuperInterfaces(classDeclaration)
            if (allInterfaces.isEmpty()) {
                logger.warn("La classe $serviceImplementer est annotée avec @AutoDiscover mais n'implémente aucune interface.", classDeclaration)
                return@forEach
            }

            allInterfaces.forEach { serviceInterface ->
                val serviceName = serviceInterface.qualifiedName!!.asString()
                services.getOrPut(serviceName) { mutableSetOf() }.add(serviceImplementer)
            }
        }

        return emptyList()
    }

    /**
     * Finalizes the processing by generating service descriptor files for the collected services.
     *
     * This method creates a descriptor file for each discovered service (interface) and writes the corresponding
     * implementations to the file. The files are generated in the "META-INF.services" directory. If an error occurs
     * during the file generation process, an error message will be logged.
     *
     * Logging:
     * - Logs a message when the service generation process starts and ends.
     * - Logs details for each successfully created service file, indicating the service name and the number of implementations.
     * - Logs errors if file generation fails or encounters exceptions.
     *
     * Behavior:
     * - If no services were collected, the method exits early without performing any operation.
     * - For each service, writes the fully qualified names of its implementations, separating them by line.
     */
    override fun finish() {
        if (services.isEmpty()) return

        logger.info("AutoDiscover : Génération des fichiers de service...")

        services.forEach { (serviceName, implementers) ->
            try {
                val file: OutputStream = codeGenerator.createNewFile(
                    dependencies = Dependencies(true),
                    packageName = "META-INF.services",
                    fileName = serviceName,
                    extensionName = ""
                )

                file.write(implementers.joinToString("\n").toByteArray())
                file.close()
                logger.info("  - Fichier pour '$serviceName' créé avec ${implementers.size} implémentation(s).")

            } catch (e: Exception) {
                logger.error("AutoDiscover: Erreur lors de la génération du fichier de service pour '$serviceName'. Exception: ${e.message}")
            }
        }
        logger.info("AutoDiscover : Génération terminée.")
    }

    /**
     * Collects and returns all superinterfaces that the given class declaration directly or indirectly implements.
     * Traverses the hierarchy of the provided class declaration to identify all implemented interfaces.
     *
     * @param classDeclaration The class declaration whose superinterfaces are to be retrieved.
     * @return A set of class declarations representing all identified superinterfaces of the provided class.
     */
    private fun getAllSuperInterfaces(classDeclaration: KSClassDeclaration): Set<KSClassDeclaration> {
        val interfaces = mutableSetOf<KSClassDeclaration>()
        val toProcess = mutableListOf(classDeclaration)
        val processed = mutableSetOf<KSClassDeclaration>()

        while (toProcess.isNotEmpty()) {
            val currentClass = toProcess.removeAt(0)
            if (currentClass in processed) continue
            processed.add(currentClass)

            currentClass.superTypes.forEach { superTypeRef ->
                val superType = superTypeRef.resolve().declaration
                if (superType is KSClassDeclaration && superType.classKind == ClassKind.INTERFACE) {
                    interfaces.add(superType)
                    toProcess.add(superType)
                } else if (superType is KSClassDeclaration && superType.classKind == ClassKind.CLASS) {
                    toProcess.add(superType)
                }
            }
        }
        return interfaces
    }
}


/**
 * Provides an implementation of SymbolProcessorProvider for enabling the automatic discovery
 * of service classes annotated with a specified annotation.
 *
 * This provider is responsible for creating instances of the SymbolProcessor, which processes
 * Kotlin symbols during annotation processing in a project.
 */
class AutoDiscoverProcessorProvider : SymbolProcessorProvider {
    /**
     * Creates and returns a new instance of the `AutoDiscoverProcessor` configured with the given environment.
     *
     * @param environment The `SymbolProcessorEnvironment` that provides the resources needed to configure the processor
     * such as the code generator and logger.
     * @return A new instance of `SymbolProcessor` configured with the specified `SymbolProcessorEnvironment`.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoDiscoverProcessor(environment.codeGenerator, environment.logger)
    }
}
