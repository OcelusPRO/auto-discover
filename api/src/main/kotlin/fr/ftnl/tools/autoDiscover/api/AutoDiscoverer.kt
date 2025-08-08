package fr.ftnl.tools.autoDiscover.api

import java.util.ServiceLoader
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Provides utility methods for discovering implementations of services at runtime using the ServiceLoader mechanism.
 * The discovery process requires proper configuration of dependencies for annotation processing,
 * ensuring all service implementations are correctly registered.
 */
object AutoDiscoverer {

    /**
     * AtomicBoolean used to track whether the initialization of the service discovery process
     * has been triggered. Ensures that repeated attempts at initialization are prevented once
     * the process has started.
     */
    @PublishedApi
    internal val initialized = AtomicBoolean(false)


    /**
     * Discovers and retrieves all instances of the specified service type using the `ServiceLoader` mechanism.
     *
     * This function requires that the target service type is properly registered and discoverable.
     * It ensures that the auto-discovery mechanism is initialized and throws a configuration error
     * if no implementations of the requested service type are found.
     *
     * @return A list of discovered service implementations of the specified type [S], or an exception if none are found.
     * @throws IllegalStateException If no implementations are registered or if there is a configuration issue.
     */
    inline fun <reified S : Any> get(): List<S> {
        val serviceClass = S::class.java
        val services = ServiceLoader.load(serviceClass).toList()
        if (services.isEmpty() && !initialized.getAndSet(true))
            ServiceLoader.load(Any::class.java).firstOrNull() ?: throw createConfigurationError(serviceClass)
        initialized.set(true)
        return services
    }

    /**
     * Creates and returns an `IllegalStateException` indicating a configuration error related to the
     * absence of an implementation for the specified service class.
     *
     * @param S The type of the service for which the configuration error is being generated.
     * @param serviceClass The `Class` object of the service type for which no implementation
     * was found. This is used within the error message to identify the service type.
     * @return An `IllegalStateException` describing the configuration error and providing guidance
     * on resolving the issue.
     */
// La fonction d'erreur ne change pas
    @PublishedApi
    internal  fun <S : Any> createConfigurationError(serviceClass: Class<S>): IllegalStateException {
        return IllegalStateException(
            """
            
            *************************************************************************************
            ERREUR DE CONFIGURATION DE AUTO-DISCOVER :
            
            Aucune implémentation de '${serviceClass.simpleName}' n'a été trouvée.
            La dépendance 'processor' est probablement manquante dans votre build.
            
            Assurez-vous d'avoir bien ajouté la ligne suivante à votre build.gradle.kts :
            
            dependencies {
                ksp("fr.ftnl.tools:processor:1.0.0")   // <-- CETTE LIGNE EST ESSENTIELLE !
            }
            *************************************************************************************
            
            """.trimIndent()
        )
    }
}