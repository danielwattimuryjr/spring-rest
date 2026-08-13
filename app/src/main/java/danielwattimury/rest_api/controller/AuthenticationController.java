package danielwattimury.rest_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.dto.LoginRequestDto;
import danielwattimury.rest_api.dto.LoginResponseDto;
import danielwattimury.rest_api.dto.RegisterRequestDto;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.responses.Response;
import danielwattimury.rest_api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Response<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto loginResponse = authenticationService.login(request);

        return Response.successfulResponse("Login Successfully", loginResponse);
    }

    @Operation(summary = "Register user", description = "Create a user and sotre it into DB")
    @SecurityRequirements
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<UserResponseDto>> register(@RequestBody RegisterRequestDto request) {
        UserResponseDto registerUserResponse = authenticationService.register(request);
        Response<UserResponseDto> successfulResponse = Response.successfulResponse(HttpStatus.CREATED,
                "User created successfully",
                registerUserResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<LoginResponseDto> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7);
        LoginResponseDto refreshResponse = authenticationService.refreshToken(refreshToken);

        return Response.successfulResponse("Success", refreshResponse);
    }

    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<LoginResponseDto> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7);
        authenticationService.logout(refreshToken);

        return Response.successfulResponse("User has been log out successfully");
    }
}
