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

import danielwattimury.rest_api.dto.WebResponseDto;
import danielwattimury.rest_api.enums.ResponseStatus;

@Slf4j
@RestControllerAdvice
public class ErrorController {

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<WebResponseDto<Map<String, String>>> constraintViolationException(
                        ConstraintViolationException exception) {
                Map<String, String> errors = new HashMap<>();

                for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
                        errors.put(
                                        violation.getPropertyPath().toString(),
                                        violation.getMessage());
                }

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(WebResponseDto
                                                .<Map<String, String>>builder()
                                                .status(ResponseStatus.ERROR)
                                                .message("Validation failed")
                                                .data(errors)
                                                .build());
        };

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<WebResponseDto<String>> apiException(ResponseStatusException exception) {
                return ResponseEntity
                                .status(exception.getStatusCode())
                                .body(WebResponseDto
                                                .<String>builder()
                                                .status(ResponseStatus.ERROR)
                                                .message(exception.getReason())
                                                .build());
        }

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<WebResponseDto<String>> noHandlerFoundException(NoHandlerFoundException exception) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(WebResponseDto
                                                .<String>builder()
                                                .status(ResponseStatus.ERROR)
                                                .message("Endpoint not found: " + exception.getRequestURL())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<WebResponseDto<String>> handleGeneric(Exception ex) {
                log.error("Unexpected error occurred", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(WebResponseDto.<String>builder()
                                                .status(ResponseStatus.ERROR)
                                                .message("An unexpected error occurred")
                                                .build());
        }

        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<WebResponseDto<String>> authorizationDeniedException(
                        AuthorizationDeniedException exception) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(WebResponseDto.<String>builder()
                                                .status(ResponseStatus.ERROR)
                                                .message("Access denied: you do not have permission to perform this action")
                                                .build());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<WebResponseDto<String>> badCredentialsException(
                        BadCredentialsException exception) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(WebResponseDto.<String>builder()
                                                .status(ResponseStatus.ERROR)
                                                .message("Username or password wrong")
                                                .build());
        }
}
