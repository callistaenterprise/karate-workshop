package se.callista.workshop.karate.product.controller;

import org.springframework.http.HttpStatus;

public class NotUniqueException extends ApiException {

    private static final long serialVersionUID = 1L;

    public NotUniqueException(String msg) {
        super(HttpStatus.BAD_REQUEST, msg);
    }
}
