package danielwattimury.rest_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.model.LoginUserResponse;
import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.model.RegisterUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Login user", description = "Authenticates a user with username and password, returns a JWT token on success")
    @SecurityRequirements
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

    @Operation(summary = "Register user", description = "Create a user and sotre it into DB")
    @SecurityRequirements
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
