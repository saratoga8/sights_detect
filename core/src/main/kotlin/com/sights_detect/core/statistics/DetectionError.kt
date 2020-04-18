package com.sights_detect.core.statistics

import com.sights_detect.core.detections.Detection

internal data class DetectionError(val foundDetection: Detection)  {
	val path: String = foundDetection.path
	val error: String = foundDetection.error
}