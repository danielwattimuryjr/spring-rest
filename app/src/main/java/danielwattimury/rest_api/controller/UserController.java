package danielwattimury.rest_api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.model.RegisterUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.service.UserService;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/users")
public class UserController {

    @Getter
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        RegisterUserResponse registerUserResponse = userService.register(request);
        ;

        return WebResponse
                .<RegisterUserResponse>builder()
                .status("success")
                .message("User Created Successfully")
                .data(registerUserResponse)
                .build();
    }

    @GetMapping(path = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<GetCurrentUserResponse> get(User user) {
        GetCurrentUserResponse getCurrentUserResponse = userService.get(user);

        return WebResponse
                .<GetCurrentUserResponse>builder()
                .status("success")
                .data(getCurrentUserResponse)
                .build();
    }

}
