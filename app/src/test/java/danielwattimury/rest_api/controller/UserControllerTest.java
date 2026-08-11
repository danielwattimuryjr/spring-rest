package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.enums.ResponseStatus;
import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.model.LoginUserResponse;
import danielwattimury.rest_api.model.PatchCurrentUserRequest;
import danielwattimury.rest_api.model.PatchCurrentUserResponse;
import danielwattimury.rest_api.model.WebResponse;
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

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("password123");

        mockLoginRequest(loginRequest).andExpect(status().isOk()).andDo(result -> {
            WebResponse<LoginUserResponse> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<WebResponse<LoginUserResponse>>() {
                    });

            mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/users/current")
                    .header("Authorization", "Bearer " + response.getData().getToken())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(currentUserResult -> {
                        WebResponse<GetCurrentUserResponse> currentUserResponse = objectMapper.readValue(
                                currentUserResult.getResponse().getContentAsString(),
                                new TypeReference<WebResponse<GetCurrentUserResponse>>() {
                                });

                        assertEquals(loginRequest.getUsername(), currentUserResponse.getData().getUsername());
                    });
        });
    }

    @Test
    void updateCurrentUserSuccess() throws Exception {
        registerUser();

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("password123");

        mockLoginRequest(loginRequest).andExpect(status().isOk()).andDo(result -> {
            WebResponse<LoginUserResponse> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<WebResponse<LoginUserResponse>>() {
                    });

            PatchCurrentUserRequest request = new PatchCurrentUserRequest();
            request.setName("john_doe_updated");

            String jsonRequest = objectMapper.writeValueAsString(request);

            mockMvc.perform(patch(ApiConstants.API_BASE_PATH + "/users/current")
                    .header("Authorization", "Bearer " + response.getData().getToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andDo(currentUserResult -> {
                        WebResponse<PatchCurrentUserResponse> currentUserResponse = objectMapper.readValue(
                                currentUserResult.getResponse().getContentAsString(),
                                new TypeReference<WebResponse<PatchCurrentUserResponse>>() {
                                });

                        assertEquals(ResponseStatus.SUCCESS, currentUserResponse.getStatus());
                        assertEquals(loginRequest.getUsername(), currentUserResponse.getData().getUsername());
                        assertEquals("john_doe_updated", currentUserResponse.getData().getName());
                    });
        });

        User updatedUser = userRepository.findByUsername("john_doe").orElseThrow();
        assertEquals("john_doe_updated", updatedUser.getName());
    }
}
