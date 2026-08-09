package danielwattimury.rest_api.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<GetCurrentUserResponse> get(Authentication authentication) {
        GetCurrentUserResponse getCurrentUserResponse = userService.get(authentication.getName());

        return WebResponse
                .<GetCurrentUserResponse>builder()
                .status("success")
                .data(getCurrentUserResponse)
                .build();
    }

}
