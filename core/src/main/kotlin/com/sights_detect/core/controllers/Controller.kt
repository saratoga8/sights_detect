package com.sights_detect.core.controllers

import com.google.gson.reflect.TypeToken
import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.detections.DetectionsStorage
import com.sights_detect.core.seekers.Seeker
import com.sights_detect.core.seekers.objects.ObjectSeekersFactory
import com.sights_detect.core.seekers.pics.PicSeekersFactory
import com.sights_detect.core.statistics.Statistics
import com.sights_detect.core.statistics.StatisticsData
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.Logging
import java.lang.Thread.sleep
import java.lang.reflect.Type
import java.util.*


abstract class Controller<in T>(private val paths: Iterable<T>): Logging {
	var detections: Hashtable<String, Detection> = Hashtable()
		protected set
	protected abstract val storage: DetectionsStorage<Hashtable<String, Detection>>
	protected abstract val properties: Properties
	private val type: Type = object : TypeToken<Hashtable<String, Detection>>() {}.type

	@get:Synchronized @set:Synchronized
	var stopped = false

	internal val seekers: MutableList<Seeker<Detection>> = mutableListOf()

	fun getStatistics(): Statistics = StatisticsData(detections.values.toList())

	fun getDetections(): List<Detection> = detections.values.toList()

	fun stop() {
		seekers.forEach { it.stop() }
		while (seekers.isNotEmpty()) {
			seekers.removeAll { it.isStopped() }
		}
		saveDetections()
		stopped = true
	}

	suspend fun start() {
		loadDetections()
		detectNewPics()
		detectObjects()
		saveDetections()
		stopped = true
	}

	protected suspend fun detectObjects() {
		findObjects(detections.values.toList()).awaitAll().flatten().forEach { found ->
			val prevDetection = detections[found.path]
			if (prevDetection != null) {
				if (prevDetection.state != found.state) {
					prevDetection.state = found.state
					prevDetection.descriptions = found.descriptions
				}
				if (found.error.isNotEmpty()) { prevDetection.error = found.error }
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
		val dispatcher: ExecutorCoroutineDispatcher = newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "Find Objects")
		return findBySeekers(objSeekers, dispatcher, 1000L).also { seekers.addAll(objSeekers) }
	}



	protected open fun findNewPics(): List<Deferred<List<Detection>>> {
		val picSeekers = buildPicSeekers()
		return findBySeekers(picSeekers).also { seekers.addAll(picSeekers) }
	}

	private fun <T> findBySeekers(seekers: Set<Seeker<T>>, dispatcher: CoroutineDispatcher = Dispatchers.IO, sleepSeconds: Long = 0): List<Deferred<List<T>>> {
		return seekers.map { CoroutineScope(dispatcher).async {
			if (sleepSeconds != 0L) { sleep(sleepSeconds) }
			it.find()
		} }
	}

	internal open fun buildObjSeekers(paths: List<String>): Set<Seeker<Detection>> {
		return ObjectSeekersFactory.getObjSeekers(paths, properties).toSet()
	}

	internal open fun buildPicSeekers(): Set<Seeker<Detection>> {
		val picSeekers: MutableSet<Seeker<Detection>> = mutableSetOf()
		paths.forEach { picSeekers.addAll(PicSeekersFactory.getPicSeekers(it.toString())) }
		return picSeekers
	}

	protected fun saveDetections() {
		val selectedDetections = detections.filterValues { it.state != Detections.PROCESSING }
		storage.save(Hashtable(selectedDetections), type)
	}

	protected fun loadDetections() {
		val loaded = storage.load(type)
		if(loaded != null)
			detections = loaded
	}
}