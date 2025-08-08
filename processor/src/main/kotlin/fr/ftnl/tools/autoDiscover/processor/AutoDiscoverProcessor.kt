package fr.ftnl.tools.autoDiscover.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

/**
 * A processor that handles the `@AutoDiscover` annotation and generates service files for
 * annotated classes implementing interfaces. This class is part of a symbol processing tool
 * using the Kotlin Symbol Processing (KSP) API.
 *
 * The processor identifies and validates classes annotated with `@AutoDiscover`, retrieves
 * their implemented interfaces, and generates service descriptor files in the `META-INF.services`
 * directory. These files map each interface to its corresponding implementing class, which is
 * necessary for runtime service discovery.
 *
 * @constructor Creates an instance of `AutoDiscoverProcessor` with the provided code generator
 * and logger for code generation and diagnostics reporting, respectively.
 *
 * @param codeGenerator A utility for generating files during the compilation process.
 * @param logger A logging utility for reporting errors, warnings, and informational messages.
 */
class AutoDiscoverProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /**
     * Processes symbols in the provided resolver annotated with `@AutoDiscover`.
     * Filters valid class declarations, generates service files for those classes,
     * and returns an empty list.
     *
     * @param resolver the Resolver instance used to obtain and process symbols annotated with `@AutoDiscover`.
     * @return a list of KSAnnotated symbols processed by this method, which is always an empty list.
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
            generateServiceFiles(classDeclaration)
        }

        return emptyList()
    }

    /**
     * Generates service provider configuration files for the provided class declaration
     * if the class implements one or more interfaces.
     *
     * The service files are created under the "META-INF.services" directory
     * corresponding to each interface the class implements.
     *
     * Outputs a warning if the class does not implement any interfaces,
     * and logs errors or warnings for various conditions during file generation.
     *
     * @param classDeclaration represents the annotated class for which service files need to be generated.
     */
    private fun generateServiceFiles(classDeclaration: KSClassDeclaration) {
        val serviceImplementer = classDeclaration.qualifiedName?.asString()
        if (serviceImplementer == null) {
            logger.error("Impossible d'obtenir le nom qualifié de la classe annotée.", classDeclaration)
            return
        }

        val allInterfaces = getAllSuperInterfaces(classDeclaration)

        if (allInterfaces.isEmpty()) {
            logger.warn("La classe $serviceImplementer est annotée avec @AutoDiscover mais n'implémente aucune interface.", classDeclaration)
            return
        }

        allInterfaces.forEach { serviceInterface ->
            val serviceName = serviceInterface.qualifiedName!!.asString()
            logger.info("Génération du fichier de service pour $serviceName avec l'implémentation $serviceImplementer")

            try {
                val file = codeGenerator.createNewFile(
                    dependencies = Dependencies(true, classDeclaration.containingFile!!),
                    packageName = "META-INF.services",
                    fileName = serviceName,
                    extensionName = ""
                )
                file.write(serviceImplementer.toByteArray())
                file.close()
            } catch (e: FileAlreadyExistsException) {
                logger.warn("Le fichier de service pour $serviceName existe déjà. Il peut y avoir plusieurs implémentations.")
            }
        }
    }

    /**
     * Retrieves all superinterfaces implemented by the given class declaration, including indirect ones.
     *
     * @param classDeclaration the class declaration whose superinterfaces need to be collected.
     * @return a set of class declarations representing all the interfaces implemented by the given class declaration.
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
                val superType = superTypeRef.resolve().declaration as KSClassDeclaration
                if (superType.classKind == ClassKind.INTERFACE) {
                    interfaces.add(superType)
                }
                toProcess.add(superType)
            }
        }
        return interfaces
    }
}

/**
 * A provider for the `AutoDiscoverProcessor` symbol processor. It is responsible for creating
 * instances of the processor when requested by the Kotlin Symbol Processing (KSP) environment.
 *
 * The `AutoDiscoverProcessorProvider` facilitates the integration of the custom symbol
 * processing logic defined in `AutoDiscoverProcessor` into the compilation process by following
 * the contract of the `SymbolProcessorProvider` interface.
 *
 * The processor generated by this provider processes symbols that are annotated with the
 * `@AutoDiscover` annotation. It validates and processes these symbols to generate
 * service files in the `META-INF.services` directory.
 */
class AutoDiscoverProcessorProvider : SymbolProcessorProvider {
    /**
     * Creates a new instance of the `AutoDiscoverProcessor` using the provided environment.
     *
     * The environment provides access to necessary tools such as a code generator and logger,
     * which are utilized by the `AutoDiscoverProcessor`.
     *
     * @param environment The processing environment which provides utilities like code generation, logging,
     *                     and access to symbols for annotation processing.
     * @return An instance of `AutoDiscoverProcessor` initialized with the provided environment.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoDiscoverProcessor(environment.codeGenerator, environment.logger)
    }
}
