package com.sights_detect.core.statistics

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections

class StatisticsData(private val detections: List<Detection>): Statistics {
	override fun getFoundPicsNum(): Int = detections.size

	override fun getFoundObjects(): List<DetectionInfo> {
		return detections.filter { it.state == Detections.FOUND }.map { DetectionInfo(it) }
	}

	override fun getErrors(): List<DetectionError> {
		return detections.filter { it.error.isNotEmpty() }.map { DetectionError(it) }
	}
}