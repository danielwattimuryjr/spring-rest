package danielwattimury.rest_api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.service.UserService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.Getter;

@RestController
public class UserController {

    @Getter
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/api/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<String> register(@RequestBody RegisterUserRequest request) {
        userService.register(request);

        return WebResponse
                .<String>builder()
                .status("success")
                .message("User Created Successfully")
                .data("OK")
                .build();
    }
}
