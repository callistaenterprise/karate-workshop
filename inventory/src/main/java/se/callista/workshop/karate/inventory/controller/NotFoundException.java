package se.callista.workshop.karate.inventory.controller;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String msg) {
        super(HttpStatus.NOT_FOUND, msg);
    }
}
