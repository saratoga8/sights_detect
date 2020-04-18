package com.sights_detect.core.statistics

import com.sights_detect.core.detections.Detection

internal data class DetectionInfo(val foundDetection: Detection) {
	val path: String = foundDetection.path
	val descriptions: List<String> = foundDetection.descriptions
}