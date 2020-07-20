@dev
Feature: user use invalid data and run the program

  Scenario: Invalid properties file
	Given there is directory with 3 picture files with landmarks
	And there is directory with 2 picture files without landmarks
	And there is invalid properties file
	When user runs program
	Then program found 5 picture files
	And program found 0 landmarks
	And user gets 1 error

