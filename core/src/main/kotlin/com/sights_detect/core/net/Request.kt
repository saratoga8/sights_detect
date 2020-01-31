package com.sights_detect.core.net

import com.sights_detect.core.seekers.objects.google.GoogleResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import java.util.*

class Request(private val properties: Properties) {
	private val client: HttpClient

	init { client = getClient() }

	private fun getClient(): HttpClient {
		return HttpClient(Apache) {
			install(JsonFeature) {
				serializer = GsonSerializer {
					serializeNulls()
					disableHtmlEscaping()
				}
			}
//			install(Logging) {
//				logger = Logger.DEFAULT
//				level = LogLevel.INFO
//			}
		}
	}

	suspend fun post(bodyObj: Any): GoogleResponse {
		return this.client.post<GoogleResponse> {
			buildURL()
			contentType(ContentType.Application.Json)
			body = bodyObj
		}.also { client.close()	}
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