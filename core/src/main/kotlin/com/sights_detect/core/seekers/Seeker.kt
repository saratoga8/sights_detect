package com.sights_detect.core.seekers

interface Seeker<T> {
	fun find(): List<T>
	fun stop() {}
}