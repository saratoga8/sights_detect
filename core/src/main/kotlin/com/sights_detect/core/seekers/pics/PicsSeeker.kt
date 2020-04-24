package com.sights_detect.core.seekers.pics

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.seekers.Seeker
import org.apache.logging.log4j.kotlin.Logging

internal abstract class PicsSeeker: Seeker<Detection>, Logging {
	val picFormats: Array<String>
		get() = arrayOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "raw", "ico", "pdf", "tiff")

	override fun isStopped(): Boolean = stopped

	protected var stopped = true

	override fun stop() {
		super.stop()
		stopped = true
	}
}