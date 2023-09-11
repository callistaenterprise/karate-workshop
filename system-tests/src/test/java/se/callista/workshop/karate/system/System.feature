Feature: Product system tests

  Background:
    * configure ssl = read('sslConfig.json')
    * configure afterScenario = read('afterScenario.js')

  Scenario: get non-existing product
    Given url product_url
    And path '/products/non-existing'
    When method get
    Then status 404

  Scenario: get existing product
    * product_db.insertInto('product', {"id":101,"name":"Product 101","sku":"sku101"})
    Given url product_url
    And path '/products/sku101'
    When method get
    Then status 200
    And match response.name == "Product 101"
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

  Scenario: deleteProduct
    * product_db.insertInto('product', {"id":101,"name":"Product 101","sku":"sku101"})
    Given url product_url
    And path '/products/sku101'
    When method delete
    Then status 204
