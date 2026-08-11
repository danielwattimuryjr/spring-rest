package danielwattimury.rest_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.entity.Role;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.enums.RoleEnum;
import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.model.LoginUserResponse;
import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.model.RegisterUserResponse;
import danielwattimury.rest_api.repository.RoleRepository;
import danielwattimury.rest_api.repository.UserRepository;
import danielwattimury.rest_api.security.JwtService;
import danielwattimury.rest_api.security.UserPrincipal;
import danielwattimury.rest_api.security.JwtService.JWTToken;
import jakarta.transaction.Transactional;

@Service
public class AuthenticationService {

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private ValidationService validationService;

    private AuthenticationManager authenticationManager;

    private JwtService jwtService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    public AuthenticationService(UserRepository userRepository, ValidationService validationService,
            AuthenticationManager authenticationManager, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public LoginUserResponse login(LoginUserRequest request) {
        validationService.validate(request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        JWTToken jwt = jwtService.generateToken(authentication.getName(), principal.getUserId().toString());

        return LoginUserResponse.builder()
                .token(jwt.token())
                .tokenExpiredAt(jwt.expiresAt())
                .build();
    }

    @Transactional
    public RegisterUserResponse register(RegisterUserRequest request) {
        validationService.validate(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        Role optionalRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Role with name: " + RoleEnum.USER + " is not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(optionalRole);

        userRepository.save(user);

        return RegisterUserResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }
}
