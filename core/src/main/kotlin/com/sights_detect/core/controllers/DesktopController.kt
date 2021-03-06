package com.sights_detect.core.controllers

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.DetectionsStorage
import java.security.InvalidParameterException
import java.util.*

open class DesktopController(paths: List<String>, override val properties: Properties): Controller<String>(paths) {
	override val storage = DetectionsStorage<Hashtable<String, Detection>>()

	init {
		if(paths.isEmpty())
			throw InvalidParameterException("There is no given directories for searching pictures")
	}
}