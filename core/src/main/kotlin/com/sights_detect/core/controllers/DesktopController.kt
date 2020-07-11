package com.sights_detect.core.controllers

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.DetectionsStorage
import java.util.*

open class DesktopController(paths: List<String>, override val properties: Properties): Controller<String>(paths) {
	override val storage = DetectionsStorage<Hashtable<String, Detection>>()

	init {
		require(paths.isNotEmpty()) { "There is no given directories for searching pictures" }
		require(properties.isNotEmpty()) { "The given properties instance is EMPTY" }
	}
}