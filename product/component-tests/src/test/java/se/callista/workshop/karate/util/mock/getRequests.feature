@ignore @report=false
Feature: Get mock requests

  Background:
    * configure ssl = { keyStore: 'classpath:/certs/client.jks', keyStorePassword: 'secret', keyStoreType: 'jks' }

  Scenario:
    # Get mock requests
    Given url mockUrl + '/requests'
    When method get
