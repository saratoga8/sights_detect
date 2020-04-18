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
		protected set
	protected abstract val storage: DetectionsStorage<Hashtable<String, Detection>>
	protected abstract val properties: Properties
	private val type: Type = object : TypeToken<Hashtable<String, Detection>>() {}.type

	protected val seekers: MutableList<Seeker<Detection>> = mutableListOf()

	fun getDetections(): List<Detection> = detections.values.toList()

	fun stop() {
		GlobalScope.launch {
			seekers.forEach { it.stop() }
			while (seekers.isNotEmpty()) {
				seekers.removeAll { it.isStopped() }
			}
		}
	}

	fun start() {
		GlobalScope.launch {
			detectNewPics()
			detectObjects()
		}
	}

	protected suspend fun detectObjects() {
		findObjects(detections.values.toList()).awaitAll().flatten().forEach { found ->
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

	protected open suspend fun detectNewPics() = findNewPics().awaitAll().flatten().forEach {
		detections.putIfAbsent(it.path, it)
	}

	protected open fun findObjects(detections: List<Detection>): List<Deferred<List<Detection>>> {
		val paths = detections.filter { it.state == Detections.UNKNOWN }.map { it.path }
		val objSeekers = buildObjSeekers(paths)
		return findBySeekers(objSeekers).also { seekers.addAll(objSeekers) }
	}

	protected open fun findNewPics(): List<Deferred<List<Detection>>> {
		val picSeekers = buildPicSeekers()
		return findBySeekers(picSeekers).also { seekers.addAll(picSeekers) }
	}

	private fun <T> findBySeekers(seekers: Set<Seeker<T>>): List<Deferred<List<T>>> {
		return seekers.map { CoroutineScope(Dispatchers.IO).async { it.find() } }
	}

	protected open fun buildObjSeekers(paths: List<String>): Set<Seeker<Detection>> {
		return ObjectSeekersFactory.getObjSeekers(paths, properties).toSet()
	}

	protected open fun buildPicSeekers(): Set<Seeker<Detection>> {
		val picSeekers: MutableSet<Seeker<Detection>> = mutableSetOf()
		paths.forEach { picSeekers.addAll(PicSeekersFactory.getPicSeekers(it.toString())) }
		return picSeekers
	}

	protected fun saveDetections() {
		val selectedDetections = detections.filterValues { it.state != Detections.PROCESSING }
		storage.save(Hashtable<String, Detection>(selectedDetections), type)
	}

	protected fun loadDetections() {
		val loaded = storage.load(type)
		if(loaded != null)
			detections = loaded
	}
}