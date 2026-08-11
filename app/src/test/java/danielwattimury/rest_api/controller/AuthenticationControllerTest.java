package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.dto.LoginRequestDto;
import danielwattimury.rest_api.dto.LoginResponseDto;
import danielwattimury.rest_api.dto.RegisterRequestDto;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.dto.WebResponseDto;
import danielwattimury.rest_api.enums.ResponseStatus;
import tools.jackson.core.type.TypeReference;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest extends BaseIntegrationTest {

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
        }

        @Test
        void testLoginFailedUserNotFound() throws Exception {
                LoginRequestDto loginRequest = new LoginRequestDto();
                loginRequest.setUsername("wrong_user");
                loginRequest.setPassword("wrong_password");

                mockLoginRequest(loginRequest).andExpectAll(
                                status().isBadRequest());
        }

        @Test
        void testLoginWrongCredentials() throws Exception {
                registerUser();

                LoginRequestDto loginRequest = new LoginRequestDto();
                loginRequest.setUsername("john_doe");
                loginRequest.setPassword("wrong_password");

                mockLoginRequest(loginRequest).andExpectAll(
                                status().isBadRequest());
        }

        @Test
        void testLoginSuccess() throws Exception {
                registerUser();

                LoginRequestDto loginRequest = new LoginRequestDto();
                loginRequest.setUsername("john_doe");
                loginRequest.setPassword("password123");

                mockLoginRequest(loginRequest)
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponseDto<LoginResponseDto> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponseDto<LoginResponseDto>>() {
                                                        });

                                        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
                                        assertNotNull(response.getData().getToken());
                                        assertNotNull(response.getData().getTokenExpiredAt());

                                        String usernameFromToken = jwtService
                                                        .extractUsername(response.getData().getToken());
                                        assertEquals(loginRequest.getUsername(), usernameFromToken);
                                });
        }

        @Test
        void testRegisterSuccess() throws Exception {
                RegisterRequestDto registerUserRequest = new RegisterRequestDto();
                registerUserRequest.setName("John Doe");
                registerUserRequest.setUsername("john_doe");
                registerUserRequest.setPassword("password123");

                String jsonRequest = objectMapper.writeValueAsString(registerUserRequest);

                mockMvc.perform(
                                post(ApiConstants.API_BASE_PATH + "/auth/register")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(jsonRequest))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponseDto<UserResponseDto> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponseDto<UserResponseDto>>() {
                                                        });

                                        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
                                        assertEquals("John Doe", response.getData().getName());
                                        assertEquals("john_doe", response.getData().getUsername());
                                });
        }
}
