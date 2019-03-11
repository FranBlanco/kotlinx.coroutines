import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.FastServiceLoader
import kotlinx.coroutines.internal.MainDispatcherFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceLoaderTest {

    @InternalCoroutinesApi
    @Test
    fun testLoadingCrossModuleService() {
        val providers = MainDispatcherFactory::class.java.let { FastServiceLoader.loadProviders(it, it.classLoader) }
        assertEquals(providers.size, 2)
    }

    @InternalCoroutinesApi
    @Test
    fun testLoadingSameModuleService() {
        val providers = CoroutineExceptionHandler::class.java.let { FastServiceLoader.loadProviders(it, it.classLoader) }
        assertEquals(providers.size, 2)
    }

    @InternalCoroutinesApi
    @Test
    fun testMultipleServicesPerFile() {
        val providers = CoroutineScope::class.java.let { FastServiceLoader.loadProviders(it, it.classLoader) }
        assertEquals(providers.size, 2)
    }
}