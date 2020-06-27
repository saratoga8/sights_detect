Feature: Basic operations

  Scenario Outline: Finding all picture files in a directory
	Given there is directory with <num> picture files without landmarks
	When user runs program
	Then program found <num> picture files

	Examples:
	  | num |
	  |   3 |
#	  |   0 |

#  Scenario Outline: Finding picture files with landmarks
#	Given there is directory with <no_landmarks_num> picture files without landmarks
#	And there is directory with <landmarks_num> picture files with landmarks
#	When user runs program
#	Then program found <no_landmarks_num> picture files without landmarks
#	And program found <landmarks_num> picture files with landmarks
#
#	Examples:
#	  | landmarks_num | no_landmarks_num |
#	  |  3            | 10               |