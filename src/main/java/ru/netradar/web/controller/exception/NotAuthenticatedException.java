package ru.netradar.web.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * For not found action
 *
 * Created by rfk on 05.01.2016.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException(String message) {
        super(message);
    }
}
