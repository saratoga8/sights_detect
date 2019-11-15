package com.sights_detect.core.seekers

internal interface PicsSeeker: Seeker {
	val picFormats: Array<String>
		get() = arrayOf("jpg", "jpeg")
}