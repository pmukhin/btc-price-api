Feature: Download and parse rates file feature
    In order to produce a rate message
    File with rates should be downloaded
    Parsed
    Converted to message stream

    Scenario:
        Given I submit the spark app with url http://fakeapi/rates
        Then I wait 5 seconds
        Then I expect 20 messages in kafka
        And messages should be like valid.json