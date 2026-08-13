package danielwattimury.rest_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.dto.LoginRequestDto;
import danielwattimury.rest_api.dto.LoginResponseDto;
import danielwattimury.rest_api.dto.RegisterRequestDto;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.dto.WebResponseDto;
import danielwattimury.rest_api.enums.ResponseStatus;
import danielwattimury.rest_api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
    public WebResponseDto<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto loginResponse = authenticationService.login(request);

        return WebResponseDto
                .<LoginResponseDto>builder()
                .status(ResponseStatus.SUCCESS)
                .message("Login Successfully")
                .data(loginResponse)
                .build();
    }

    @Operation(summary = "Register user", description = "Create a user and sotre it into DB")
    @SecurityRequirements
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponseDto<UserResponseDto> register(@RequestBody RegisterRequestDto request) {
        UserResponseDto registerUserResponse = authenticationService.register(request);

        return WebResponseDto
                .<UserResponseDto>builder()
                .status(ResponseStatus.SUCCESS)
                .message("User Created Successfully")
                .data(registerUserResponse)
                .build();
    }

    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponseDto<LoginResponseDto> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7);

        LoginResponseDto refreshResponse = authenticationService.refreshToken(refreshToken);

        return WebResponseDto
                .<LoginResponseDto>builder()
                .status(ResponseStatus.SUCCESS)
                .data(refreshResponse)
                .build();
    }

    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponseDto<LoginResponseDto> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7);

        authenticationService.logout(refreshToken);

        return WebResponseDto
                .<LoginResponseDto>builder()
                .status(ResponseStatus.SUCCESS)
                .message("User has been log out successfully")
                .build();
    }
}
