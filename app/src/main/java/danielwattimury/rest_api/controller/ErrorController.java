package danielwattimury.rest_api.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import danielwattimury.rest_api.model.WebResponse;

@Slf4j
@RestControllerAdvice
public class ErrorController {

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<WebResponse<Map<String, String>>> constraintViolationException(
                        ConstraintViolationException exception) {
                Map<String, String> errors = new HashMap<>();

                for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
                        errors.put(
                                        violation.getPropertyPath().toString(),
                                        violation.getMessage());
                }

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(WebResponse
                                                .<Map<String, String>>builder()
                                                .status("error")
                                                .message("Validation failed")
                                                .data(errors)
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

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<WebResponse<String>> noHandlerFoundException(NoHandlerFoundException exception) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(WebResponse
                                                .<String>builder()
                                                .status("error")
                                                .message("Endpoint not found: " + exception.getRequestURL())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<WebResponse<String>> handleGeneric(Exception ex) {
                log.error("Unexpected error occurred", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(WebResponse.<String>builder()
                                                .status("error")
                                                .message("An unexpected error occurred")
                                                .build());
        }

        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<WebResponse<String>> authorizationDeniedException(
                        AuthorizationDeniedException exception) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(WebResponse.<String>builder()
                                                .status("error")
                                                .message("Access denied: you do not have permission to perform this action")
                                                .build());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<WebResponse<String>> badCredentialsException(
                        BadCredentialsException exception) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(WebResponse.<String>builder()
                                                .status("error")
                                                .message("Username or password wrong")
                                                .build());
        }
}
