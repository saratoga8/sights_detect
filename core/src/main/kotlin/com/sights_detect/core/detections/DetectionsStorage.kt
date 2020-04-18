package com.sights_detect.core.detections

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type

internal class DetectionsStorage<T>(private val path: String = "detections.json"): Logging {
	fun save(detections: T, type: Type) {
		logger.debug("Saving detections in ${File(path).absolutePath}")

		exec {
			FileWriter(path).use { writer ->
				Gson().toJson(detections, type, writer)
			}
		}
	}

	fun load(type: Type): T? {
		logger.debug("Loading detections from ${File(path).absolutePath}")
		return exec {
			FileReader(path).use { reader ->
				return@exec Gson().fromJson(JsonReader(reader), type)
			}
		}
	}

	private fun <R>exec(other: () -> R?): R? {
		try {
			other
		}
		catch (e: JsonParseException) {
			logger.error("Can't load detections from the file $path: $e")
		}
		catch (e: IOException) {
			logger.error("Can't load detections from the file $path: $e")
		}
		return null
	}
}