package com.baskettecase.plumchat.host.error;

import java.time.Instant;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;

@ControllerAdvice
@Order(1)
public class GlobalErrorHandler {

    public record ErrorResponse(String error, String message, int status, String path, Instant timestamp) {}

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleBadInput(ServerWebInputException ex) {
        ErrorResponse body = new ErrorResponse(
                "Bad Request",
                ex.getReason(),
                HttpStatus.BAD_REQUEST.value(),
                null,
                Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                "Internal Server Error",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null,
                Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}


