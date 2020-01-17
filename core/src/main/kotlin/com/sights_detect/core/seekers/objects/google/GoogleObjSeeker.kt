package com.sights_detect.core.seekers.objects.google

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.seekers.objects.ObjectSeeker
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import java.io.File


class GoogleObjSeeker(private val path: String): ObjectSeeker, Logging {
	override fun find(): List<Detection> {
		if (File(path).exists()) {
			val client = getClient()
			val response = runBlocking { doRequest(client) }
			client.close()
			return listOf(createDetections(response))
		} else logger.warn( "File of picture $path DOESN'T exist")
		return listOf()
	}

	private suspend fun doRequest(client: HttpClient): GoogleResponse {
		return client.post<GoogleResponse> {
			url {
				host = "vision.googleapis.com"
				path("v1/images:annotate")
				parameters.append("key", "AIzaSyAJkP7gXXuNijeRy7OugpBozZoQ7dfVeQo")  // TODO should be saved in local file!
				protocol = URLProtocol.HTTPS
			}
			contentType(ContentType.Application.Json)
			body = RequestBuilder.build(path)
		}
	}

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

	private fun createDetections(response: GoogleResponse): Detection {
		val detection = Detection(path)
		if (response != null) {
			if (response.responses != null && response.responses.isNotEmpty()) {
				if (response.responses.size > 1)
					logger.warn("There are > 1 responses in Google HTTP response for the picture $path")
				val annotations = response.responses[0].landmarkAnnotations
				if (annotations != null && annotations.isNotEmpty()) {
					val descriptions = annotations.map(LandmarkAnnotation::description)
					val state: Detections = if (descriptions.isEmpty()) Detections.NO else Detections.FOUND
					detection.state = state
					detection.descriptions = descriptions
				}
				else
					detection.state = Detections.NO
			} else
				logger.error("There is no responses array in the response instance")
		}
		else
			logger.error("Response instance is NULL")
		return detection
	}
}