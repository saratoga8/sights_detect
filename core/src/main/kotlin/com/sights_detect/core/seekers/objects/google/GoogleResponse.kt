package com.sights_detect.core.seekers.objects.google

internal data class GoogleResponse(var _responses: List<Response>?) {
	var responses: List<Response>? = listOf()
	set(value) {
		value
		value ?: listOf()
	}
}

internal data class Response(val webDetection: WebDetection?) {
	var landmarkAnnotations: List<LandmarkAnnotation>? = listOf()
		set(value) {
			value
			value ?: listOf()
		}
}

internal data class LandmarkAnnotation(
		val boundingPoly: BoundingPoly?,
		val description: String,
		val locations: List<Location>?,
		val mid: String,
		val score: Double
)

internal data class WebDetection(
		val bestGuessLabels: List<BestGuessLabel>?,
		val visuallySimilarImages: List<VisuallySimilarImage>?,
		val webEntities: List<WebEntity>?
)

internal data class BoundingPoly(
		val vertices: List<Vertice>?
)

internal data class BestGuessLabel(
		val label: String
)

internal data class LatLng(
		val latitude: Double,
		val longitude: Double
)

internal data class Location(
		val latLng: LatLng
)

internal data class WebEntity(
		val description: String,
		val entityId: String,
		val score: Double
)

internal data class VisuallySimilarImage(
		val url: String
)

internal data class Vertice(
		val x: Int,
		val y: Int
)