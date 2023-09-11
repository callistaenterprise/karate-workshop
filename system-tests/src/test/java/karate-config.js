function fn() {
  var DatabaseAccess = Java.type('se.callista.workshop.karate.util.jdbc.DatabaseAccess');
  var config = {
    product_url: 'https://host.docker.internal:8443',
    inventory_url: 'https://host.docker.internal:9443',
    sleep: function(pause) { java.lang.Thread.sleep(pause) },
    product_db: new DatabaseAccess('jdbc:postgresql://host.docker.internal:5432/product', 'admin', 'secret')
  }
  return config;
}