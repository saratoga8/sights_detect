package com.sights_detect.core.seekers.objects

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.seekers.Seeker

internal abstract class ObjectSeeker: Seeker<Detection> {
	override fun isStopped(): Boolean = stopped

	protected var stopped = true
}
