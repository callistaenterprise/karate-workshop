Feature: Inventory Mock

  Background:
    * configure ssl = read('trustStoreConfig.json')
    # Recorded requests
    * def recordedRequests = []
    # Hook executed after all regular requests to record the request
    * configure afterScenario = read('requestRecorder.js')

  # Get recorded requests
  Scenario: requestUri == '/requests' && methodIs('get')
    * def response = recordedRequests

  # Reset recorded requests
  Scenario: requestUri == '/requests' && methodIs('delete')
    * karate.set('recordedRequests', [])

  Scenario: pathMatches('/inventory/{sku}')
    * string requestBody = (request)
    * def sku = pathParams.sku
    * def response = read('response/inventory.json')

  # Catch-all
  Scenario:
    * string requestBody = (request)
    * print 'No dedicated scenario matches incoming request.'
    * print 'With Uri:'
    * print requestUri
    * print 'With Headers:'
    * print requestHeaders
    * print 'With Request Parameters'
    * print requestParams
    * print 'And Request:'
    * print request
    * def responseStatus = 404
    