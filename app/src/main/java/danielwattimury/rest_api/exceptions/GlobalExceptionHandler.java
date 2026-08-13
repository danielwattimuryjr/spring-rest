package danielwattimury.rest_api.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import danielwattimury.rest_api.responses.Response;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(Exception.class)
        public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
                log.error(ex.getMessage(), ex);
                if (ex.getCause() instanceof UnknownHostException) {
                        Response<String> error = Response.failedResponse(HttpStatus.NOT_FOUND,
                                        ex.getLocalizedMessage());
                        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                Response<String> error = Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                "We are unable to process your request at this time, please try again later.",
                                ex.getMessage());
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(ApplicationException.class)
        public ResponseEntity<Object> handleApplicationException(ApplicationException ex) {
                return ResponseEntity.status(ex.getHttpStatus())
                                .body(Response.failedResponse(ex.getHttpStatus(), ex.getMessage()));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Response<Map<String, String>>> handleConstraintViolationException(
                        ConstraintViolationException ex) {
                Map<String, String> errors = new HashMap<>();

                for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                        errors.put(
                                        violation.getPropertyPath().toString(),
                                        violation.getMessage());
                }

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Response.<Map<String, String>>failedResponse(HttpStatus.BAD_REQUEST,
                                                ex.getLocalizedMessage(),
                                                errors));
        };

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
                return ResponseEntity.status(ex.getHttpStatus())
                                .body(Response.failedResponse(ex.getHttpStatus(), ex.getMessage()));
        }
}
