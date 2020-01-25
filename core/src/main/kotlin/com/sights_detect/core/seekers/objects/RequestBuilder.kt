package com.sights_detect.core.seekers.objects

import java.io.File
import java.util.*

interface RequestBuilder {
	fun encodeFile(path: String): String {
		require(File(path).exists())    { "The given image's path $path doesn't exist" }
		val bytes = File(path).readBytes()
		return Base64.getEncoder().encodeToString(bytes)
	}

	fun build(path: String): Any;
}