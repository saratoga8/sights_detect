package com.sights_detect.core.seekers.pics

import com.sights_detect.core.detections.Detection
import com.sights_detect.core.seekers.Seeker
import java.io.File

internal object PicSeekersFactory {
	fun getPicSeekers(path: String): List<Seeker<Detection>> {
		val dir = File(path)
		require(dir.exists()) { "The given path $path doesn't exist" }
		require(dir.isDirectory) { "The given path $path isn't directory" }

		if (dir.list() == null || dir.list().isEmpty()) return listOf()

		val seekers = mutableListOf<PicsSeeker>()
		val files = dir.listFiles()
		try {
			if (files.any { it.isFile }) seekers.add(DesktopFS(path, recursive = false))
		}
		catch (e: IllegalArgumentException) {}
		seekers.addAll(files.filter { it.isDirectory && it.list().isNotEmpty() }.map { DesktopFS(it.absolutePath) }.toList())
		return seekers
	}
}