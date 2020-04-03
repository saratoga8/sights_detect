package com.sights_detect.core.seekers.pics

import com.sights_detect.core.detections.Detection
import org.apache.logging.log4j.kotlin.Logging
import java.io.File


class DesktopFS(private val path: String, private val recursive: Boolean = true): PicsSeeker, Logging {
	private val dirPath: String = path
	init {
		require(File(dirPath).exists())    { "The given path $path doesn't exist" }
		require(File(dirPath).isDirectory) { "The given path $path isn't directory" }
	}

	override fun find(): List<Detection> {
		logger.debug("Finding pictures inside $dirPath")
		val results = mutableListOf<Detection>()
		for (pic_format in picFormats) {
			fun getAllFiles(): List<File> = if (recursive) { File(dirPath).walk().toList() } else { File(dirPath).listFiles().toList() }
			results.addAll(getAllFiles().filter { it.extension == pic_format }.map { Detection(it.absolutePath) }.toList())
		}
		return results
	}
}