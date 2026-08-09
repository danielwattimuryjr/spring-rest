package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.model.GetCurrentUserResponse;
import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.model.LoginUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import tools.jackson.core.type.TypeReference;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends BaseIntegrationTest {

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
}
