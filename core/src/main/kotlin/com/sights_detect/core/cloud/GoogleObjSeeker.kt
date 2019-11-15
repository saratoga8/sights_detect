package com.sights_detect.core.cloud

import com.google.cloud.vision.v1.*
import com.google.protobuf.ByteString
import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.seekers.Seeker
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class GoogleObjSeeker(private val path: String): Seeker, Logging {

	private fun detectLandmarks(path: String): List<EntityAnnotation> {
		val requests: List<AnnotateImageRequest> = getAnnotateImgRequests(path)
		val annotations: MutableList<EntityAnnotation> = mutableListOf()
		val client = ImageAnnotatorClient.create()
		try {
			val response: BatchAnnotateImagesResponse = client.batchAnnotateImages(requests)
			for (res in response.responsesList) {
				if (res.hasError()) {
					logger.error("ERROR in response from google cloud API: " + res.error.message)
					break
				}
				annotations.addAll(res.landmarkAnnotationsList)
			}
		}
		finally {
			client.close()
		}
		return annotations
	}



	@Throws(IOException::class)
	private fun getAnnotateImgRequests(path: String): List<AnnotateImageRequest> {
		val imgBytes: ByteString = ByteString.readFrom(FileInputStream(path))
		val img: Image = Image.newBuilder().setContent(imgBytes).build()
		val feat: Feature = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build()
		val request: AnnotateImageRequest = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
		return listOf(request)
	}


	override fun find(): List<Detection> {
		if (File(path).exists()) {
			try {
				val descriptions = detectLandmarks(path).map(EntityAnnotation::getDescription).toList()
				return listOf(createDetection(descriptions))
			} catch (e: IOException) {
				logger.warn( "Can't detect landmarks in the image " + path + ": " + e.message)
			}
		} else logger.warn( "File of picture $path DOESN'T exist")
		return listOf()
	}

	private fun createDetection(descriptions: List<String>): Detection {
		val detection = Detection(path)
		detection.descriptions = descriptions
		val state: Detections = if (descriptions.isEmpty()) Detections.NO else Detections.FOUND
		detection.state = state
		return detection
	}
}