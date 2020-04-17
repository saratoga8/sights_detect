package com.sights_detect.core.seekers

interface Seeker<T> {
	fun isStopped(): Boolean { return true }
	fun find(): List<T>
	fun stop() { }
}