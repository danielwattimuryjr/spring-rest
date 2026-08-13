package danielwattimury.rest_api.responses;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Response<T> {

    private HttpStatus statusCode;

    private String message;

    boolean success = false;

    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pagination pagination;

    public Response(HttpStatus statCode, String statusDesc) {
        statusCode = statCode;
        message = statusDesc;
        this.success = statCode != null && statCode == HttpStatus.OK;
    }

    public Response() {
    }

    public static <T> Response<T> failedResponse(String message) {
        return failedResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    public static <T> Response<T> failedResponse(T data) {
        return failedResponse(HttpStatus.BAD_REQUEST, "Bad request", data);
    }

    public static <T> Response<T> failedResponse(HttpStatus statusCode, String message) {
        return failedResponse(statusCode, message, null);
    }

    public static <T> Response<T> failedResponse(HttpStatus statusCode, String message, T data) {
        Response<T> response = new Response<>(statusCode, message);
        response.setSuccess(false);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> successfulResponse(String message) {
        return successfulResponse(message, null);
    }

    public static <T> Response<T> successfulResponse(String message, T data) {
        return successfulResponse(HttpStatus.OK, message, data);
    }

    public static <T> Response<T> successfulResponse(String message, T data, Pagination pagination) {
        Response<T> response = successfulResponse(message, data);
        response.setPagination(pagination);
        return response;
    }

    public static <T> Response<T> successfulResponse(HttpStatus statusCode, String message, T data) {
        Response<T> response = new Response<>(statusCode, message);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

}
