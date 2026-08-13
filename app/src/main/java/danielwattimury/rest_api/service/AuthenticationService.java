package danielwattimury.rest_api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import danielwattimury.rest_api.dto.LoginRequestDto;
import danielwattimury.rest_api.dto.LoginResponseDto;
import danielwattimury.rest_api.dto.RegisterRequestDto;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.entity.RefreshToken;
import danielwattimury.rest_api.entity.Role;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.enums.RoleEnum;
import danielwattimury.rest_api.exceptions.ResourceNotFoundException;
import danielwattimury.rest_api.repository.RefreshTokenRepository;
import danielwattimury.rest_api.repository.RoleRepository;
import danielwattimury.rest_api.repository.UserRepository;
import danielwattimury.rest_api.security.JwtService;
import danielwattimury.rest_api.security.UserPrincipal;
import danielwattimury.rest_api.security.JwtService.JwtToken;
import jakarta.transaction.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final ValidationService validationService;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    public AuthenticationService(UserRepository userRepository, ValidationService validationService,
            AuthenticationManager authenticationManager, JwtService jwtService, RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private record TokenPair(
            JwtToken accessToken,
            JwtToken refreshToken) {
    }

    private TokenPair generateTokens(String username, Integer userId) {
        JwtToken accessToken = jwtService.generateToken(username, userId.toString());

        Instant refreshTokenExpiration = Instant.now().plus(7, ChronoUnit.DAYS);
        JwtToken refreshToken = jwtService.generateToken(username, userId.toString(), refreshTokenExpiration);

        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        validationService.validate(request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        TokenPair tokens = generateTokens(authentication.getName(), principal.getUserId());

        User userEntity = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(tokens.refreshToken.token());
        refreshTokenEntity.setExpiresAt(tokens.refreshToken.expiresAt());
        refreshTokenEntity.setUser(userEntity);
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponseDto.builder()
                .accessToken(tokens.accessToken.token())
                .refreshToken(tokens.refreshToken.token())
                .refreshTokenExpiresAt(tokens.refreshToken.expiresAt())
                .build();
    }

    @Transactional
    public LoginResponseDto refreshToken(String refreshToken) {
        RefreshToken existingRefreshToken = refreshTokenRepository.findFirstByToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (existingRefreshToken.isRevoked() || existingRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        boolean isTokenValid = jwtService.validateToken(refreshToken);
        if (!isTokenValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        existingRefreshToken.setRevoked(true);
        refreshTokenRepository.save(existingRefreshToken);

        User user = existingRefreshToken.getUser();
        TokenPair newTokens = generateTokens(user.getUsername(), user.getId());

        RefreshToken newRefreshTokenEntity = new RefreshToken();
        newRefreshTokenEntity.setToken(newTokens.refreshToken.token());
        newRefreshTokenEntity.setExpiresAt(newTokens.refreshToken.expiresAt());
        newRefreshTokenEntity.setUser(user);
        newRefreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(newRefreshTokenEntity);

        return LoginResponseDto.builder()
                .accessToken(newTokens.accessToken.token())
                .refreshToken(newTokens.refreshToken.token())
                .refreshTokenExpiresAt(newTokens.refreshToken.expiresAt())
                .build();
    }

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        validationService.validate(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        Role optionalRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role with name: " + RoleEnum.USER + " is not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(optionalRole);

        userRepository.save(user);

        return UserResponseDto.builder()
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken existingRefreshToken = refreshTokenRepository.findFirstByToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (existingRefreshToken.isRevoked() || existingRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        boolean isTokenValid = jwtService.validateToken(refreshToken);
        if (!isTokenValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        existingRefreshToken.setRevoked(true);
        refreshTokenRepository.save(existingRefreshToken);
    }
}
