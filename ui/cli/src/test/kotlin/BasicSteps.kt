import com.github.tomakehurst.wiremock.client.WireMock.*
import com.sights_detect.core.statistics.Statistics
import io.cucumber.java8.En
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.*
import kotlin.random.Random

class BasicSteps : En {
	private var tmpDir: File? = null
	private var statistics: Statistics? = null
	private var withLandmarksPicsNum = 0
	private var noLandmarksPicsNum = 0
	private val expectedDescription = "Description"
	private enum class ImgType { WITH_LANDMARK, NO_LANDMARK }
	private val urls = mapOf(ImgType.WITH_LANDMARK to "https://storage.googleapis.com/sights_detect/star.jpg",
			ImgType.NO_LANDMARK to "https://storage.googleapis.com/sights_detect/man.jpg")
	private val fileNames = mapOf(ImgType.WITH_LANDMARK to "with_landmark.jpg", ImgType.NO_LANDMARK to "no_landmark.jpg")
	private val responseFileNames = mapOf(ImgType.WITH_LANDMARK to "landmarks_response.json", ImgType.NO_LANDMARK to "no_landmarks_response.json")

	init {
		Given("^there is directory with (\\d+) picture files (without|with) landmarks$") { num: Int, landmark: String ->
			if (tmpDir == null) {
				tmpDir = createTempDir("tests")
			}
			val imgType = if(landmark == "with") ImgType.WITH_LANDMARK else ImgType.NO_LANDMARK
			val file = download(imgType)
			for (i in 1 until num) {
				var path = "${tmpDir!!.absolutePath}${File.separator}pic${Random.Default.nextInt()}.jpg"
				file.copyTo(File(path)).absolutePath
			}
			file.delete()
			if (landmark == "with") {
				withLandmarksPicsNum = num
			} else {
				noLandmarksPicsNum = num
			}
		}

		When("user runs program") {
			val propertiesPath = getResourceURL("google.properties").path
			val properties = Properties().also { it.load(FileInputStream(propertiesPath)) }

			setStubbing(withLandmarksPicsNum, properties, true)
			setStubbing(noLandmarksPicsNum, properties, false)

			main(arrayOf(tmpDir!!.absolutePath, propertiesPath))
			Assert.assertNotNull("There is no stats, the result of program run", stats)
			statistics = stats
		}

		Then("program found {int} picture files") { num: Int  ->
			Assert.assertNotNull("There is no stats, the result of program run", statistics)
			Assert.assertEquals("Invalid number of found pic files", num, stats!!.getFoundPicsNum())
			Assert.assertTrue("There are errors in program's run: ${stats!!.getErrors()}", stats!!.getErrors().isEmpty())
		}

		Then("program found {int} landmarks") { num: Int ->
			val foundLandmarks = statistics!!.getFoundObjects()
			Assert.assertEquals("Invalid number of found landmarks", num, foundLandmarks.size)
			for(i in 0 until num) {
				Assert.assertTrue("There is no landmark '$expectedDescription' between found ones: $foundLandmarks",
						foundLandmarks.map { it.foundDetection.descriptions.last() }.contains(expectedDescription))
			}
		}
	}

	private fun getResourceURL(fileName: String): URL {
		val url = javaClass.classLoader.getResource(fileName)
		Assert.assertNotNull("Can't find resource file $fileName", url)
		return url
	}

	private fun encodeImg(withLandmarks: Boolean): String {
		val imgType = if(withLandmarks) ImgType.WITH_LANDMARK else ImgType.NO_LANDMARK
		return Base64.getEncoder().encodeToString(download(imgType).readBytes())
	}

	private fun setStubbing(times: Int, properties: Properties, withLandmarks: Boolean) {
		if (times == 0) return
		val path = properties.getProperty("path")
		val key = properties.getProperty("key")

		val dataWithLandmarks = getResourceURL(responseFileNames.getValue(ImgType.WITH_LANDMARK)).readText(Charsets.UTF_8).replace("<description>", expectedDescription)
		val dataNoLandmarks = getResourceURL(responseFileNames.getValue(ImgType.NO_LANDMARK)).readText(Charsets.UTF_8)
		val responseBodyStr = if(withLandmarks) dataWithLandmarks else dataNoLandmarks

		val imgStr = encodeImg(withLandmarks)
		val requestBodyStr = getResourceURL("request.json").readText(Charsets.UTF_8).replace("<content>", imgStr)

		stubFor(post(urlEqualTo("/$path?key=$key"))
				.withHeader("Content-Type", equalTo("application/json"))
				.withRequestBody(equalToJson(requestBodyStr))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(responseBodyStr)))
	}

	private fun download(type: ImgType): File {
		return downloadPic(urls.getValue(type), tmpDir!!.absolutePath, fileNames.getValue(type))
	}

	private fun downloadPic(url: String, rootPath: String, picFile: String): File {
		val file = File(rootPath, picFile)
		val readableByteChannel: ReadableByteChannel = Channels.newChannel(URL(url).openStream())
		val fileOutputStream = FileOutputStream(file)
		fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
		Assert.assertTrue("Can't download the file $url", file.exists() && file.length() > 0)
		return file
	}
}