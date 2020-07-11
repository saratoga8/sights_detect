package com.sights_detect.core.controllers


import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import com.sights_detect.core.detections.DetectionsStorage
import com.sights_detect.core.seekers.Seeker
import com.sights_detect.core.seekers.objects.google.GoogleObjSeekerTest
import com.sights_detect.core.seekers.pics.DesktopFS
import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
//			properties.load(FileInputStream("/home/saratoga/progs/SightsDetect/core/src/test/resources/google.properties"))
		}
	}

	@BeforeEach
	fun setUp() {
		try {
			if (File(DetectionsStorage.DEFAULT_FILE_NAME).exists())
				File(DetectionsStorage.DEFAULT_FILE_NAME).delete()

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
						val detections = findNewPics().awaitAll().flatten()

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
							val detections = findNewPics().awaitAll().flatten()
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
				fun test() {
					val detections = runBlocking {
						findNewPics().awaitAll().flatten()
					}
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
			class TestController : DesktopController(listOf(rootPath), properties) {
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

		class TestController: DesktopController(listOf(rootPath), properties) {
			suspend fun testFindObjs(detections: List<Detection>) {
				super.findObjects(detections)
			}

			override fun buildObjSeekers(paths: List<String>): Set<Seeker<Detection>> {
				class TestSeeker(private val detectionPath: String) : Seeker<Detection> {
					override fun find(): List<Detection> {
						runBlocking { delay(1000) }
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

	@Test
	@DisplayName("Stopping of pictures find")
	fun stopFindPics() {
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

			class TestObjSeeker(dirPath: String, recursive: Boolean = true) : DesktopFS(dirPath, recursive) {
				override fun getAllFiles(): List<String> {
					runBlocking { delay(3000) }
					return listOf()
				}
			}

			val controller = object : DesktopController(subDirs.asList(), properties) {
				fun startTest() {
					Assertions.assertTrue(seekers.isEmpty(), "Before starting shouldn't be seekers")
					findNewPics()
					Assertions.assertEquals(2, seekers.size, "After start should be added new seekers")
				}

				override fun buildPicSeekers(): Set<Seeker<Detection>> {
					return subDirs.map { TestObjSeeker(it) }.toSet()
				}

				fun checkSeekersStopped(state: Boolean) {
					var msg = "Every seeker shouldn't be "
					msg += if(state) "running" else  "stopped"
					seekers.forEach { Assertions.assertEquals(state, it.isStopped(), msg) }
				}
			}

			runBlocking {
				controller.startTest()
				delay(1500)
				controller.checkSeekersStopped(false)
				controller.stop()
				delay(300)
				controller.checkSeekersStopped(true)
			}
		} catch (e: IOException) {
			fail("The test aborted: $e")
		}
	}

	@Test
	@DisplayName("Stopping of objects find")
	fun stopDetectsObjs() {
		val url = javaClass.classLoader.getResource("man.jpg")
		Assertions.assertNotNull(url, "Can't find a file with picture in resources")
		val path = url.path

		val picsNum = 100
		val detections = List(picsNum) { Detection(rootPath + File.separator + "pic$it.jpg") }
		detections.forEach { detection -> File(path).copyTo(File(detection.path)) }
		detections.forEach { detection -> Assertions.assertTrue(File(detection.path).exists(), "File ${detection.path} doesn't exist") }

		class TestController: DesktopController(listOf(rootPath), properties) {
			suspend fun run() {
				val foundDetections: Hashtable<String, Detection> = Hashtable()
				detections.forEach { foundDetections[it.path] = it }
				super.detections = foundDetections
				detectObjects()
			}

			fun chkSeekersStarted() {
				Assertions.assertEquals(picsNum, seekers.size, "Invalid number of seekers after start")
				Assertions.assertTrue(seekers.count { it.isStopped() } < picsNum, "There is no started seekers")
			}

			fun chkNoSeekers() {
				Assertions.assertEquals(0, seekers.size, "Invalid number of seekers after stop")
			}
		}
		runBlocking {
			val test = TestController()
			test.run()
			sleep(500)
			test.chkSeekersStarted()
			test.stop()
			sleep(100)
			test.chkNoSeekers()
		}
	}

	@Test
	@DisplayName("Found pics statistics")
	fun testPicsStat() {
		val picsNum = 4
		try {
			createTempFile("test", ".jpg", File(rootPath))
			createTempFile("test2", ".jpg", File(rootPath))
			createTempFile("test3", ".txt", File(rootPath))
			createTempFile("test4", ".dat", File(rootPath))
			createTempFile("test5", ".jpeg", File(rootPath))
			createTempFile("test6", ".png", File(rootPath))

			val controller = object : DesktopController(listOf(rootPath), properties) {
				suspend fun test() {
					detectNewPics()
				}
			}
			var statistics = controller.getStatistics()
			Assertions.assertEquals(0, statistics.getFoundPicsNum(), "Before start there shouldn't be any found pics")
			Assertions.assertEquals(0, statistics.getFoundObjects().size, "Before start there shouldn't be any found objects")
			Assertions.assertEquals(0, statistics.getErrors().size, "Before start there shouldn't be any errors")
			runBlocking {
				controller.test()
				while (controller.detections.size < picsNum) sleep(1000)
				statistics = controller.getStatistics()
				Assertions.assertEquals(picsNum, statistics.getFoundPicsNum(), "Invalid found pics statistics")
				Assertions.assertEquals(0, statistics.getFoundObjects().size, "Invalid found objects statistics")
				Assertions.assertEquals(0, statistics.getErrors().size, "Invalid errors statistics")
			}
		} catch (e: Exception) {
			fail("The test aborted: $e")
		}
	}

	@Test
	@DisplayName("Errors statistics")
	fun errsStat() {
		try {
			val picsNum = 4
			val errsNum = 2
			createTempFile("test", ".jpg", File(rootPath))
			createTempFile("test2", ".jpg", File(rootPath))
			createTempFile("test3", ".txt", File(rootPath))
			createTempFile("test4", ".dat", File(rootPath))
			createTempFile("test5", ".jpeg", File(rootPath))
			createTempFile("test6", ".png", File(rootPath))

			val controller = object : DesktopController(listOf(rootPath), properties) {
				suspend fun test() {
					detectNewPics()
					detections["bla1"] = Detection("bla-bla1").also { it.error = "Error1" }
					detections["bla2"] = Detection("bla-bla2").also { it.error = "Error2" }
				}
			}
			runBlocking {
				controller.test()
				while (controller.detections.size != picsNum + errsNum) sleep(1000)
				val statistics = controller.getStatistics()
				Assertions.assertEquals(errsNum, statistics.getErrors().size, "Invalid errors number")
				Assertions.assertEquals("[File: bla-bla2; Error: Error2, File: bla-bla1; Error: Error1]", statistics.getErrors().toString(), "Invalid errors texts")
			}
		} catch (e: Exception) {
			fail("The test aborted: $e")
		}
	}

	@ParameterizedTest
	@DisplayName("Starting of controller")
	@CsvSource("https://storage.googleapis.com/sights_detect/eiffel.jpg, Eiffel Tower")
	fun startTest(url: String, description: String) {
		try {
			val downloadedFile = GoogleObjSeekerTest.downloadPic(url, rootPath, "pic.jpg")
			val filesNum = 10
			val paths = List(filesNum - 1) { rootPath + File.separator + "pic$it.jpg" }
			paths.forEach { downloadedFile.copyTo(File(it)) }
			paths.forEach { Assertions.assertTrue(File(it).exists(), "File $it doesn't exist") }
			val controller = DesktopController(listOf(rootPath), properties)
			GlobalScope.launch { controller.start() }
			for (i in 1..filesNum) {
				if (controller.detections.size == filesNum)
					if (controller.detections.values.all { it.state == Detections.FOUND }) {
						Assertions.assertEquals(filesNum, controller.getStatistics().getFoundObjects().size, "Invalid number of found objects")
						Assertions.assertEquals(filesNum, controller.getStatistics().getFoundPicsNum(), "Invalid number of found pictures")
						Assertions.assertEquals(0, controller.getStatistics().getErrors().size, "There are errors")
						controller.detections.forEach { Assertions.assertEquals(Detections.FOUND, it.value.state, "There shouldn't be any detected object") }
						return
					}
				sleep(2000)
			}
			fail( "Detection process hasn't finished")
		} catch (e: Exception) {
			fail("The test aborted: " + e.message)
		}
	}
}