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
    And match response.name == "Product 1"
    And match response.inventory == '#number'

  Scenario: create product
    Given url product_url
    And path '/products/'
    And request {"name": "a new product", "sku": "new"}
    When method post
    Then status 201
    And match response == read('response/new.json')
    Given url product_url
    And path '/products/new'
    When method get
    Then status 200
    And match response == read('response/new.json')
    * sleep(2000)
    Given url inventory_url
    And path '/inventory/new'
    When method get
    Then status 200
    And match response == {"sku": "new", "stock": 0}
    Given url product_url
    And path '/products/new'
    When method delete
    Then status 204
