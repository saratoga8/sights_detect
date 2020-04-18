package com.sights_detect.core.seekers.objects.google

internal data class GoogleRequest(
    val requests: List<Request>
)

internal data class Request(
		val features: List<Feature>,
		val image: Image
)

internal data class Feature(
		val maxResults: Int,
		val type: String
)

internal data class Image(val content: String)