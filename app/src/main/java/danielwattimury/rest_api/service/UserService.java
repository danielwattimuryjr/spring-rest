package danielwattimury.rest_api.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.model.PatchCurrentUserRequest;
import danielwattimury.rest_api.model.PatchCurrentUserResponse;
import danielwattimury.rest_api.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private UserRepository userRepository;

    private ValidationService validationService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    public UserService(UserRepository userRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    public GetCurrentUserResponse get(String username) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return GetCurrentUserResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public PatchCurrentUserResponse patch(String username, PatchCurrentUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (Objects.nonNull(request.getName())) {
            user.setName(request.getName());
        }

        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(
                    bCryptPasswordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return PatchCurrentUserResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

}
