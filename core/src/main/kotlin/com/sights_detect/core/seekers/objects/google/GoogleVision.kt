package com.sights_detect.core.seekers.objects.google

import java.io.File
import java.util.*

class GoogleVision(private val properties: Properties) {
	suspend fun request(imgPath: String): GoogleResponse {
		return com.sights_detect.core.net.Request(properties).post(build(imgPath))
	}

	private fun encodeFile(path: String): String {
		require(File(path).exists())    { "The given image's path $path doesn't exist" }
		val bytes = File(path).readBytes()
		return Base64.getEncoder().encodeToString(bytes)
	}

	private fun build(path: String): Any {
		val features = listOf(Feature(1, "LANDMARK_DETECTION"), Feature(2, "WEB_DETECTION"))
		val requests = listOf(Request(features, Image(encodeFile(path))))
		return GoogleRequest(requests)
	}
}