package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import danielwattimury.rest_api.model.ContactRequestDto;
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

        ContactRequestDto postContactRequest = new ContactRequestDto();
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

        ContactRequestDto request = new ContactRequestDto();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPhone("081234567890");

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verify database
        List<Contact> contacts = contactRepository.findAll();
        assertEquals(1, contacts.size());
        assertEquals(sampleUser.getUsername(), contacts.get(0).getUser().getUsername());
    }

    @Test
    void testPutSuccess() throws Exception {
        String token = loginAndGetToken(sampleUser.getUsername(), DEFAULT_PASSWORD);

        // Create initial contact
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setEmail("john@example.com");
        contact.setPhone("081111111111");
        contact.setUser(sampleUser);
        contactRepository.save(contact);

        // Update request
        ContactRequestDto request = new ContactRequestDto();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPhone("081234567890");

        mockMvc.perform(put(ApiConstants.API_BASE_PATH + "/contacts/" + contact.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify database
        Contact updatedContact = contactRepository
                .findById(contact.getId())
                .orElseThrow();

        assertEquals("Jane", updatedContact.getFirstName());
        assertEquals("Doe", updatedContact.getLastName());
        assertEquals("jane@example.com", updatedContact.getEmail());
        assertEquals("081234567890", updatedContact.getPhone());

        // Make sure ownership didn't change
        assertEquals(
                sampleUser.getUsername(),
                updatedContact.getUser().getUsername());
    }

}
