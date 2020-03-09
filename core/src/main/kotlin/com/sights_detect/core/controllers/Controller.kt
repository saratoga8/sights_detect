package com.sights_detect.core.controllers

import com.google.gson.reflect.TypeToken
import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.DetectionsStorage
import com.sights_detect.core.seekers.Seeker
import com.sights_detect.core.seekers.pics.PicSeekersFactory
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.Logging
import java.lang.Thread.sleep
import java.lang.reflect.Type
import java.util.*


abstract class Controller<in T>(private val paths: Iterable<T>): Logging {
	var detections: Hashtable<String, Detection> = Hashtable()
		private set
	protected abstract val storage: DetectionsStorage<Hashtable<String, Detection>>
	private val type: Type = object : TypeToken<Hashtable<String, Detection>>() {}.type

	fun start() {
		GlobalScope.launch {
			findNewPics().forEach() { found -> detections.putIfAbsent(found.path, found) }
		}
	}

	protected open suspend fun findNewPics(): List<Detection> {
		val founds = mutableListOf<Detection>()
		buildPicSeekers().map { seeker -> CoroutineScope(Dispatchers.IO).async { founds.addAll(seeker.find()) } }.forEach { job -> job.await() }
		return founds
	}

	protected open fun buildPicSeekers(): Set<Seeker<Detection>> {
		val picSeekers: MutableSet<Seeker<Detection>> = mutableSetOf()
		paths.forEach { path -> picSeekers.addAll(PicSeekersFactory.getPicSeekers(path.toString())) }
		return picSeekers
	}

	protected fun saveDetections() = storage.save(detections, type)
	protected fun loadDetections() {
		val loaded = storage.load(type)
		if(loaded != null)
			detections = loaded
	}
}