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

class DetectionsStorage<T>(private val path: String = "detections.json"): Logging {
	public fun save(detections: T, type: Type) {
		logger.debug("Saving detections in ${File(path).absolutePath}")
		try {
			FileWriter(path).use { writer ->
				Gson().toJson(detections, type, writer)
			}
		} catch (e: JsonParseException) {
			logger.error("Can't save detections to the file $path: $e")
		} catch (e: IOException) {
			logger.error("Can't save detections to the file $path: $e")
		}
	}

	public fun load(type: Type): T? {
		logger.debug("Loading detections from ${File(path).absolutePath}")
		try {
			FileReader(path).use { reader ->
				return Gson().fromJson(JsonReader(reader), type)
			}
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