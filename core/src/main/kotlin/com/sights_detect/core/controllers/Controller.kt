package com.sights_detect.core.controllers

import com.google.gson.reflect.TypeToken
import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.detections.DetectionsStorage
import com.sights_detect.core.seekers.Seeker
import com.sights_detect.core.seekers.objects.ObjectSeekersFactory
import com.sights_detect.core.seekers.pics.PicSeekersFactory
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.Logging
import java.lang.reflect.Type
import java.util.*


abstract class Controller<in T>(private val paths: Iterable<T>): Logging {
	var detections: Hashtable<String, Detection> = Hashtable()
		private set
	protected abstract val storage: DetectionsStorage<Hashtable<String, Detection>>
	protected abstract val properties: Properties
	private val type: Type = object : TypeToken<Hashtable<String, Detection>>() {}.type

	fun stop() {

	}

	fun start() {
		GlobalScope.launch {
			detectNewPics()
			detectObjects()
		}
	}

	private suspend fun detectObjects() {
		findObjects(detections.values.toList()).forEach { found ->
			val prevDetection = detections[found.path]
			if (prevDetection != null) {
				if (prevDetection.state != found.state) {
					prevDetection.state = found.state
					prevDetection.descriptions = found.descriptions
				}
			} else
				logger.error("Detection instance of ${found.path} has removed")
		}
	}

	protected open suspend fun detectNewPics() = findNewPics().forEach { found -> detections.putIfAbsent(found.path, found) }

	protected open suspend fun findObjects(detections: List<Detection>): List<Detection> {
		val paths = detections.filter { detection -> detection.state == Detections.UNKNOWN }.map { detection -> detection.path }
		return findBySeekers(buildObjSeekers(paths))
//		val founds = mutableListOf<Detection>()
//		buildObjSeekers(paths).map { seeker -> CoroutineScope(Dispatchers.IO).async { founds.addAll(seeker.find()) } }.forEach { job -> job.await() }
//		return founds
	}

	protected open suspend fun findNewPics(): List<Detection> {
//		val founds = mutableListOf<Detection>()
//		buildPicSeekers().map { seeker -> CoroutineScope(Dispatchers.IO).async { founds.addAll(seeker.find()) } }.forEach { job -> job.await() }
//		return founds
		return findBySeekers(buildPicSeekers())
	}

	private suspend fun findBySeekers(seekers: Set<Seeker<Detection>>): List<Detection> {
		val founds = mutableListOf<Detection>()
		seekers.map { seeker -> CoroutineScope(Dispatchers.IO).async { founds.addAll(seeker.find()) } }.forEach { job -> job.await() }
		return founds
	}

	protected open suspend fun buildObjSeekers(paths: List<String>): Set<Seeker<Detection>> {
		return ObjectSeekersFactory.getObjSeekers(paths, properties).toSet()
	}

	protected open fun buildPicSeekers(): Set<Seeker<Detection>> {
		val picSeekers: MutableSet<Seeker<Detection>> = mutableSetOf()
		paths.forEach { path -> picSeekers.addAll(PicSeekersFactory.getPicSeekers(path.toString())) }
		return picSeekers
	}

	protected fun saveDetections() {
		val selectedDetections = detections.filterValues { detection -> detection.state != Detections.PROCESSING }
		storage.save(Hashtable<String, Detection>(selectedDetections), type)
	}

	protected fun loadDetections() {
		val loaded = storage.load(type)
		if(loaded != null)
			detections = loaded
	}
}