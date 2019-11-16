package com.sights_detect.core.controllers

import com.google.gson.reflect.TypeToken
import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.DetectionsStorage
import com.sights_detect.core.seekers.PicSeekersFactory
import com.sights_detect.core.seekers.Seeker
import org.apache.logging.log4j.kotlin.Logging
import java.lang.reflect.Type


abstract class Controller<in T>(private val paths: Iterable<T>): Logging {
	var detections: MutableMap<String, Detection> = mutableMapOf()
	protected abstract val storage: DetectionsStorage<Map<String, Detection>>
	private val type: Type = object : TypeToken<Map<String, Detection>>() {}.type

	public fun start() {
		findNewPics().forEach { found -> detections.putIfAbsent(found.path, found) }
	}

	protected open fun findNewPics(): List<Detection> {
		val founds = mutableListOf<Detection>()
		buildPicSeekers().forEach { seeker -> founds.addAll(seeker.find()) }
		return founds
	}

	private fun buildPicSeekers(): Set<Seeker<Detection>> {
		val picSeekers: MutableSet<Seeker<Detection>> = mutableSetOf()
		paths.forEach { path -> picSeekers.addAll(PicSeekersFactory.getPicSeekers(path.toString())) }
		return picSeekers
	}

	protected fun saveDetections() = storage.save(detections, type)
	protected fun loadDetections() {
		detections = storage.load(type)?.toMutableMap() ?: return
	}
}