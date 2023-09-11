Feature: Product contract tests

  Background:
    * url product_url
    * configure ssl = read('sslConfig.json')

  @performance
  Scenario: get non-existing product
    Given path '/products/non-existing'
    When method get
    Then status 404

  @performance
  Scenario: get existing product
    Given path '/products/sku1'
    When method get
    Then status 200
    And match response.name == "Product 1"
    And match response.inventory == '#number'

  Scenario: create product
    Given path '/products/'
    And request {"name": "a new product", "sku": "new"}
    When method post
    Then status 201
    And match response == read('response/new.json')
    Given path '/products/new'
    When method get
    Then status 200
    And match response == read('response/new.json')
    Given json message = replenish_queue.waitForMessage()
    Then match message == {"sku":"new","stock":0}
    And assert replenish_queue.size() == 1

  Scenario: deleteProduct
    * product_db.insertInto('product', {"id":101,"name":"Product 101","sku":"sku101"})
    Given path '/products/sku101'
    When method delete
    Then status 204
    Given json message = replenish_queue.waitForMessage()
    Then match message == {"sku":"sku101","stock":-1}
    And assert replenish_queue.size() == 1
