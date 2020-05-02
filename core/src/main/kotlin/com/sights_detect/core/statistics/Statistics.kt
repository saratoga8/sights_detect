package com.sights_detect.core.statistics

interface Statistics {
	fun getFoundPicsNum(): Int
	fun getFoundObjects(): List<DetectionInfo>
	fun getErrors(): List<DetectionError>
}