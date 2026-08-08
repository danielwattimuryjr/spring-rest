package danielwattimury.rest_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.model.LoginUserResponse;
import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.model.RegisterUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.service.AuthenticationService;
import lombok.Getter;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Getter
    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<LoginUserResponse> login(@RequestBody LoginUserRequest request) {
        LoginUserResponse loginResponse = authenticationService.login(request);

        return WebResponse
                .<LoginUserResponse>builder()
                .status("success")
                .message("Login Successfully")
                .data(loginResponse)
                .build();
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        RegisterUserResponse registerUserResponse = authenticationService.register(request);

        return WebResponse
                .<RegisterUserResponse>builder()
                .status("success")
                .message("User Created Successfully")
                .data(registerUserResponse)
                .build();
    }

}
