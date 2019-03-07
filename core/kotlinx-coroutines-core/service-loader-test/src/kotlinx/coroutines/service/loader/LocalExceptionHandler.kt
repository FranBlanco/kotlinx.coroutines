package kotlinx.coroutines.service.loader

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class LocalExceptionHandler : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler
{
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        println(exception.message)
    }
}