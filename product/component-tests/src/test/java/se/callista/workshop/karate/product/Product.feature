Feature: Product contract tests

  Background:
    * url product_url
    * configure ssl = read('sslConfig.json')

  Scenario: get non-existing product
    Given path '/products/non-existing'
    When method get
    Then status 404

  Scenario: get existing product
    * product_db.insertInto('product', {"id":101,"name":"Product 101","sku":"sku101"})
    Given path '/products/sku101'
    When method get
    Then status 200
    And match response.name == "Product 101"
    And match response.inventory == '#number'
    # TODO verify there is a single request to the inventory mock
    # with the correct uri

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
    # TODO verify there is a single message on the replenish queue,
    # with the correct sku and stock = -1

  Scenario: deleteProduct
    * product_db.insertInto('product', {"id":101,"name":"Product 101","sku":"sku101"})
    Given path '/products/sku101'
    When method delete
    Then status 204
    # TODO verify there is a single message on the replenish queue,
    # with the correct sku and stock = -1
