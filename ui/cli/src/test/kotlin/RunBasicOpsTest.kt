import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.logging.MockServerLogger
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.socket.tls.KeyStoreFactory
import javax.net.ssl.HttpsURLConnection


@RunWith(Cucumber::class)
@CucumberOptions(
		features = ["src/test/kotlin/basic.feature"], strict = true
)
class RunBasicOpsTest {

	companion object {
		@JvmStatic
		private lateinit var mockServer: ClientAndServer

		@BeforeClass
		@JvmStatic
		fun startServer() {
			HttpsURLConnection.setDefaultSSLSocketFactory(KeyStoreFactory(MockServerLogger()).sslContext().socketFactory)
			mockServer = startClientAndServer(5555)
			MockServerClient("localhost", 5555)
					.`when`(
							HttpRequest.request()
									.withMethod("POST")
									.withPath("/v1/images:annotate")
									.withQueryStringParameter("key", "blabla")
//									.withHeaders(Header.header("Content-Type", "application/json"))
									.withBody("{username: 'bla'}")
					)
					.respond(
							HttpResponse.response()
									.withStatusCode(200)
									.withBody("{yes}")
					)

		}

		@AfterClass
		@JvmStatic
		fun stopServer() {
			mockServer.retrieveRecordedRequests(HttpRequest.request()).map {
				println("Path: ${it.path}")
				println("Body: ${it.bodyAsString}")
				println("Headers: ${it.headers}")
			}

			println(mockServer.retrieveLogMessages(HttpRequest.request()))

			mockServer.stop()
		}

	}
}