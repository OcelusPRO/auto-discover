import fr.ftnl.tools.autoDiscover.api.AutoDiscoverer
import org.junit.jupiter.api.assertThrows
import java.util.ServiceConfigurationError
import kotlin.test.*


/**
 * Test suite for verifying the functionality of the AutoDiscover integration within the system.
 *
 * This class contains unit tests that validate the discovery of services using the AutoDiscoverer API,
 * as well as the correct generation of service files by the KSP annotation processing mechanism.
 */
class TestAutoDiscoverIntegration {

    /**
     * Test to ensure that a service implementing the specified interface can be discovered
     * using the AutoDiscoverer API.
     *
     * This test verifies:
     * - The list of discovered services is not null.
     * - Exactly one implementation is discovered.
     * - The discovered service matches the expected implementation type.
     *
     * This ensures that the AutoDiscoverer API is correctly registering and discovering
     * implementations annotated with `@AutoDiscover`.
     *
     * Assertions:
     * - The services list is not null.
     * - The size of the services list is exactly 1.
     * - The discovered service matches the type of the expected implementation.
     *
     * This ensures conformance with the expected behavior of the AutoDiscoverer API and validates
     * runtime service discovery via the `ServiceLoader` mechanism.
     */
    @Test
    fun `service should be discoverable via the AutoDiscoverer API`() {
        val services = AutoDiscoverer.get<AnImplementedInterface>()

        assertNotNull(services, "La liste des services ne devrait pas être nulle.")
        assertEquals(1, services.size, "Il devrait y avoir un seul service découvert.")
        assertTrue(services.first() is IImplementAnInterface, "Le service découvert doit être de type MyServiceImpl.")
    }

    /**
     * Validates that the service file corresponding to a specific interface is correctly generated
     * when using Kotlin Symbol Processing (KSP).
     *
     * This test ensures the following:
     * - The service file's path is constructed correctly based on the qualified name of the interface.
     * - The service file is discoverable as a resource in the classpath.
     * - The content of the service file matches the fully qualified name of the expected implementation class.
     *
     * Fails the test if:
     * - The service file is not found in the expected resource path.
     * - The content of the service file is incorrect or does not match the expected implementer name.
     *
     * Exceptions:
     * Throws an assertion error if the service file is missing or its content is invalid.
     */
    @Test
    fun `service file should be generated correctly by KSP`() {
        AutoDiscoverer.get<AnImplementedInterface>()

        val serviceInterfaceName = AnImplementedInterface::class.qualifiedName
        val serviceResourcePath = "META-INF/services/$serviceInterfaceName"

        val resourceStream = this::class.java.classLoader.getResourceAsStream(serviceResourcePath)

        assertNotNull(resourceStream, "Le fichier de service '$serviceResourcePath' n'a pas été trouvé dans les ressources.")

        val content = resourceStream.bufferedReader().readText().trim()
        val expectedImplementerName = IImplementAnInterface::class.qualifiedName

        assertEquals(expectedImplementerName, content, "Le contenu du fichier de service est incorrect.")
    }


    /**
     * Tests that annotating an interface does not lead to the generation of a service file.
     *
     * This ensures that the `AutoDiscover` annotation is ignored when applied to interfaces,
     * as only concrete classes should trigger service file generation.
     *
     * The test verifies that no service file is created in the path `META-INF/services/{interfaceName}`
     * for an annotated interface. This behavior helps avoid runtime discovery for interfaces that are
     * not intended to be directly instantiated or used as services.
     *
     * Assertions:
     * - Confirms that accessing the path for the service file of the annotated interface returns null,
     *   indicating the absence of any generated file.
     */
    @Test
    fun `annotating an interface should not generate a service file`(){
        AutoDiscoverer.get<PleaseDiscoverMe>()

        val serviceInterfaceName = PleaseDiscoverMe::class.qualifiedName
        val serviceResourcePath = "META-INF/services/$serviceInterfaceName"

        val resourceStream = this::class.java.classLoader.getResourceAsStream(serviceResourcePath)

        assertNull(resourceStream, "Aucun fichier de service ne devrait être généré pour une interface annotée.")
    }


    /**
     * Tests that attempting to annotate an abstract class with `@AutoDiscover` results in a
     * `ServiceConfigurationError` being thrown.
     *
     * Abstract classes are not valid candidates for registration with the `AutoDiscoverer`
     * API because they cannot be instantiated directly. This test ensures that the discovery
     * mechanism enforces this restriction and provides meaningful feedback when misconfigured
     * annotations are used.
     *
     * Throws:
     * - `ServiceConfigurationError` if the annotated abstract class is incorrectly processed.
     */
    @Test
    fun `annotating an abstract class should throw an exception`() {
        assertThrows<ServiceConfigurationError> { AutoDiscoverer.get<AnotherCoolInterface>() }
    }


    /**
     * Validates that services implementing a parent interface can be discovered along with services implementing
     * its child interface through the AutoDiscoverer API.
     *
     * This test ensures the functionality of the service discovery mechanism, verifying that:
     * - All implementations of `IAmAChildInterface` are returned when querying for child interface services.
     * - All implementations of `IAmAParentInterface`, including those implementing `IAmAChildInterface`,
     *   are returned when querying for parent interface services.
     * - The correct number of discovered services is as expected for both parent and child interfaces.
     * - At least one service implementing the child interface is present in the discovered parent services.
     *
     * The test employs assertions to confirm:
     * - The non-nullity of the discovered service lists.
     * - The count of services returned for both parent and child interfaces.
     * - That a child interface service is included in the discovered parent interface services.
     */
    @Test
    fun `service should be discoverable through parent interface`() {
        val childServices = AutoDiscoverer.get<IAmAChildInterface>()
        assertNotNull(childServices)
        assertEquals(1, childServices.size)

        val parentServices = AutoDiscoverer.get<IAmAParentInterface>()
        assertNotNull(parentServices)
        assertEquals(2, parentServices.size)
        assertTrue(parentServices.any { it is IAmAChildInterface} )
    }


    /**
     * Tests the discovery of all implementations of a specified interface using the AutoDiscoverer API.
     *
     * Validates that the auto-discovery mechanism correctly identifies and retrieves all classes
     * implementing the `IAmASuperPopularInterface`. The method performs the following checks:
     * - Ensures that the discovered implementations are not null.
     * - Verifies the total count of discovered implementations matches the expected number.
     * - Checks that the discovered implementations include the expected concrete classes.
     *
     * This test assumes that the `IAmASuperPopularInterface` interface has three annotated implementations:
     * `IAmASuperPopularClass`, `IAmASuperPopularClass2`, and `IAmASuperPopularClass3`.
     *
     * Assertions in the test serve to confirm that the discovery mechanism is functioning as expected,
     * ensuring the auto-discovery process works under the assumed configuration.
     */
    @Test
    fun `should discover all implementations of an interface`() {
        val multiServices = AutoDiscoverer.get<IAmASuperPopularInterface>()
        assertNotNull(multiServices)

        assertEquals(3, multiServices.size, "Doit découvrir les trois implémentations.")

        val serviceTypes = multiServices.map { it::class }
        assertTrue(serviceTypes.contains(IAmASuperPopularClass::class))
        assertTrue(serviceTypes.contains(IAmASuperPopularClass2::class))
        assertTrue(serviceTypes.contains(IAmASuperPopularClass3::class))
    }
}