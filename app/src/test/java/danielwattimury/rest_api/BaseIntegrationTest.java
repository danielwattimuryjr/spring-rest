package danielwattimury.rest_api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.LoginUserRequest;
import danielwattimury.rest_api.repository.UserRepository;
import danielwattimury.rest_api.service.JwtService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtService jwtService;

    protected BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    protected void registerUser() {
        User user = new User();
        user.setName("John Doe");
        user.setUsername("john_doe");
        user.setPassword(encoder.encode("password123"));
        userRepository.save(user);
    }

    protected ResultActions mockLoginRequest(LoginUserRequest request) throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(request);

        return mockMvc.perform(
                post(ApiConstants.API_BASE_PATH + "/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest));
    }
}
