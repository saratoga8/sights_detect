package com.sights_detect.core.seekers.pics

import com.sights_detect.core.detections.Detection
import org.apache.logging.log4j.kotlin.Logging
import java.io.File


internal open class DesktopFS(private val dirPath: String, private val recursive: Boolean = true): PicsSeeker(), Logging {
	init {
		require(File(dirPath).exists())    { "The given path $dirPath doesn't exist" }
		require(File(dirPath).isDirectory) { "The given path $dirPath isn't directory" }
	}

	override fun find(): List<Detection> {
		logger.debug("Finding pictures inside $dirPath")
		stopped = false
		val results = mutableListOf<Detection>()
		for (pic_format in picFormats) {
			if (stopped) break
			results.addAll(getAllFiles().filter { it.endsWith(pic_format, true) }.map { Detection(it) }.toList())
		}
		return results.also { stopped = true }
	}

	protected open fun getAllFiles(): List<String> {
		if (recursive) return File(dirPath).walk().toList().map { it.absolutePath }
		val files = File(dirPath).listFiles()
		return (files?.toList()?: listOf()).map { it.absolutePath }
	}
}