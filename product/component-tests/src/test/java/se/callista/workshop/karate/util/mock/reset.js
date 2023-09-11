function(mockUrl) {
      return karate.call('classpath:/se/callista/workshop/karate/util/mock/reset.feature', {'mockUrl': mockUrl}).response;
    }
