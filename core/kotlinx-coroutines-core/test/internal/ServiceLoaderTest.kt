package kotlinx.coroutines.internal

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceLoaderTest {
    @Test
    fun testLoadingSameModuleService() {
        val providers = MainDispatcherFactory::class.java.let { FastServiceLoader.loadProviders(it, it.classLoader) }
        assertEquals(providers.size, 2)
        val providers1 = CoroutineExceptionHandler::class.java.let { FastServiceLoader.loadProviders(it, it.classLoader) }
        assertEquals(providers1.size, 1)
    }

    @Test
    fun testCrossModuleService() {
        val providers = CoroutineScope::class.java.let { FastServiceLoader.loadProviders(it, it.classLoader) }
        assertEquals(providers.size, 3)
    }
}