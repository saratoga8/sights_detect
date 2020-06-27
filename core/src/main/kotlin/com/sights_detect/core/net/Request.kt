package com.sights_detect.core.net

import com.sights_detect.core.seekers.objects.google.GoogleResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.BrowserUserAgent
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.UserAgent
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import kotlinx.coroutines.cancel
import org.apache.logging.log4j.kotlin.Logging
import java.util.*

internal open class Request(private val properties: Properties, requestTimeout: Long = 120000L, connectTimeout: Long = 30000L, socketTimeout: Long = 120000L): Logging {
	private val client: HttpClient
	private val usedKeys = listOf("host", "path", "key", "compression")

	init {
		checkProperties(properties, usedKeys)
		client = getClient(requestTimeout, connectTimeout, socketTimeout)
	}

	protected open fun getClient(requestTimeout: Long, connectTimeout: Long, socketTimeout: Long): HttpClient { // open only for tests
		return HttpClient(Apache) {
			install(JsonFeature) {
				serializer = GsonSerializer {
					serializeNulls()
					disableHtmlEscaping()
				}
			}
			install(HttpTimeout) {
				requestTimeoutMillis = requestTimeout
				connectTimeoutMillis = connectTimeout
				socketTimeoutMillis = socketTimeout
			}
			install(io.ktor.client.features.logging.Logging) {
				logger = Logger.DEFAULT
				level = LogLevel.ALL
			}
			install(UserAgent) {
				BrowserUserAgent()
			}
		}
	}

	fun stop() = client.cancel()

	suspend fun post(bodyObj: Any): GoogleResponse {
		return this.client.post<GoogleResponse> {
			buildURL()
			contentType(ContentType.Application.Json)
			body = bodyObj
			if(properties.getProperty("compression") == "yes") header("Content-Encoding", "gzip")
		}.also { client.close()	}
	}

	private fun checkProperties(properties: Properties, keys: List<String>) {
		keys.forEach { key -> require(!properties.getProperty(key).isNullOrEmpty()) { "There is no '$key' key in properties" } }
	}

	 private fun HttpRequestBuilder.buildURL() {
		 url {
			 host = getHost(properties)
			 path(properties.getProperty("path"))
			 parameters.append("key", properties.getProperty("key"))
			 if (properties.getProperty("host").startsWith("https://"))
				 protocol = URLProtocol.HTTPS
			 if(getPort(properties).isNotEmpty())
				 port = getPort(properties).toInt()
		 }
	}

	private fun getHost(properties: Properties): String {
		val host = properties.getProperty("host").removePrefix("https://").removePrefix("http://")
		return if(host.contains(':'))
			host.split(":")[0]
		else
			host
	}

	private fun getPort(properties: Properties): String {
		val host = properties.getProperty("host").removePrefix("https://").removePrefix("http://")
		var port = ""
		if(host.contains(':')) { port = host.split(":")[1] }
		return port
	}
}