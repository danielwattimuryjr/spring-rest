package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.dto.ContactRequestDto;
import danielwattimury.rest_api.dto.ContactResponseDto;
import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.Role;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.enums.RoleEnum;
import danielwattimury.rest_api.responses.Response;
import tools.jackson.core.type.TypeReference;

public class ContactControllerTest extends BaseIntegrationTest {

        private User sampleUser;

        private String token;

        @BeforeEach
        void setUp() throws Exception {
                contactRepository.deleteAll();
                userRepository.deleteAll();

                sampleUser = registerUser();
                token = login(sampleUser.getUsername(), DEFAULT_PASSWORD).getAccessToken();
        }

        private Contact createContact(String firstName, String lastName, String email, String phone) {
                Contact contact = new Contact();
                contact.setFirstName(firstName);
                contact.setLastName(lastName);
                contact.setEmail(email);
                contact.setPhone(phone);
                contact.setUser(sampleUser);
                return contactRepository.save(contact);
        }

        /*
         * Only provide:
         * 1. Last Name
         * 2. Phone
         * 3. Malformed Email
         * 
         * Should: Error
         * Reason: Email/First Name is null
         */
        @Test
        void testPostValidationError() throws Exception {
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
                                        Response<Map<String, String>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<Map<String, String>>>() {
                                                        });

                                        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                                        assertTrue(response.getData().containsKey("email"));
                                        assertTrue(response.getData().containsKey("firstName"));
                                });
        }

        @Test
        void testPostSuccess() throws Exception {
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
                // Create initial contact
                Contact contact = createContact("John", "Doe", "john@example.com", "081111111111");

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

        @Test
        void testGetOneSuccess() throws Exception {
                Contact savedContact = createContact("John", "Doe", "john@example.com", "081111111111");

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + savedContact.getId())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<ContactResponseDto> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<ContactResponseDto>>() {
                                                        });

                                        assertEquals(HttpStatus.OK, response.getStatusCode());
                                        assertEquals(
                                                        "Contact retrieved successfully",
                                                        response.getMessage());

                                        ContactResponseDto data = response.getData();

                                        assertEquals("John", data.getFirstName());
                                        assertEquals("Doe", data.getLastName());
                                        assertEquals("john@example.com", data.getEmail());
                                        assertEquals("081111111111", data.getPhone());
                                });
        }

        @Test
        void testDeleteSuccess() throws Exception {
                Contact savedContact = createContact("John", "Doe", "john@example.com", "081111111111");

                mockMvc.perform(delete(ApiConstants.API_BASE_PATH + "/contacts/" + savedContact.getId())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk());

                assertFalse(contactRepository.existsById(savedContact.getId()));
        }

        @Test
        void testSearchContactSuccessNoFilters() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");
                createContact("John", "Smith", "john@example.com", "082222222222");

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(HttpStatus.OK, response.getStatusCode());
                                        assertEquals(2, response.getData().size());
                                });
        }

        @Test
        void searchContactsFilterByName() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");
                createContact("John", "Smith", "john@example.com", "082222222222");

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("name", "Jane"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(1, response.getData().size());
                                        assertEquals("Jane", response.getData().get(0).getFirstName());
                                });
        }

        @Test
        void searchContactsFilterByNameMatchesLastName() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");
                createContact("John", "Smith", "john@example.com", "082222222222");

                // "name" should match against last name too, not just first name
                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("name", "Smith"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(1, response.getData().size());
                                        assertEquals("John", response.getData().get(0).getFirstName());
                                });
        }

        @Test
        void searchContactsFilterByEmail() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");
                createContact("John", "Smith", "john@example.com", "082222222222");

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("email", "john@"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(1, response.getData().size());
                                        assertEquals("john@example.com", response.getData().get(0).getEmail());
                                });
        }

        @Test
        void searchContactsFilterByPhone() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");
                createContact("John", "Smith", "john@example.com", "082222222222");

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("phone", "0822"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(1, response.getData().size());
                                        assertEquals("082222222222", response.getData().get(0).getPhone());
                                });
        }

        @Test
        void searchContactsNoResultsMatchingFilter() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("name", "Nonexistent"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(0, response.getData().size());
                                });
        }

        @Test
        void searchContactsRespectsPagination() throws Exception {
                for (int i = 0; i < 15; i++) {
                        createContact("First" + i, "Last" + i, "user" + i + "@example.com", "08" + i);
                }

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(10, response.getData().size());
                                });
        }

        @Test
        void searchContactsSecondPage() throws Exception {
                for (int i = 0; i < 15; i++) {
                        createContact("First" + i, "Last" + i, "user" + i + "@example.com", "08" + i);
                }

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(5, response.getData().size()); // remaining 5 of 15
                                });
        }

        @Test
        void searchContactsDefaultsWhenNoPagingParamsGiven() throws Exception {
                for (int i = 0; i < 3; i++) {
                        createContact("First" + i, "Last" + i, "user" + i + "@example.com", "08" + i);
                }

                // no page/size params — should fall back to defaults (page=0, size=10)
                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(3, response.getData().size());
                                });
        }

        @Test
        void searchContactsOnlyReturnsOwnContacts() throws Exception {
                createContact("Jane", "Doe", "jane@example.com", "081111111111");

                // a second user's contact should never appear in sampleUser's search results
                Role role = roleRepository.findByName(RoleEnum.USER).orElseThrow();
                User otherUser = new User();
                otherUser.setUsername("other_user");
                otherUser.setName("Other User");
                otherUser.setPassword(encoder.encode(DEFAULT_PASSWORD));
                otherUser.setRole(role);
                userRepository.save(otherUser);

                Contact otherContact = new Contact();
                otherContact.setFirstName("Someone");
                otherContact.setLastName("Else");
                otherContact.setEmail("someone@example.com");
                otherContact.setPhone("089999999999");
                otherContact.setUser(otherUser);
                contactRepository.save(otherContact);

                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<List<ContactResponseDto>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<List<ContactResponseDto>>>() {
                                                        });

                                        assertEquals(1, response.getData().size());
                                        assertEquals("Jane", response.getData().get(0).getFirstName());
                                });
        }

        @Test
        void searchContactsWithoutToken() throws Exception {
                mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }
}
