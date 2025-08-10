/*
 * auto-discover - auto-discover.api.main
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

package fr.ftnl.tools.autoDiscover.api

import java.util.ServiceLoader

/**
 * Annotation to mark a class for automatic discovery of implementations at runtime.
 *
 * Classes annotated with this annotation are expected to be processed by an annotation processor
 * to register their implementations, enabling runtime discovery via the `ServiceLoader` mechanism.
 *
 * Proper configuration of the annotation processor is required to ensure discovery works correctly.
 * This typically involves adding the appropriate dependency in the project's build configuration.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoDiscover {

    /**
     * Provides a companion object to supplement the `AutoDiscover` annotation with a utility
     * method to initialize and ensure that service implementations are discoverable at runtime.
     * The initialization validates that the underlying service discovery mechanism is properly
     * configured and throws descriptive exceptions if misconfiguration is detected.
     */
    companion object {
        /**
         * Initializes the auto-discovery process for the specified service class using the ServiceLoader mechanism.
         *
         * Throws an exception if no implementation of the provided service class is found.
         * This exception typically indicates a configuration issue where the required annotation processor
         * has not been added to the build setup.
         *
         * @param S The type of the service to initialize.
         * @param serviceClass The class object corresponding to the service type [S].
         *                      This is used to locate and load all available implementations of the service.
         * @throws IllegalStateException If no implementations of the specified service are found,
         *                                typically caused by a missing annotation processor.
         */
        fun <S : Any> initialize(serviceClass: Class<S>) {
            val services = ServiceLoader.load(serviceClass).iterator()
            if (!services.hasNext()) {
                throw IllegalStateException(
                    """
                    
                    *************************************************************************************
                    ERREUR DE CONFIGURATION DE AUTO-DISCOVER :
                    
                    Aucune implémentation de '${serviceClass.simpleName}' n'a été trouvée.
                    Cela signifie probablement que le processeur d'annotations (processor) n'a pas été
                    ajouté à votre configuration de build.
                    
                    Veuillez vous assurer que la dépendance 'processor' est bien présente
                    avec la configuration 'ksp' dans votre fichier build.gradle.kts :
                    
                    dependencies {
                        implementation("fr.ftnl.tools:api:1.0.0") // ou via le BOM
                        ksp("fr.ftnl.tools:processor:1.0.0")   // <-- CETTE LIGNE EST ESSENTIELLE !
                    }
                    
                    Pour plus d'aide, consultez la documentation du projet.
                    *************************************************************************************
                    
                    """.trimIndent()
                )
            }
        }
    }
}