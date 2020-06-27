import com.github.tomakehurst.wiremock.client.WireMock.*
import com.sights_detect.core.statistics.Statistics
import io.cucumber.java8.En
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.*

class BasicSteps : En {
	private lateinit var tmpDir: File
	private var statistics: Statistics? = null
	private var withLandmarks = true

	init {
		Given("^there is directory with (\\d+) picture files (without|with) landmarks$") { num: Int, landmark: String ->
			tmpDir = createTempDir("tests")
			val file = File("/home/saratoga/progs/SightsDetect/ui/cli/src/test/resources/man.jpg")
			for (i in 0 until num) {
				var path = "${tmpDir.absolutePath}${File.separator}pic$i.jpg"
				file.copyTo(File(path)).absolutePath
			}
			withLandmarks = landmark == "with"
		}

		When("user runs program") {
			val propertiesPath = getResourceURL("google.properties").path
			val properties = Properties().also { it.load(FileInputStream(propertiesPath)) }

			val path = properties.getProperty("path")
			val key = properties.getProperty("key")
			val fileName = if(withLandmarks) "landmarks_response.json" else "no_landmarks_response.json"
			val data: String = getResourceURL(fileName).readText(Charsets.UTF_8)

			stubFor(post(urlEqualTo("/$path?key=$key"))
					.withHeader("Content-Type", equalTo("application/json"))
					.willReturn(aResponse()
							.withHeader("Content-Type", "application/json")
							.withBody(data)))

			main(arrayOf(tmpDir.absolutePath, propertiesPath))
			Assert.assertNotNull("There is no stats, the result of program run", stats)
			statistics = stats
		}

		Then("program found {int} picture files") { num: Int  ->
			Assert.assertNotNull("There is no stats, the result of program run", statistics)
			Assert.assertEquals("Invalid number of found pic files", num, stats!!.getFoundPicsNum())
			Assert.assertTrue("There are errors in program's run: ${stats!!.getErrors()}", stats!!.getErrors().isEmpty())
		}
	}

	private fun getResourceURL(fileName: String): URL {
		var url = javaClass.classLoader.getResource(fileName)
		Assert.assertNotNull("Can't find resource file $fileName", url)
		return url
	}
}