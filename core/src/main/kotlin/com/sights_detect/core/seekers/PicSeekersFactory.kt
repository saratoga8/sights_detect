package com.sights_detect.core.seekers

import java.io.File

object PicSeekersFactory {
	fun getPicSeekers(path: String): List<Seeker> {
		val dir = File(path)
		require(dir.exists()) { "The given path $path doesn't exist" }
		require(dir.isDirectory) { "The given path $path isn't directory" }

		if (dir.list() == null || dir.list().isEmpty()) return listOf()

		val seekers = mutableListOf<PicsSeeker>()
		try {
			if (dir.listFiles().any { it.isFile }) seekers.add(DesktopFS(path, recursive = false))
		}
		catch (e: IllegalArgumentException) {}
		seekers.addAll(dir.listFiles().filter { it.isDirectory && it.listFiles().isNotEmpty() }.map { DesktopFS(it.absolutePath) }.toList())
		return seekers
	}
}