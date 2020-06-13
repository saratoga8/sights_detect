import com.sights_detect.core.detections.Detection
import com.sights_detect.core.detections.Detections
import io.cucumber.java8.En
import java.io.File

class BasicSteps : En {
	private lateinit var tmpDir: File
	private val detections: MutableList<Detection> = mutableListOf()

	init {
		Given("^there is directory with (\\d+) picture files (without|with) landmarks$") { num: Int, landmark: String ->
			tmpDir = createTempDir("tests")
			for (i in 1 until num) {
				val path: String = createTempFile("pic$i", ".jpg", tmpDir).absolutePath
				if(landmark == "with")
					detections.add(Detection(path, Detections.FOUND, listOf("Landmark$i")))
				if(landmark == "without")
					detections.add(Detection(path))
			}
		}

		When("user runs program") {

			val fileName = "google.properties"
			val url = javaClass.classLoader.getResource(fileName)

			main(arrayOf(tmpDir.absolutePath, "/home/saratoga/progs/SightsDetect/ui/cli/src/test/resources/$fileName"))
		}

		Then("^program found (\\d+) picture files (without|with) landmarks$") { num: Int, landmark: String  ->
		}
	}
}