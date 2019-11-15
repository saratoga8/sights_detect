package com.sights_detect.core.seekers

import org.junit.jupiter.api.*
import java.io.File
import java.io.IOException

internal class PicSeekersFactoryTest {

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

	@DisplayName("No sub-dirs")
	@Test
	fun getPicSeekers1() {
		try {
			createTempFile( "test", ".jpg", File(rootPath))
			createTempFile( "test2", ".jpg", File(rootPath))
			createTempFile( "test3", ".txt", File(rootPath))
			createTempFile("test4", ".dat", File(rootPath))
			Assertions.assertEquals(1, PicSeekersFactory.getPicSeekers(rootPath).size, "The only pic seeker should be build from files of the current dir")
		} catch (e: IOException) {
			AssertionError("Test aborted: ${e.message}")
		}
	}

	@DisplayName("Empty dir")
	@Test
	internal fun getPicSeekers2() {
		Assertions.assertTrue(PicSeekersFactory.getPicSeekers(rootPath).isEmpty(), "No seekers should be build in empty dir")
	}

	@DisplayName("With empty sub dirs")
	@Test
	internal fun getPicSeekers3() {
		try {
			File(rootPath + File.separator + "sub1").mkdir()
			File(rootPath + File.separator + "sub2").mkdir()
			File(rootPath + File.separator + "sub3").mkdir()
			Assertions.assertTrue(PicSeekersFactory.getPicSeekers(rootPath).isEmpty(), "No seeker should be build")
		} catch (e: IOException) {
			AssertionError("Test aborted: ${e.message}")
		}
	}

	@DisplayName("With one non-empty sub dir")
	@Test
	internal fun getPicSeekers4() {
		try {
			File(rootPath + File.separator + "sub1").mkdir()
			File(rootPath + File.separator + "sub2").mkdir()
			File(rootPath + File.separator + "sub3").mkdir()
			createTempFile("test", ".txt", File(rootPath + File.separator + "sub3"))
			Assertions.assertEquals(1, PicSeekersFactory.getPicSeekers(rootPath).size, "Only one seeker should be built")
		} catch (e: IOException) {
			AssertionError("Test aborted: ${e.message}")
		}
	}

	@DisplayName("Current dir has a file and one of sub-dirs has")
	@Test
	internal fun getPicSeekers5() {
		try {
			createTempFile("test1", ".jpg", File(rootPath))
			File(rootPath + File.separator + "sub1").mkdir()
			File(rootPath + File.separator + "sub2").mkdir()
			File(rootPath + File.separator + "sub3").mkdir()
			createTempFile("test", ".txt", File(rootPath + File.separator + "sub3"))
			Assertions.assertEquals(2, PicSeekersFactory.getPicSeekers(rootPath).size)
		} catch (e: IOException) {
			AssertionError("Test aborted: ${e.message}")
		}
	}

	@DisplayName("Given path of a file instead dir")
	@Test
	internal fun getPicSeekers6() {
		try {
			val file = createTempFile( "test", ".jpg", File(rootPath))
			Assertions.assertTrue(file.exists(), "The temp file hasn't created")
			PicSeekersFactory.getPicSeekers(file.absolutePath)
		}
		catch (e: Exception) {
			Assertions.assertTrue(e is IllegalArgumentException, "Invalid thrown exception: $e instead of IllegalArgumentException")
			return
		}
		AssertionError("Should be thrown exception")
	}
}