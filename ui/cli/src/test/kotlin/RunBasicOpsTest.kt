import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.runner.RunWith


@RunWith(Cucumber::class)
@CucumberOptions(
		features = ["src/test/kotlin/features/basic.feature"], strict = true, plugin = ["pretty", "html:target/cucumber.html"]
)
class RunBasicOpsTest {

//	companion object {

	companion object {
		@ClassRule
		@JvmField
		val wireMockRule = WireMockRule(5555)

		@BeforeClass
		@JvmStatic
		fun beforeClass() {
			wireMockRule.resetAll()
		}

//		@AfterClass
//		@JvmStatic
//		fun afterClass() {}

	}

}