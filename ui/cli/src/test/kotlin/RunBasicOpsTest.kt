import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.sights_detect.core.detections.DetectionsStorage
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.runner.RunWith
import java.io.File


@RunWith(Cucumber::class)
@CucumberOptions(
		features = ["src/test/kotlin/basic.feature"], strict = true, plugin = ["pretty", "html:target/cucumber.html"], tags = "@dev"
)
class RunBasicOpsTest {

//	companion object {

	companion object {
		@ClassRule
		@JvmField
		val wireMockRule = WireMockRule(5555)

		@BeforeClass
		@JvmStatic
		fun startServer() {
			wireMockRule.resetAll()

			if (File(DetectionsStorage.DEFAULT_FILE_NAME).exists())
				File(DetectionsStorage.DEFAULT_FILE_NAME).delete()
		}

		@AfterClass
		@JvmStatic
		fun stopServer() {
			if (File(DetectionsStorage.DEFAULT_FILE_NAME).exists())
				File(DetectionsStorage.DEFAULT_FILE_NAME).delete()
		}
	}
}