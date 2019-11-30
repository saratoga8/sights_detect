package com.sights_detect.core.controllers

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.DetectionsStorage
import java.util.*

open class DesktopController(paths: List<String>): Controller<String>(paths) {
	override val storage = DetectionsStorage<Hashtable<String, Detection>>()
}