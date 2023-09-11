function(mockUrl) {
      return karate.call('classpath:/se/callista/workshop/karate/util/mock/getRequests.feature', {'mockUrl': mockUrl}).response;
    }
