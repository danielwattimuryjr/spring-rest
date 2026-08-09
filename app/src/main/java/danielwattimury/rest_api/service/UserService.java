package danielwattimury.rest_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public GetCurrentUserResponse get(String username) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return GetCurrentUserResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }

}
