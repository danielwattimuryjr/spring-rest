package danielwattimury.rest_api.controller;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.model.WebResponse;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<String>> constraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(WebResponse
                        .<String>builder()
                        .status("error")
                        .message(exception.getMessage())
                        .build());
    };

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String>> apiException(ResponseStatusException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(WebResponse
                        .<String>builder()
                        .status("error")
                        .message(exception.getReason())
                        .build());
    }

}
