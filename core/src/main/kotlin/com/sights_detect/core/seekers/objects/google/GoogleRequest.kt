package com.sights_detect.core.seekers.objects.google

data class GoogleRequest(
    val requests: List<Request>
)

data class Request(
		val features: List<Feature>,
		val image: Image
)

data class Feature(
		val maxResults: Int,
		val type: String
)

data class Image(val content: String)