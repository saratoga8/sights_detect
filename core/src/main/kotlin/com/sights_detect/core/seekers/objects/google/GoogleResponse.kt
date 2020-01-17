package com.sights_detect.core.seekers.objects.google

data class GoogleResponse(
    val responses: List<Response>
)

data class Response(
		val landmarkAnnotations: List<LandmarkAnnotation>,
		val webDetection: WebDetection
)

data class LandmarkAnnotation(
		val boundingPoly: BoundingPoly,
		val description: String,
		val locations: List<Location>,
		val mid: String,
		val score: Double
)

data class WebDetection(
		val bestGuessLabels: List<BestGuessLabel>,
		val visuallySimilarImages: List<VisuallySimilarImage>,
		val webEntities: List<WebEntity>
)

data class BoundingPoly(
		val vertices: List<Vertice>
)

data class BestGuessLabel(
		val label: String
)

data class LatLng(
		val latitude: Double,
		val longitude: Double
)

data class Location(
		val latLng: LatLng
)

data class WebEntity(
		val description: String,
		val entityId: String,
		val score: Double
)

data class VisuallySimilarImage(
		val url: String
)

data class Vertice(
		val x: Int,
		val y: Int
)