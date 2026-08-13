package danielwattimury.rest_api.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.dto.PatchUserDto;
import danielwattimury.rest_api.responses.Response;
import danielwattimury.rest_api.security.UserPrincipal;
import danielwattimury.rest_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Users", description = "Endpoints for retrieving user information")
@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current user", description = "Returns information about the currently authenticated user, based on the JWT token")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<UserResponseDto> get(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponseDto getCurrentUserResponse = userService.get(principal.getUserId());

        return Response.successfulResponse("User data retrieved successfully", getCurrentUserResponse);
    }

    @Operation(summary = "Update current user", description = "Partially updates the currently authenticated user's profile. "
            + "Only the fields provided in the request body are updated (name and/or password); "
            + "omitted fields are left unchanged.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(path = "/current", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<UserResponseDto> patch(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PatchUserDto request) {
        UserResponseDto patchCurrentUserResponse = userService.patch(principal.getUserId(), request);

        return Response.successfulResponse("User data updated successfully", patchCurrentUserResponse);
    }

}
