package danielwattimury.rest_api.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.model.PatchCurrentUserRequest;
import danielwattimury.rest_api.model.PatchCurrentUserResponse;
import danielwattimury.rest_api.model.WebResponse;
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
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<GetCurrentUserResponse> get(Authentication authentication) {
        GetCurrentUserResponse getCurrentUserResponse = userService.get(authentication.getName());

        return WebResponse
                .<GetCurrentUserResponse>builder()
                .status("success")
                .data(getCurrentUserResponse)
                .build();
    }

    @Operation(summary = "Update current user", description = "Partially updates the currently authenticated user's profile. "
            + "Only the fields provided in the request body are updated (name and/or password); "
            + "omitted fields are left unchanged.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(path = "/current", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<PatchCurrentUserResponse> patch(Authentication authentication,
            @RequestBody PatchCurrentUserRequest request) {
        PatchCurrentUserResponse patchCurrentUserResponse = userService.patch(authentication.getName(), request);

        return WebResponse
                .<PatchCurrentUserResponse>builder()
                .status("success")
                .message("User data updated successfully")
                .data(patchCurrentUserResponse)
                .build();
    }

}
