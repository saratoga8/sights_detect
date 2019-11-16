package com.sights_detect.core.controllers


import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.detections.DetectionsStorage
import org.junit.jupiter.api.*
import java.io.File
import java.io.IOException


internal class DesktopControllerTest {

	private var rootPath = ""

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

	@DisplayName("No sub-dirs")
	@Test
	internal fun noSubDirs() {
		try {
			createTempFile( "test", ".jpg", File(rootPath))
			createTempFile( "test2", ".jpg", File(rootPath))
			createTempFile( "test3", ".txt", File(rootPath))
			createTempFile("test4", ".dat", File(rootPath))
			class TestController: DesktopController(listOf(rootPath)) {
				public fun test() {
					val detections = findNewPics()
					detections.forEach { detection -> Assertions.assertTrue(detection.path.endsWith(".jpg"), "Found detection hasn't extension of pic in path: ${detection.path}") }
					detections.forEach { detection -> Assertions.assertTrue(detection.descriptions.isEmpty(), "Found detection shouldn't have a description") }
					detections.forEach { detection -> Assertions.assertTrue(detection.state == Detections.UNKNOWN, "Found detection should have state UNKNOWN") }
				}
			}
			TestController().test()
		} catch (e: IOException) {
			fail("The test aborted: $e")
		}
	}

	@DisplayName("1 sub-dir")
	@Test
	internal fun subDir() {
		try {
			val subDir = createTempDir(rootPath + File.separator + "dir1")
			Assertions.assertTrue(subDir.exists(), "Temp dir hasn't created")
			createTempFile( "test", ".jpg", subDir)
			createTempFile( "test2", ".jpg", subDir)
			createTempFile( "test3", ".txt", subDir)
			createTempFile("test4", ".dat", subDir)
			class TestController: DesktopController(listOf(rootPath)) {
				public fun test() {
					val detections = findNewPics()
					detections.forEach { detection -> Assertions.assertTrue(detection.path.startsWith(subDir.absolutePath), "Found detection path hasn't sub-dir's path: ${detection.path}") }
					detections.forEach { detection -> Assertions.assertTrue(detection.path.endsWith(".jpg"), "Found detection hasn't extension of pic in path: ${detection.path}") }
					detections.forEach { detection -> Assertions.assertTrue(detection.descriptions.isEmpty(), "Found detection shouldn't have a description") }
					detections.forEach { detection -> Assertions.assertTrue(detection.state == Detections.UNKNOWN, "Found detection should have state UNKNOWN") }
				}
			}
			TestController().test()
		} catch (e: IOException) {
			fail("The test aborted: $e")
		}
	}

	@DisplayName("2 sub-dir and one empty")
	@Test
	internal fun subDirs() {
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

			class TestController: DesktopController(subDirs.asList()) {
				public fun test() {
					val detections = findNewPics()
					Assertions.assertEquals(4, detections.size, "Found detections number invalid")
					detections.forEach { detection -> Assertions.assertTrue(detection.path.endsWith(".jpg"), "Found detection hasn't extension of pic in path: ${detection.path}") }
					detections.forEach { detection -> Assertions.assertTrue(detection.descriptions.isEmpty(), "Found detection shouldn't have a description") }
					detections.forEach { detection -> Assertions.assertTrue(detection.state == Detections.UNKNOWN, "Found detection should have state UNKNOWN") }
				}
			}
			TestController().test()
		} catch (e: IOException) {
			fail("The test aborted: $e")
		}
	}

	@DisplayName("Save detections")
	@Test
	internal fun saveDetections() {
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

			class TestController : DesktopController(subDirs.asList()) {
				override val storage = DetectionsStorage<Map<String, Detection>>(rootPath + File.separator + "detections.json")
				public fun test() {
					start()
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
			TestController().test()
		} catch (e: Exception) {
			fail("Test aborted: ${e.toString()}")
		}
	}

	@DisplayName("Try to load detections from invalid path")
	@Test
	internal fun loadDetectionsInvalidPath() {
		try {
			class TestController : DesktopController(listOf()) {
				override val storage = DetectionsStorage<Map<String, Detection>>(rootPath + File.separator + "bla-bla.json")
				public fun test() {
					loadDetections()
					Assertions.assertTrue(detections.isEmpty(), "Shouldn't be any detections")
				}
			}
		} catch (e: Exception) {
			fail("Test aborted: ${e.toString()}")
		}
	}
}