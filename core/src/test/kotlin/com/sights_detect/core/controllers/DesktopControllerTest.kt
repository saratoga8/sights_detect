package com.sights_detect.core.controllers


import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.detections.DetectionsStorage
import com.sights_detect.core.seekers.Seeker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Thread.sleep
import java.util.*


class DesktopControllerTest {

	private var rootPath = ""

	companion object {
		val properties = Properties()
		@BeforeAll
		@JvmStatic
		internal fun before() {
			val fileName = "google.properties"
			val url = javaClass.classLoader.getResource(fileName)
			Assertions.assertNotNull(url, "Can't find resource file $fileName")
			properties.load(FileInputStream(url.path))
		}
	}

	@BeforeEach
	fun setUp() {
		try {
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

	@Test
	@DisplayName("No sub-dirs")
	fun noSubDirs() {
		try {
			createTempFile("test", ".jpg", File(rootPath))
			createTempFile("test2", ".jpg", File(rootPath))
			createTempFile("test3", ".txt", File(rootPath))
			createTempFile("test4", ".dat", File(rootPath))

			class TestController : DesktopController(listOf(rootPath), properties) {
				suspend fun test() {
						val detections = findNewPics()

						detections.forEach { detection -> Assertions.assertTrue(detection.path.endsWith(".jpg"), "Found detection hasn't extension of pic in path: ${detection.path}") }
						detections.forEach { detection -> Assertions.assertTrue(detection.descriptions.isEmpty(), "Found detection shouldn't have a description") }
						detections.forEach { detection -> Assertions.assertTrue(detection.state == Detections.UNKNOWN, "Found detection should have state UNKNOWN") }
				}
			}
			runBlocking { TestController().test() }
		} catch (e: Exception) {
			fail("The test aborted: $e")
		}
	}

	@Test
	@DisplayName("1 sub-dir")
	fun subDir() {
		try {
				val subDir = createTempDir(rootPath + File.separator + "dir1")
				Assertions.assertTrue(subDir.exists(), "Temp dir hasn't created")
				createTempFile("test", ".jpg", subDir)
				createTempFile("test2", ".jpg", subDir)
				createTempFile("test3", ".txt", subDir)
				createTempFile("test4", ".dat", subDir)
				class TestController : DesktopController(listOf(rootPath), properties) {
					fun test() {
						GlobalScope.launch {
							val detections = findNewPics()
							detections.forEach { detection -> Assertions.assertTrue(detection.path.startsWith(subDir.absolutePath), "Found detection path hasn't sub-dir's path: ${detection.path}") }
							detections.forEach { detection -> Assertions.assertTrue(detection.path.endsWith(".jpg"), "Found detection hasn't extension of pic in path: ${detection.path}") }
							detections.forEach { detection -> Assertions.assertTrue(detection.descriptions.isEmpty(), "Found detection shouldn't have a description") }
							detections.forEach { detection -> Assertions.assertTrue(detection.state == Detections.UNKNOWN, "Found detection should have state UNKNOWN") }
						}
					}
				}
			runBlocking { TestController().test() }
		} catch (e: IOException) {
			fail("The test aborted: $e")
		}
	}

	@Test
	@DisplayName("2 sub-dir and one empty")
	fun multipleSubDirs() {
		try {
			val subDirs = arrayOf(rootPath + File.separator + "dir1", rootPath + File.separator + "dir2")
			subDirs.forEach { path ->
				File(path).mkdir()
				createTempFile("test", ".jpg", File(path))
				createTempFile("test2", ".jpg", File(path))
				createTempFile("test3", ".txt", File(path))
				createTempFile("test4", ".dat", File(path))
			}
			Assertions.assertTrue(createTempDir(rootPath + File.separator + "empty").exists(), "Empty temp dir hasn't created")

			class TestController : DesktopController(subDirs.asList(), properties) {
				suspend fun test() {
					val detections = findNewPics()
					Assertions.assertEquals(4, detections.size, "Found detections number invalid")
					detections.forEach { detection -> Assertions.assertTrue(detection.path.endsWith(".jpg"), "Found detection hasn't extension of pic in path: ${detection.path}") }
					detections.forEach { detection -> Assertions.assertTrue(detection.descriptions.isEmpty(), "Found detection shouldn't have a description") }
					detections.forEach { detection -> Assertions.assertTrue(detection.state == Detections.UNKNOWN, "Found detection should have state UNKNOWN") }
				}
			}
			runBlocking { TestController().test() }
		} catch (e: IOException) {
			fail("The test aborted: $e")
		}
	}

	@Test
	@DisplayName("Save detections")
	fun saveDetections() {
		try {
			val subDirs = arrayOf(rootPath + File.separator + "dir1", rootPath + File.separator + "dir2")
			subDirs.forEach { path ->
				File(path).mkdir()
				createTempFile("test", ".jpg", File(path))
				createTempFile("test2", ".jpg", File(path))
				createTempFile("test3", ".txt", File(path))
				createTempFile("test4", ".dat", File(path))
			}
			Assertions.assertTrue(createTempDir(rootPath + File.separator + "empty").exists(), "Empty temp dir hasn't created")

			class TestController : DesktopController(subDirs.asList(), properties) {
				override val storage = DetectionsStorage<Hashtable<String, Detection>>(rootPath + File.separator + "detections.json")
				suspend fun test() {
					findNewPics()
					val expected = detections
					saveDetections()
					loadDetections()
					Assertions.assertEquals(expected.size, detections.size, "Invalid number of detections")
					expected.keys.forEach { key ->
						Assertions.assertEquals(expected[key]?.path, detections[key]?.path, "Invalid path of detection")
						Assertions.assertEquals(expected[key]?.state, detections[key]?.state, "Invalid state of detection")
						Assertions.assertEquals(expected[key]?.descriptions, detections[key]?.descriptions, "Invalid descriptions of detection")
					}
				}
			}
			runBlocking { TestController().test() }
		} catch (e: Exception) {
			fail("Test aborted: $e")
		}
	}

	@Test
	@DisplayName("Try to load detections from invalid path")
	fun invalidPath() {
		try {
			class TestController : DesktopController(listOf(), properties) {
				override val storage = DetectionsStorage<Hashtable<String, Detection>>(rootPath + File.separator + "bla-bla.json")
				fun test() {
					loadDetections()
					Assertions.assertTrue(detections.isEmpty, "Shouldn't be any detections")
				}
			}
			TestController().test()
		} catch (e: Exception) {
			fail("Test aborted: $e")
		}
	}


	@Test
	@DisplayName("Parallel run of detection objects in pictures")
	fun detectObjs() {
		val url = javaClass.classLoader.getResource("man.jpg")
		Assertions.assertNotNull(url, "Can't find a file with picture in resources")
		val path = url.path

		val detections = List(100) { Detection(rootPath + File.separator + "pic$it.jpg") }
		detections.forEach { detection -> File(path).copyTo(File(detection.path)) }
		detections.forEach { detection -> Assertions.assertTrue(File(detection.path).exists(), "File ${detection.path} doesn't exist") }

		class TestController: DesktopController(listOf(), properties) {
			suspend fun testFindObjs(detections: List<Detection>) {
				super.findObjects(detections)
			}

			override suspend fun buildObjSeekers(paths: List<String>): Set<Seeker<Detection>> {
				class TestSeeker(private val detectionPath: String) : Seeker<Detection> {
					override fun find(): List<Detection> {
						sleep(1000)
						return listOf(Detection(detectionPath))
					}
				}
				return paths.map { path -> TestSeeker(path) }.toSet()
			}
		}
		runBlocking {
			val startTime = System.currentTimeMillis()
			TestController().testFindObjs(detections)
			val stopTime = System.currentTimeMillis()
			assertThat(stopTime - startTime, Matchers.lessThan(3000L))
		}
	}
}