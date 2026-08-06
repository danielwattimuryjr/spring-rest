package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.model.LoginUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.repository.UserRepository;
import danielwattimury.rest_api.security.BCrypt;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private void registerUser() {
        User user = new User();
        user.setName("John Doe");
        user.setUsername("john_doe");
        user.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        userRepository.save(user);
    }

    private ResultActions mockLoginRequest(LoginUserRequest request) throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(request);

        return mockMvc.perform(
                post(ApiConstants.API_BASE_PATH + "/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest));
    }

    @Test
    void testLoginFailedUserNotFound() throws Exception {
        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("wrong_user");
        loginRequest.setPassword("wrong_password");

        mockLoginRequest(loginRequest)
                .andExpectAll(
                        status().isNotFound())
                .andDo(result -> {
                    WebResponse<LoginUserResponse> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponse<LoginUserResponse>>() {
                            });

                    assertEquals("error", response.getStatus());
                    assertEquals("Username or password wrong", response.getMessage());
                });
    }

    @Test
    void testLoginWrongCredentials() throws Exception {
        registerUser();

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("wrong_password");

        mockLoginRequest(loginRequest)
                .andExpectAll(
                        status().isNotFound())
                .andDo(result -> {
                    WebResponse<LoginUserResponse> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponse<LoginUserResponse>>() {
                            });

                    assertEquals("error", response.getStatus());
                    assertEquals("Username or password wrong", response.getMessage());
                });
    }

    @Test
    void testLoginSuccess() throws Exception {
        registerUser();

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("password123");

        mockLoginRequest(loginRequest)
                .andExpectAll(
                        status().isOk())
                .andDo(result -> {
                    WebResponse<LoginUserResponse> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponse<LoginUserResponse>>() {
                            });

                    assertEquals("success", response.getStatus());
                    assertNotNull(response.getData().getToken());
                    assertNotNull(response.getData().getTokenExpiredAt());

                    User userDb = userRepository.findById("john_doe").orElse(null);
                    assertNotNull(userDb);
                    assertEquals(userDb.getToken(), response.getData().getToken());
                });
    }
}
