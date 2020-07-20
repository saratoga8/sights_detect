@wip
Feature: User runs program and there are connection problems to Visual Detection service

  Scenario: Slow connection to Visual Detection service
	Given there is directory with 3 picture files with landmarks
	And there is directory with 2 picture files without landmarks
	And there is slow connection to Visual Detection service
	When user runs program
	Then program found 5 pictures
	And program found 0 landmarks
	And user gets 1 error

  Scenario: Invalid API key for connection to Visual Detection service
	Given there is directory with 3 picture files with landmarks
	And there is directory with 2 picture files without landmarks
	And there is invalid API key for connection to Visual Detection service
	When user runs program
	Then program found 5 pictures
	And program found 0 landmarks
	And user gets 1 error
