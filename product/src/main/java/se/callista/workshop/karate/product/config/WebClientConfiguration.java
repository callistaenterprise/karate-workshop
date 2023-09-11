package se.callista.workshop.karate.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class WebClientConfiguration implements WebClientCustomizer {

    @Autowired WebClientSsl ssl;

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        webClientBuilder.apply(ssl.fromBundle("client"));
    }
}
