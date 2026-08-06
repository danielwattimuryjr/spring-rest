package danielwattimury.rest_api.service;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.repository.UserRepository;
import danielwattimury.rest_api.security.BCrypt;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.Getter;

@Service
public class UserService {

    @Getter
    private UserRepository userRepository;

    @Getter
    private Validator validator;

    public UserService(UserRepository userRepository, Validator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Transactional
    public void register(RegisterUserRequest request) {
        Set<ConstraintViolation<RegisterUserRequest>> constraintViolations = validator.validate(request);
        if (constraintViolations.size() != 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        if (userRepository.existsById(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setName(request.getName());

        userRepository.save(user);
    }
}
