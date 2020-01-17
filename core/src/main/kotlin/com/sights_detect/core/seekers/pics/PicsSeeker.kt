package com.sights_detect.core.seekers.pics

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.seekers.Seeker

internal interface PicsSeeker: Seeker<Detection> {
	val picFormats: Array<String>
		get() = arrayOf("jpg", "jpeg")
}