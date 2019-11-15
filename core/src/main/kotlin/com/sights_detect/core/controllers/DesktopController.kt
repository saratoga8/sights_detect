package com.sights_detect.core.controllers

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.DetectionsStorage

open class DesktopController(private val paths: List<String>): Controller<String>(paths) {
	override val storage = DetectionsStorage<Map<String, Detection>>()
}