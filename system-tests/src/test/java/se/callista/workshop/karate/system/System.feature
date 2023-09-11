Feature: Product system tests

  Background:
    * configure ssl = read('sslConfig.json')

  Scenario: get non-existing product
    Given url product_url
    And path '/products/non-existing'
    When method get
    Then status 404

  Scenario: get existing product
    Given url product_url
    And path '/products/sku1'
    When method get
    Then status 200
    And match response == {"productId":1,"name":"Product 1","sku":"sku1","inventory":1}

  Scenario: create product
    Given url product_url
    And path '/products/'
    And request {"name": "a new product", "sku": "new"}
    #TODO
