Feature: Kie Scanner Embedded Adapter

  Scenario Outline: Validate query output
    Given I have the facts: "<facts>" and the query: "<query>" for a Kie Scanner adapter
    When I ask for the output from the Kie Scanner adapter
    Then the Kie Scanner adapter should return status of "<status>" and output of <output>

  Examples:
    | facts | query | status | output |
    |Input 1| Get Output | SUCCESS | 1 |
    |Input 1,Input 2| Get Output | SUCCESS | 2 |
    | Input 1 | XXX | FAILURE | 1 |
