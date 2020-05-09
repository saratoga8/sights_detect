package com.sights_detect.core.net

import com.sights_detect.core.seekers.objects.google.GoogleResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
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
	private val usedKeys = listOf("host", "path", "key")

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
//			install(Logging) {
//				logger = Logger.DEFAULT
//				level = LogLevel.INFO
//			}
		}
	}

	fun stop() = client.cancel()

	suspend fun post(bodyObj: Any): GoogleResponse {
		return this.client.post<GoogleResponse> {
			buildURL()
			contentType(ContentType.Application.Json)
			body = bodyObj
			header("Content-Encoding", "gzip")
		}.also { client.close()	}
	}

	private fun checkProperties(properties: Properties, keys: List<String>) {
		keys.forEach { key -> require(!properties.getProperty(key).isNullOrEmpty()) { "There is no '$key' key in properties" } }
	}

	 private fun HttpRequestBuilder.buildURL() {
		 url {
			 host = properties.getProperty("host")
			 path(properties.getProperty("path"))
			 parameters.append("key", properties.getProperty("key"))
			 protocol = URLProtocol.HTTPS
		 }
	}
}