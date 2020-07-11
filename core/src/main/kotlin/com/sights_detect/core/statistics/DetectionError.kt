package com.sights_detect.core.statistics

import com.sights_detect.core.detections.Detection

data class DetectionError(val foundDetection: Detection)  {
	val path: String = foundDetection.path
	val error: String = foundDetection.error
	override fun toString(): String {
		return "File: $path; Error: $error"
	}
}