package com.sights_detect.core.seekers.objects.google

import com.sights_detect.core.net.Request
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.FileInputStream
import java.util.*
import kotlin.test.fail

internal class GoogleVisionTest {
	private var path: String = ""
	private val properties: Properties = Properties()

	@BeforeEach
	fun setUp() {
		val fileName = "google.properties"
		var url = javaClass.classLoader.getResource(fileName)
		Assert.assertNotNull("Can't find resource file $fileName", url)
		properties.load(FileInputStream(url.path))

		url = javaClass.classLoader.getResource("man.jpg")
		Assertions.assertNotNull(url, "Can't find a file with picture in resources")
		path = url.path
	}

	@ParameterizedTest
	@DisplayName("Implementing request to Google Vision")
	@ValueSource(strings = ["{responses: []}"])
	fun request(expected: String) = runBlocking {
		val response = object : GoogleVision(properties) {
			override val request: Request = GoogleObjSeekerTest.RequestMock(properties, expected)
		}.doRequest(path)
		Assert.assertEquals("Invalid response", expected, "{responses: ${response.responses}}")
	}

	@Test
	@DisplayName("Building request to Google Vision")
	fun testBuildRequest() {
		val request: GoogleRequest = object : GoogleVision(properties) {
			fun getRequest(): Any {
				return buildRequest(path)
			}
		}.getRequest() as GoogleRequest

		val image = Image("/9j/4AAQSkZJRgABAQIAdgB2AAD/2wBDAAMCAgICAgMCAgIDAwMDBAYEBAQEBAgGBgUGCQgKCgkICQkKDA8MCgsOCwkJDRENDg8QEBEQCgwSExIQEw8QEBD/wAALCABLAEsBAREA/8QAGwABAAIDAQEAAAAAAAAAAAAAAAgJBgcKBAX/xAA6EAAABQMCAwMICAcAAAAAAAAAAQIEBQMGBxESCAkTFCExGTVBV3SWstQVFjI4WHF2tCM5UWF1d7X/2gAIAQEAAD8AtTAAGn8h8X3DNii6fqTkHM9uRE4k0pqsl1zqVG5n3kVbpkoqJ6GR/wAQ09xkfgY2nCzcNckS0nrelmcnGP6Sa7R4zrprUK9JRapWhaTNKkmXgZHoPaAAAAPgZAd3CwsO5H1o0DrzreIeVYykSd3UdporOinT06rJJaDmAlZCTlpR5KTTtw7kHleo4d13CzXVq1lqNS1rUfeajUZmZn36mYuK5Kc3echgy9IiYqOatuRdwoRDKqmZpp1alEluqVMz8EkZ0l7S7t1VR+KjFigAACNN/cxrhExjecxj+9MmOWM5Au1sn7ZMDIVSpVk/aSS0UTSr8yMyGP8AlVOB31tu/duT+XDyqnA7623fu3J/LiKeVJ3k05avhzkCZu2eiZKQrm5kKUNGSrVu9qqPVS10uzGSVKPvUdPZqZmZ95mZyIxzzDOXJiSzo6wMc3v9BQEUg0NWba2pTanU9VKUo6BqWtRmZqWozUozMzMzGSeVU4HfW27925P5cPKqcDvrbd+7cn8uJI43yLaOWrHiMjWHJqkICcom4YuVUKlE6tMlGkz2VEpWnvSfcZEMlAVuZ55QklmnMl35WpZ7bRCLpla0kliq2lVzbks9dh1O0p3af12l+QwLyG0t+JVp7pq+bEYON3gTd8GTWz3LrJtG7frbUfU0ppxBsuzdmKgep61qm/d1/wC2m30690Vhl2IMfryxlW0MYU5VMYu7JtlDJeqo9YmxuKyafUNG5O/bu103Frp4kLGvIbS34lWnumr5sPIbS34lWnumr5sWMcOeIquBsJ2niGtPJml2yzU0N+lt2cq+tVa93T3K2/b003H4DZAAAq155XmrDftE98LEVQjb/B396/D364hf3lIdJIAAAAq155XmrDftE98LEVQjb/B396/D364hf3lIdJIAAAAq155XmrDftE98LEejl3cDPCznXhih8h5Uxd9N3A6kpBvWefTci23U6Vc0oLp0HCEFoRaaknU/TqIQYOhY23OP2zbehm3Z4+Lys1ZNKO9S+nRpyhIQncozUeiUkWpmZnp3mY6IQAAABVrzyvNWG/aJ74WIkBykvuW2/wD5iV/cqFWuKv5i9uf7gpf9cdCgAAAAh5zD+Cy++MRnYjayLrgYRVqVZJbk5XraVSck3JOzpIV4dBWuuniQ2XwT8PtycMeA4zEt2TUZKyLF89dLcx3U6Kk1qprSRdRKVakR9/cIeWbyqsv21xSxWd3WRbOqxLC+UXStnTN12lTcn3aOmWtLbv293jpr6RZ2AAAAAAAAA//Z")
		val expected = GoogleRequest(listOf(Request(listOf(Feature(1, "LANDMARK_DETECTION"), Feature(2, "WEB_DETECTION")), image)))

		Assert.assertEquals("Invalid encoded image", expected.requests.first().image.toString(), request.requests.first().image.toString())
		Assert.assertEquals("Invalid features", expected.requests.first().features.toString(), request.requests.first().features.toString())
	}

	@Test
	@DisplayName("Building request to Google Vision of an image with invalid path")
	fun buildRequestInvalidPath() {
		try {
			object : GoogleVision(properties) {
				fun getRequest(): Any {
					return buildRequest("/sdfasdfasdf/asdfasdfasd")
				}
			}.getRequest() as GoogleRequest
			fail("Should be thrown exception")
		}
		catch (exception: Exception) {
			Assert.assertTrue("Invalid class of expected exception", exception is IllegalArgumentException)
		}
	}

	@ParameterizedTest
	@DisplayName("Request to Google Vision with invalid API key")
	@CsvSource("google-invalid.properties, {responses: []}")
	fun invalidApiKey(fileName: String, expected: String) {
		val url = javaClass.classLoader.getResource(fileName)
		Assert.assertNotNull("Can't find resource file $fileName", url)
		properties.load(FileInputStream(url.path))

		runBlocking {
			val vision = GoogleVision(properties)
			Assert.assertEquals("Shouldn't be any error string", "", vision.error)
			val response = vision.doRequest(path)
			assertThat("Invalid error message", vision.error, Matchers.startsWith("HTTP request to Google Vision Service has failed"))
			Assert.assertEquals("Invalid response", expected, "{responses: ${response.responses}}")
		}
	}

	@Test
	@DisplayName("Timeout of request")
	fun requestTimeout() = runBlocking {
		val vision = object : GoogleVision(properties) {
			override val request: Request = Request(properties, 1000L)
		}
		val responses = vision.doRequest(path).responses ?: fail("Response is NULL")
		Assert.assertTrue("Time out should return empty results", responses.isEmpty())
		assertThat("Invalid error message", vision.error, Matchers.startsWith("HTTP request to Google Vision Service timed out"))
	}
}