package com.sights_detect.core.seekers.objects.google

import com.sights_detect.core.seekers.objects.RequestBuilder

class GoogleRequestBuilder: RequestBuilder {
	override fun build(path: String): Any {
		val features = listOf(Feature(1, "LANDMARK_DETECTION"), Feature(2, "WEB_DETECTION"))
		val requests = listOf(Request(features, Image(encodeFile(path))))
		return GoogleRequest(requests)
	}
}