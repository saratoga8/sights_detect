package com.sights_detect.core.seekers.objects

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.seekers.Seeker
import com.sights_detect.core.seekers.objects.google.GoogleObjSeeker
import java.util.*

internal object ObjectSeekersFactory {
	fun getObjSeekers(paths: List<String>, properties: Properties): List<Seeker<Detection>> {
		return paths.map { path -> GoogleObjSeeker(path, properties) }
	}
}
