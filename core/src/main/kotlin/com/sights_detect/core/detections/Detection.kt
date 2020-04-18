package com.sights_detect.core.detections

import com.sights_detect.core.detections.Detections.UNKNOWN


internal data class Detection(var path: String, var error: String = "") {
	var state: Detections = UNKNOWN
	var descriptions: List<String> = listOf()
	init {
		require(!path.isBlank()) { "Can't create detection instance by empty string" }
		if (path.contains(DELIMITER)) {
			require(path.count { char -> char == DELIMITER } == DELIMETERS_NUM) { "String presentation of Detection should contain $DELIMETERS_NUM of delimiter $DELIMITER" }
			state = Detections.valueOf(path.split(DELIMITER)[1])
			descriptions = listOf(path.split(DELIMITER)[2])
			path = path.split(DELIMITER)[0]
		}
	}

	constructor(str: String, state: Detections, descriptions: List<String>): this(str) {
		this.state = state
		this.descriptions = descriptions
	}

	override fun toString(): String = "$path$DELIMITER $descriptions$DELIMITER $state"

	companion object {
		const val DELIMITER = ';'
		const val DELIMETERS_NUM = 2
	}
}