import io.cucumber.java8.En

class BasicSteps : En {
	init {
		Given("^there is directory with (\\d+) picture files (without|with) landmarks$") { num: Int, landmark: String ->
			print("Pics num $num $landmark")
		}
		When("user runs program") {

		}
		Then("^program found (\\d+) picture files (without|with) landmarks$") { num: Int, landmark: String  ->
			print("Pics num $num $landmark")
		}
	}
}