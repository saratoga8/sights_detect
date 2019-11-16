package com.sights_detect.core.seekers

import com.sights_detect.core.detections.Detection

internal interface PicsSeeker: Seeker<Detection> {
	val picFormats: Array<String>
		get() = arrayOf("jpg", "jpeg")
}