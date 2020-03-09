package com.sights_detect.core.seekers.objects.google

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.net.URL

internal class GoogleVisionTest {

	@Test
	fun request() {
	}

	@Test
	fun buildRequest() {
		//		val url = javaClass.classLoader.getResource("man.jpg")
		val url = URL("file:/home/saratoga/progs/SightsDetect/core/src/test/resources/man.jpg")
		Assertions.assertNotNull(url, "Can't find a file with picture in resources")

	}
}