package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.dto.LoginRequestDto;
import danielwattimury.rest_api.dto.LoginResponseDto;
import danielwattimury.rest_api.dto.PatchUserDto;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.responses.Response;
import tools.jackson.core.type.TypeReference;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testEmptyJwtToken() throws Exception {
        mockMvc.perform(
                get(ApiConstants.API_BASE_PATH + "/users/current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser() throws Exception {
        registerUser();

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("password123");

        mockLoginRequest(loginRequest).andExpect(status().isOk()).andDo(result -> {
            Response<LoginResponseDto> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<Response<LoginResponseDto>>() {
                    });

            mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/users/current")
                    .header("Authorization", "Bearer " + response.getData().getAccessToken())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(currentUserResult -> {
                        Response<UserResponseDto> currentUserResponse = objectMapper.readValue(
                                currentUserResult.getResponse().getContentAsString(),
                                new TypeReference<Response<UserResponseDto>>() {
                                });

                        assertEquals(loginRequest.getUsername(), currentUserResponse.getData().getUsername());
                    });
        });
    }

    @Test
    void updateCurrentUserSuccess() throws Exception {
        registerUser();

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("password123");

        mockLoginRequest(loginRequest).andExpect(status().isOk()).andDo(result -> {
            Response<LoginResponseDto> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<Response<LoginResponseDto>>() {
                    });

            PatchUserDto request = new PatchUserDto();
            request.setName("john_doe_updated");

            String jsonRequest = objectMapper.writeValueAsString(request);

            mockMvc.perform(patch(ApiConstants.API_BASE_PATH + "/users/current")
                    .header("Authorization", "Bearer " + response.getData().getAccessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andDo(currentUserResult -> {
                        Response<UserResponseDto> currentUserResponse = objectMapper.readValue(
                                currentUserResult.getResponse().getContentAsString(),
                                new TypeReference<Response<UserResponseDto>>() {
                                });

                        assertEquals(HttpStatus.OK, currentUserResponse.getStatusCode());
                        assertEquals(loginRequest.getUsername(), currentUserResponse.getData().getUsername());
                        assertEquals("john_doe_updated", currentUserResponse.getData().getName());
                    });
        });

        User updatedUser = userRepository.findByUsername("john_doe").orElseThrow();
        assertEquals("john_doe_updated", updatedUser.getName());
    }
}
