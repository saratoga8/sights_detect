package com.sights_detect.core.seekers

import com.sights_detect.core.detections.Detection

interface Seeker {
	fun find(): List<Detection>
	fun stop() {}
}