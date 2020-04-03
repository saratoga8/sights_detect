package com.sights_detect.core.seekers.objects.google

import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.network.sockets.ConnectTimeoutException
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.util.*
import kotlin.Exception

open class GoogleVision(private val properties: Properties): Logging {
	open val request = com.sights_detect.core.net.Request(properties)
	var error = ""

	suspend fun doRequest(imgPath: String): GoogleResponse {
		if(properties == null) {
			logger.error("The given properties instance is NULL")
			return GoogleResponse(listOf())
		}
		return try {
			request.post(buildRequest(imgPath))
		}
		catch (e: ClientRequestException) {
			exception_response(e, "HTTP request to Google Vision Service has failed")
		}
		catch (e: HttpRequestTimeoutException) {
			exception_response(e, "HTTP request to Google Vision Service timed out")
		}
		catch (e: ConnectTimeoutException) {
			exception_response(e, "Connection to Google Vision Service timed out")
		}
	}

	private fun exception_response(exception: Exception, msg: String): GoogleResponse {
		error = msg
//		logger.error(exception.toString())
		return GoogleResponse(listOf())
	}

	private fun encodeFile(path: String): String {
//		logger.debug("Encoding picture in file $path")              makes error during testing
		require(File(path).exists())    { "The given image's path $path doesn't exist" }
		val bytes = File(path).readBytes()
		return Base64.getEncoder().encodeToString(bytes)
	}

	protected fun buildRequest(path: String): Any {
		val features = listOf(Feature(1, "LANDMARK_DETECTION"), Feature(2, "WEB_DETECTION"))
		val requests = listOf(Request(features, Image(encodeFile(path))))
		return GoogleRequest(requests)
	}
}