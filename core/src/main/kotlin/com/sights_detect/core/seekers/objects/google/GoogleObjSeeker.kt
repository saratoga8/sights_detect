package com.sights_detect.core.seekers.objects.google

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.seekers.objects.ObjectSeeker
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.util.*


class GoogleObjSeeker(private val path: String, private val properties: Properties): ObjectSeeker, Logging {
	override fun find(): List<Detection> {
		if (properties.isNotEmpty()) {
			if (File(path).exists()) {
				val response = runBlocking { GoogleVision(properties).doRequest(path) }
				return listOf(createDetections(response))
			} else logger.warn( "File of picture $path DOESN'T exist")
		} else logger.error("Google Cloud Vision API info from properties file hasn't loaded")
		return listOf()
	}

	private fun createDetections(response: GoogleResponse): Detection {
		val detection = Detection(path)
		if (response != null) {
			if (response.responses != null && response.responses.isNotEmpty()) {
				if (response.responses.size > 1)
					logger.warn("There are > 1 responses in Google HTTP response for the picture $path")
				val annotations = response.responses[0].landmarkAnnotations
				if (annotations != null && annotations.isNotEmpty()) {
					val descriptions = annotations.map(LandmarkAnnotation::description)
					val state: Detections = if (descriptions.isEmpty()) Detections.NO else Detections.FOUND
					detection.state = state
					detection.descriptions = descriptions
				}
				else
					detection.state = Detections.NO
			} else
				logger.error("There is no responses array in the response instance")
		}
		else
			logger.error("Response instance is NULL")
		return detection
	}
}