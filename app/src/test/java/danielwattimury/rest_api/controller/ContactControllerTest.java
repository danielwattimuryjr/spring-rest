package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.model.PostContactRequest;
import danielwattimury.rest_api.model.WebResponse;
import tools.jackson.core.type.TypeReference;

public class ContactControllerTest extends BaseIntegrationTest {

    private User sampleUser;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();

        sampleUser = registerUser();
    }

    /*
     * Only provide:
     * 1. Last Name
     * 2. Phone
     * 3. Malformed Email
     */
    @Test
    void testPostValidationError() throws Exception {

        String token = loginAndGetToken(sampleUser.getUsername(), BaseIntegrationTest.DEFAULT_PASSWORD);

        PostContactRequest postContactRequest = new PostContactRequest();
        postContactRequest.setLastName("Wattimury");
        postContactRequest.setPhone("+01 234-456-789");
        postContactRequest.setEmail("malformed.email");

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/contacts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(postContactRequest)))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    WebResponse<Map<String, String>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponse<Map<String, String>>>() {
                            });

                    assertEquals("error", response.getStatus());
                    assertTrue(response.getData().containsKey("email"));
                    assertTrue(response.getData().containsKey("firstName"));
                });
    }

    @Test
    void testPostSuccess() throws Exception {
        String token = loginAndGetToken(sampleUser.getUsername(), DEFAULT_PASSWORD);

        PostContactRequest request = new PostContactRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPhone("081234567890");

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        List<Contact> contacts = contactRepository.findAll();
        assertEquals(1, contacts.size());
        assertEquals(sampleUser.getUsername(), contacts.get(0).getUser().getUsername());
    }

}
