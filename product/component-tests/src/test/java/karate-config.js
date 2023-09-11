function fn() {
  var env = karate.env; // get system property 'karate.env'
  if (!env) {
    env = 'dev';
  }
  var host = java.lang.System.getProperty('SYSTEM_UNDER_TEST_HOST', 'host.docker.internal');
  var port = java.lang.System.getProperty('SYSTEM_UNDER_TEST_PORT', '8443');
  var mock_host = java.lang.System.getProperty('KARATE_HOST', 'host.docker.internal');
  var mock_port =  java.lang.System.getProperty('KARATE_PORT', '9443');
  var postgresql_host = java.lang.System.getProperty('POSTGRESQL_HOST', 'host.docker.internal');
  var postgresql_port = java.lang.System.getProperty('POSTGRESQL_PORT', '5432');
  var postgresql_username = java.lang.System.getProperty('POSTGRESQL_USERNAME', 'admin');
  var postgresql_password = java.lang.System.getProperty('POSTGRESQL_PASSWORD', 'secret');
  var DatabaseAccess = Java.type('se.callista.workshop.karate.util.jdbc.DatabaseAccess');
  var brokerUrl = java.lang.System.getProperty('ACTIVEMQ_URL', 'tcp://host.docker.internal:61616');
  var QueueListener = Java.type('se.callista.workshop.karate.util.jms.QueueListener');
  var config = {
    product_url: 'https://' + host + ':' + port,
    inventory_mock_url: 'https://' + mock_host + ':' + mock_port,
    product_db: new DatabaseAccess('jdbc:postgresql://' + postgresql_host + ':' + postgresql_port + '/product', postgresql_username, postgresql_password),
    replenish_queue: new QueueListener(brokerUrl, 'replenish'),
    getMockRequests: read('classpath:/se/callista/workshop/karate/util/mock/getRequests.js'),
    resetMock: read('classpath:/se/callista/workshop/karate/util/mock/reset.js')
  }
  if (env == 'dev') {
    karate.configure('afterScenario', read('afterScenario.js'))
  }
  return config;
}