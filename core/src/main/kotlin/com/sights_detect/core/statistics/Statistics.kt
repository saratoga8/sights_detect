package com.sights_detect.core.statistics

internal interface Statistics {
	fun getFoundPicsNum(): Int
	fun getFoundObjects(): List<DetectionInfo>
	fun getErrors(): List<DetectionError>
}