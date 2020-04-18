package com.sights_detect.core.seekers.objects.google

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.seekers.objects.ObjectSeeker
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.util.*


internal class GoogleObjSeeker(private val path: String, private val properties: Properties): ObjectSeeker(), Logging {
	private val vision: GoogleVision = GoogleVision(properties)

	override fun find(): List<Detection> {
		if (properties.isNotEmpty()) {
			return if (File(path).exists()) {
				stopped = false
				val response = runBlocking { vision.doRequest(path) }
				listOf(createDetections(response))
			} else listOf(Detection(path, "File of picture $path DOESN'T exist"))
		} else logger.error("Google Cloud Vision API info from properties file hasn't loaded")
		return listOf()
	}

	override fun stop() {
		vision.stop()
		stopped = true
	}

	private fun createDetections(googleResponse: GoogleResponse): Detection {
		val detection = Detection(path)
		val responses = googleResponse.responses
		if (!responses.isNullOrEmpty()) {
			if (responses.size > 1)
				logger.warn("There are > 1 responses in Google HTTP response for the picture $path")
			val annotations = responses[0].landmarkAnnotations
			if (!annotations.isNullOrEmpty()) {
				val descriptions = annotations.map(LandmarkAnnotation::description)
				val state: Detections = if (descriptions.isEmpty()) Detections.NO else Detections.FOUND
				detection.state = state
				detection.descriptions = descriptions
			}
			else
				detection.state = Detections.NO
		} else
			return Detection(path, "There is no responses array in the response instance")
		return detection
	}
}