package com.sights_detect.core.seekers.objects.google

import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.util.*

open class GoogleVision(private val properties: Properties): Logging {
	suspend fun request(imgPath: String): GoogleResponse {
		if(properties == null) {
			logger.error("The given properties instance is NULL")
			return GoogleResponse(listOf())
		}
		return com.sights_detect.core.net.Request(properties).post(buildRequest(imgPath))
	}

	private fun encodeFile(path: String): String {
		logger.debug("Encoding picture in file $path")
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