package kotlinx.coroutines.internal

import kotlinx.coroutines.InternalCoroutinesApi
import java.io.*
import java.net.URL
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.io.BufferedReader
import java.lang.Exception

/**
 * Simplified version of [ServiceLoader]
 *
 * FastServiceLoader locates and instantiates all service providers named in configuration files placed in the resource directory <tt>META-INF/services</tt>.
 * In order to speed up reading JAR resources it omits signed JAR verification.
 * In case of [ServiceConfigurationError] thrown service loading falls back to the standard [ServiceLoader].
 */
private const val PREFIX: String = "META-INF/services/"

/**
 * Name of the boolean property that enables using of [FastServiceLoader].
 */
internal const val FAST_SERVICE_LOADER_PROPERTY_NAME = "kotlinx.coroutines.verify.service.loader"

@JvmField
internal val FAST_SERVICE_LOADER_ENABLED = systemProp(FAST_SERVICE_LOADER_PROPERTY_NAME, false)

@InternalCoroutinesApi
public object FastServiceLoader {

    internal fun <S> load(service: Class<S>, loader: ClassLoader): List<S> {
        return if (FAST_SERVICE_LOADER_ENABLED) {
            try {
                loadProviders(service, loader)
            } catch (e: Throwable) {
                ServiceLoader.load(service, loader).toList()
            }
        } else {
            ServiceLoader.load(service, loader).toList()
        }
    }

    @InternalCoroutinesApi
    public fun <S> loadProviders(service: Class<S>, loader: ClassLoader): List<S> {
        val fullServiceName = PREFIX + service.name
        val urls = loader.getResources(fullServiceName).toList()
        val providers = mutableListOf<S>()
        urls.forEach {
            val providerNames = parse(it)
            providers.addAll(providerNames.map { getProviderInstance(it, loader, service) })
        }
        return providers
    }

    private fun <S> getProviderInstance(name: String, loader: ClassLoader, service: Class<S>): S {
        val cl = Class.forName(name, false, loader)
        require(service.isAssignableFrom(cl))
        return service.cast(cl.getDeclaredConstructor().newInstance())
    }

    private fun parse(url: URL): List<String> {
        val string = url.toString()
        when {
            string.startsWith("file") -> {
                val pathToFile = string.substringAfter("file:")
                BufferedReader(FileReader(pathToFile)).use { r ->
                    return parseFile(r)
                }
            }
            string.startsWith("jar") -> {
                val pathToJar = string.substringAfter("jar:file:").substringBefore('!')
                val entry = string.substringAfter("!/")
                (JarFile(pathToJar, false) as Closeable).use { file ->
                    BufferedReader(InputStreamReader((file as JarFile).getInputStream(ZipEntry(entry)),"UTF-8")).use { r ->
                        return parseFile(r)
                    }
                }
            }
            else -> throw Exception("Error parsing configuration file URL $url")
        }
    }

    private fun parseFile(r: BufferedReader): List<String> {
        val names = mutableSetOf<String>()
        while (true) {
            val line = r.readLine() ?: break
            val serviceName = line.substringBefore("#").trim()
            require(serviceName.all { it == '.' || Character.isJavaIdentifierPart(it) })
            if (serviceName.isNotEmpty()) {
                names.add(serviceName)
            }
        }
        return names.toList()
    }
}