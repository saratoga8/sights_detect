Feature: Basic operations

  Scenario Outline: Finding all picture files in a directory
	Given there is directory with <num> picture files without landmarks
	When user runs program
	Then program found <num> picture files
	And program found 0 landmarks

	Examples:
	  | num |
	  |   3 |
   	  |   0 |

  Scenario Outline: Detecting landmarks on pictures(in directory only pics with landmarks)
	Given there is directory with <num> picture files with landmarks
	When user runs program
	And program found 3 picture files
	Then program found <num> landmarks

	Examples:
	  | num |
	  |  3  |

  Scenario Outline: Detecting landmarks on pictures(in directory pics with landmarks and without)
	Given there is directory with <num1> picture files with landmarks
	And there is directory with <num2> picture files without landmarks
	When user runs program
	Then program found <num1> landmarks
	And program found <pics> picture files

	Examples:
	  | num1 | num2 | pics |
	  |  2   | 3    | 5    |

  @dev
  Scenario Outline: Multiple run
	Given there is directory with <num1> picture files with landmarks
	And there is directory with <num2> picture files without landmarks
	When user runs program
	And program found <num1> landmarks
	And program found <pics> picture files
	And user runs program
	Then program found <num1> landmarks
	And program found <pics> picture files


	Examples:
	  | num1 | num2 | pics |
	  |  2   | 3    | 5    |
