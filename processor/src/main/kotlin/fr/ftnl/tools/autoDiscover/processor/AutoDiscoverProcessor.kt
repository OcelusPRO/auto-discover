package fr.ftnl.tools.autoDiscover.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class AutoDiscoverProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

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

class AutoDiscoverProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoDiscoverProcessor(environment.codeGenerator, environment.logger)
    }
}
