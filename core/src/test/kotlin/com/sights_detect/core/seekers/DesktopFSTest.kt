package com.sights_detect.core.seekers

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import java.io.File
import java.io.IOException


internal class DesktopFSTest {

	private var rootPath: String = ""

	@BeforeEach
	fun setUp() {
		try {
			rootPath = createTempDir("tests").absolutePath
		}
		catch (e: IOException) {
			AssertionError("Can't create temp directory tests: $e")
		}
	}

	@AfterEach
	fun tearDown() {
		File(rootPath).walk().forEach { it.deleteRecursively() }
		Assertions.assertFalse(File(rootPath).exists(), "Temp directory $rootPath still exists")
	}


	@DisplayName("Find pics in an empty dir")
	@Test
	internal fun find1() {
		Assertions.assertTrue(DesktopFS(rootPath).find().isEmpty(), "In an empty directory shouldn't be pics files")
	}

	@Test
	@DisplayName("Find pics in current dir")
	internal fun find2() {
		try {
			createTempFile( "test", ".jpg", File(rootPath))
			createTempFile( "test2", ".jpg", File(rootPath))
			createTempFile( "test3", ".txt", File(rootPath))
			createTempFile("test4", ".dat", File(rootPath))
			Assertions.assertEquals(2, DesktopFS(rootPath).find().size, "Should find the only pic file")
		} catch (e: IOException) {
			AssertionError("Cant't create temp file in dir " + rootPath + ": " + e.message)
		}
	}

	@Test
	@DisplayName("Find pics in sub dirs")
	internal fun find3() {
		try {
			val subPath: String = rootPath + File.separator + "sub"
			Assertions.assertFalse(File(subPath).exists(), "Sub directory shouldn't exist before creating")
			File(subPath).mkdir()
			Assertions.assertTrue(File(subPath).exists(), "Sub dir hasn't created")
			createTempFile( "test", ".jpg", File(subPath))
			createTempFile( "test2", ".txt", File(subPath))
			createTempFile("test3", ".jpg", File(subPath))
			Assertions.assertEquals(2, DesktopFS(rootPath).find().size, "Should find the only pic file")
		} catch (e: IOException) {
			AssertionError("Cant't create temp file in dir " + rootPath + ": " + e.message)
		}
	}

	@DisplayName("Invalid path")
	@Test
	internal fun find4() {
		try {
			val subPath: String = rootPath + File.separator + "bla-bla"
			Assertions.assertFalse(File(subPath).exists(), "Sub directory shouldn't exist before creating")
			Assertions.assertTrue(DesktopFS(subPath).find().isEmpty(), "In an empty directory shouldn't be pics files")
		}
		catch (e: Exception) {
			Assertions.assertTrue(e is IllegalArgumentException, "Invalid thrown exception: $e instead of IllegalArgumentException")
			return
		}
		AssertionError("Should be thrown exception")
	}

	@DisplayName("Given path of a file instead dir")
	@Test
	internal fun find5() {
		try {
			val file = createTempFile( "test", ".jpg", File(rootPath))
			Assertions.assertTrue(file.exists(), "The temp file hasn't created")
			DesktopFS(file.absolutePath).find()
		}
		catch (e: Exception) {
			Assertions.assertTrue(e is IllegalArgumentException, "Invalid thrown exception: $e instead of IllegalArgumentException")
			return
		}
		AssertionError("Should be thrown exception")
	}

	private suspend fun foo(str: String) = CoroutineScope(Dispatchers.IO).async {
		delay(3000L)
		println(str)
	}

	@Test
	internal fun checkCoroutine() {
		GlobalScope.launch {
			foo("one").await()
			 foo("two")
			 foo("three")
		}
		println("here")
		Thread.sleep(7000L)
	}
}