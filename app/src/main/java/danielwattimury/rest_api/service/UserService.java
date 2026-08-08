package danielwattimury.rest_api.service;

import org.springframework.stereotype.Service;

import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.GetCurrentUserResponse;

@Service
public class UserService {

    public GetCurrentUserResponse get(User user) {
        return GetCurrentUserResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }

}
