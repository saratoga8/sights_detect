package com.sights_detect.core.seekers.objects.google

import java.io.File
import java.util.*

object RequestBuilder {
	private fun encodeFile(path: String): String  {
		val bytes = File(path).readBytes()
		return Base64.getEncoder().encodeToString(bytes)
	}

	fun build(path: String): GoogleRequest {
		require(File(path).exists())    { "The given image's path $path doesn't exist" }
		val features = listOf(Feature(1, "LANDMARK_DETECTION"), Feature(2, "WEB_DETECTION"))
		val requests = listOf(Request(features, Image(encodeFile(path))))
		return GoogleRequest(requests)
	}
}