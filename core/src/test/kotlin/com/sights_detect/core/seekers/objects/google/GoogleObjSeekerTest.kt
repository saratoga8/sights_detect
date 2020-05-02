package com.sights_detect.core.seekers.objects.google

import com.sights_detect.core.detections.Detections
import com.sights_detect.core.net.Request
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.*


internal class GoogleObjSeekerTest {

	private val picFile = "pic.jpg"
	private var rootPath = ""

	private val properties: Properties = Properties()

	init {
		val fileName = "google.properties"
		val url = javaClass.classLoader.getResource(fileName)
		if (url != null) {
			properties.load(FileInputStream(url.path))
		}
	}

	@BeforeEach
	fun setUp() {
		try {
//			val keyPath: String = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
//			Assertions.assertFalse(TextUtils.isEmpty(keyPath), "No env var GOOGLE_APPLICATION_CREDENTIALS")
//			Assertions.assertTrue(File(keyPath).exists(), "The file with google's account key $keyPath doesn't exist")
			rootPath = createTempDir("tests").absolutePath
		} catch (e: IOException) {
			fail("Cant't create temp dir: " + e.message)
		}
	}

	@AfterEach
	fun tearDown() {
		File(rootPath).walk().forEach { it.deleteRecursively() }
		Assertions.assertFalse(File(rootPath).exists(), "Temp directory $rootPath still exists")
	}

	@ParameterizedTest
	@CsvSource("https://cloud.google.com/vision/images/rushmore.jpg, Mount Rushmore National Memorial, Mount Rushmore")
	fun `Detect landmark with 2 descriptions`(url: String, description1: String, description2: String) {
		try {
			val downloadedFile = downloadPic(url, rootPath, picFile)
			val found = runBlocking { GoogleObjSeeker(downloadedFile.absolutePath, properties).find() }
			Assertions.assertEquals(1, found.size, "There should be only one detection")
			val detection = found[0]
			Assertions.assertEquals(downloadedFile.absolutePath, detection.path, "Invalid path of detection")
			Assertions.assertTrue(description1 in detection.toString(), "Description $description1 hasn't found in $detection")
			Assertions.assertTrue(description2 in detection.toString(), "Description $description2 hasn't found in $detection")
			Assertions.assertEquals(Detections.FOUND, detection.state, "Invalid detection state")
		} catch (e: IOException) {
			fail("Cant't download the file " + url + " to the temp dir: " + e.message)
		}
	}


	@ParameterizedTest
	@DisplayName("No landmarks")
	@ValueSource(strings = ["https://media.istockphoto.com/photos/slice-cucumber-in-squar-white-cup-on-wooden-table-picture-id640908364"])
	fun noLandMarks(url: String) {
		try {
			val downloadedFile = downloadPic(url, rootPath, picFile)
			val found = runBlocking { GoogleObjSeeker(downloadedFile.absolutePath, properties).find() }
			Assertions.assertEquals(1, found.size, "There should be only one detection")
			Assertions.assertEquals("$downloadedFile; []; NO", found[0].toString(), "Image $url HAS landmark")
			Assertions.assertEquals(Detections.NO, found[0].state)
		} catch (e: Exception) {
			fail("Test aborted because of: $e")
		}
	}


	@Test
	@DisplayName("Invalid image path")
	fun invalidImgPath() {
		try {
			val found = GoogleObjSeeker("/sdfasdf/adsfasdf", properties).find()
			Assertions.assertEquals(1, found.size, "There should be only one detection")
			Assertions.assertFalse(found.last().error.isEmpty(), "Should be there error message of detection")
			Assertions.assertEquals(Detections.UNKNOWN, found.last().state, "Detection type should be unknown")
		} catch (e: Exception) {
			fail("Test aborted because of: $e")
		}
	}

	companion object {
		@Throws(IOException::class)
		fun downloadPic(url: String, rootPath: String, picFile: String): File {
			val file = File(rootPath, picFile)
			val readableByteChannel: ReadableByteChannel = Channels.newChannel(URL(url).openStream())
			val fileOutputStream = FileOutputStream(file)
			fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
			Assertions.assertTrue(file.exists() && file.length() > 0, "Can't download the file $url")
			return file
		}
	}

	@Disabled
	@Test
	@DisplayName("Google Vision mocking")
	fun mocking() {
		val url = javaClass.classLoader.getResource("man.jpg")
		Assertions.assertNotNull(url, "Can't find a file with picture in resources")
		runBlocking { GoogleVisionTest(properties).test(url.path).await() }
	}

	class GoogleVisionTest(private val properties: Properties): GoogleVision(properties) {
		suspend fun test(path: String) = CoroutineScope(Dispatchers.IO).async {
			RequestMock(properties, "{responses: []}").post(buildRequest(path))
		}
	}

	class RequestMock(private val properties: Properties, private val responseStr: String) : Request(properties) {
		override fun getClient(requestTimeout: Long, connectTimeout: Long): HttpClient {
			return HttpClient(MockEngine) {
				install(JsonFeature) {
					serializer = GsonSerializer {
						serializeNulls()
						disableHtmlEscaping()
					}
				}
				engine {
					addHandler { request ->
						when (request.url) {
							Url("https://${properties.getProperty("host")}/${properties.getProperty("path")}?key=${properties.getProperty("key")}") -> {
								val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
								respond(responseStr, headers = responseHeaders)
							}
							else -> error("Unhandled ${request.url}")
						}
					}
				}
			}
		}
	}

}
