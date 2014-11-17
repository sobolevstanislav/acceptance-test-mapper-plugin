Scenario: old test scenario

Given I have an implemented JBehave scenario
Given I have an implemented JBehave scenario with one 'param'
When user save 'link' for use
Given the scenario with two 'first' and 'second' parameters also works
Then user should see title with: 'titleTable'
Then user will see custom 'customText' text