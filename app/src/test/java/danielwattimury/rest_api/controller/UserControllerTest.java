package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.model.RegisterUserRequest;
import danielwattimury.rest_api.model.RegisterUserResponse;
import danielwattimury.rest_api.model.WebResponse;
import danielwattimury.rest_api.repository.UserRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

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

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setName("John Doe");
        registerUserRequest.setUsername("john_doe");
        registerUserRequest.setPassword("password123");

        String jsonRequest = objectMapper.writeValueAsString(registerUserRequest);

        mockMvc.perform(
                post(ApiConstants.API_BASE_PATH + "/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpectAll(
                        status().isOk())
                .andDo(result -> {
                    WebResponse<RegisterUserResponse> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponse<RegisterUserResponse>>() {
                            });

                    assertEquals("success", response.getStatus());
                    assertEquals("John Doe", response.getData().getName());
                    assertEquals("john_doe", response.getData().getUsername());
                });
    }

}
