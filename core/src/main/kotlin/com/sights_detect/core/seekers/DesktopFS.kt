package com.sights_detect.core.seekers

import com.sights_detect.core.detections.Detection
import java.io.File


class DesktopFS(private val path: String, private val recursive: Boolean = true): PicsSeeker {
	private val dirPath: String = path
	init {
		require(File(dirPath).exists()) { "The given path $path doesn't exist" }
		require(File(dirPath).isDirectory) { "The given path $path isn't directory" }
	}

	override fun find(): List<Detection> {
		val results = mutableListOf<Detection>()
		for (pic_format in picFormats) {
			fun action(): List<File> = if (recursive) { File(dirPath).walk().toList() } else { File(dirPath).listFiles().toList() }
			results.addAll(action().filter { it.extension == pic_format }.map { Detection(it.absolutePath) }.toList())
		}
		return results
	}
}