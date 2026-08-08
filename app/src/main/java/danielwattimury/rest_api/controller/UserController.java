package danielwattimury.rest_api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.GetCurrentUserResponse;
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

    @GetMapping(path = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<GetCurrentUserResponse> get() {
        //

        return WebResponse
                .<GetCurrentUserResponse>builder()
                .status("success")

                .build();
    }

}
