package danielwattimury.rest_api.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.dto.PatchCurrentUserRequest;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.entity.User;
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

    public UserResponseDto get(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return UserResponseDto.builder()
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public UserResponseDto patch(Integer userId, PatchCurrentUserRequest request) {
        validationService.validate(request);

        User user = getUserOrFail(userId);

        if (Objects.nonNull(request.getName())) {
            user.setName(request.getName());
        }

        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(
                    bCryptPasswordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return UserResponseDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

    public User getUserOrFail(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

}
