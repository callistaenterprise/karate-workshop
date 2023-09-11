@ignore @report=false
Feature: Mock reset feature to be used in an 'afterScenario' hook

  Background:
    * configure ssl = { keyStore: 'classpath:/certs/client.jks', keyStorePassword: 'secret', keyStoreType: 'jks' }

  Scenario:
    # Reset mock requests
    Given url mockUrl + '/requests'
    And method delete
