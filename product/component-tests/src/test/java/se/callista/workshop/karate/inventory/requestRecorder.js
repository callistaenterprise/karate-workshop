function() {
      if (karate.get('requestUri') != '/requests') {
        karate.appendTo(karate.get('recordedRequests'), {
          'uri': karate.get('requestUri'),
          'headers': karate.get('requestHeaders'),
          'params': karate.get('requestParams'),
          'body': karate.get('requestBody')
        });
      }
    }