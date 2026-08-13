package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import danielwattimury.rest_api.dto.AddressRequestDto;
import danielwattimury.rest_api.dto.AddressResponseDto;
import danielwattimury.rest_api.entity.Address;
import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.Role;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.enums.RoleEnum;
import danielwattimury.rest_api.responses.Response;
import tools.jackson.core.type.TypeReference;

public class AddressControllerTest extends BaseIntegrationTest {

    private User sampleUser;

    private Contact sampleContact;

    private String token;

    private Contact createContact(String firstName, String lastName, String email, String phone, User user) {
        Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setUser(user);
        return contactRepository.save(contact);
    }

    private Address createAddress(Contact contact) {
        Address address = new Address();
        address.setCity("Denpasar");
        address.setCountry("Indonesia");
        address.setPostalCode("80225");
        address.setProvince("Bali");
        address.setStreet("Jl. Sunset Road");
        address.setContact(contact);

        return addressRepository.save(address);
    }

    @BeforeEach
    void setUp() throws Exception {
        addressRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();

        sampleUser = registerUser();
        sampleContact = createContact("Jane", "Doe", "jane@example.com", "081111111111", sampleUser);
        token = login(sampleUser.getUsername(), DEFAULT_PASSWORD).getAccessToken();
    }

    /*
     * Only provide:
     * 1. City
     * 2. Postal Code
     * 3. Province
     * 4. Street
     * 5. Contact Id
     * 
     * Should: Error
     * Reason: Country is undefined
     */
    @Test
    void testPostValidationError() throws Exception {
        AddressRequestDto request = new AddressRequestDto();
        request.setCity("Denpasar");
        request.setPostalCode("80225");
        request.setProvince("Bali");
        request.setStreet("Jl. Sunset Road");

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/contacts/{contactId}/addresses", sampleContact.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    Response<Map<String, String>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<Map<String, String>>>() {
                            });

                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertTrue(response.getData().containsKey("country"));
                });
    }

    @Test
    void testPostSuccess() throws Exception {
        AddressRequestDto request = new AddressRequestDto();
        request.setCity("Denpasar");
        request.setPostalCode("80225");
        request.setProvince("Bali");
        request.setStreet("Jl. Sunset Road");
        request.setCountry("Indonesia");

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/contacts/{contactId}/addresses", sampleContact.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        List<Address> addresses = addressRepository.findAll();
        assertEquals(1, addresses.size());
        assertEquals(sampleContact.getId(), addresses.get(0).getContact().getId());
    }

    @Test
    void testPutSuccess() throws Exception {
        // Arrange
        Address address = createAddress(sampleContact);

        AddressRequestDto request = new AddressRequestDto();
        request.setCity("Badung");
        request.setPostalCode("80361");
        request.setProvince("Bali");
        request.setStreet("Jl. Raya Kuta");
        request.setCountry("Indonesia");

        // Act & Assert
        mockMvc.perform(
                put(ApiConstants.API_BASE_PATH
                        + "/contacts/{contactId}/addresses/{addressId}",
                        sampleContact.getId(),
                        address.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify database
        Address updatedAddress = addressRepository
                .findById(address.getId())
                .orElseThrow();

        assertEquals("Badung", updatedAddress.getCity());
        assertEquals("80361", updatedAddress.getPostalCode());
        assertEquals("Bali", updatedAddress.getProvince());
        assertEquals("Jl. Raya Kuta", updatedAddress.getStreet());
        assertEquals("Indonesia", updatedAddress.getCountry());

        // Make sure ownership didn't change
        assertEquals(
                sampleContact.getId(),
                updatedAddress.getContact().getId());
    }

    @Test
    void testGetOneSuccess() throws Exception {
        Address savedAddress = createAddress(sampleContact);

        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/{contactId}/addresses/{addressId}",
                sampleContact.getId(), savedAddress.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Response<AddressResponseDto> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<AddressResponseDto>>() {
                            });

                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(
                            "Address retrieved successfully",
                            response.getMessage());

                    AddressResponseDto data = response.getData();

                    assertEquals("Denpasar", data.getCity());
                    assertEquals("Indonesia", data.getCountry());
                    assertEquals("80225", data.getPostalCode());
                    assertEquals("Bali", data.getProvince());
                    assertEquals("Jl. Sunset Road", data.getStreet());
                });
    }

    @Test
    void testDeleteSuccess() throws Exception {
        Address savedAddress = createAddress(sampleContact);

        mockMvc.perform(delete(ApiConstants.API_BASE_PATH + "/contacts/{contactId}/addresses/{addressId}",
                sampleContact.getId(), savedAddress.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertFalse(addressRepository.existsById(savedAddress.getId()));
    }

    @Test
    void getAllAddressesSuccess() throws Exception {
        createAddress(sampleContact);
        createAddress(sampleContact);

        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + sampleContact.getId() + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Response<List<AddressResponseDto>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<List<AddressResponseDto>>>() {
                            });

                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2, response.getData().size());
                });
    }

    @Test
    void getAllAddressesReturnsEmptyListWhenNoAddresses() throws Exception {
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + sampleContact.getId() + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Response<List<AddressResponseDto>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<List<AddressResponseDto>>>() {
                            });

                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getData().isEmpty());
                });
    }

    @Test
    void getAllAddressesPagingMetadataIsCorrect() throws Exception {
        for (int i = 0; i < 15; i++) {
            createAddress(sampleContact);
        }

        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + sampleContact.getId() + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Response<List<AddressResponseDto>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<List<AddressResponseDto>>>() {
                            });

                    assertEquals(10, response.getData().size());
                    assertNotNull(response.getPagination());
                    assertEquals(0, response.getPagination().getPage());
                    assertEquals(2, response.getPagination().getTotalPages()); // 15 items / size 10 -> 2 pages
                    assertEquals(10, response.getPagination().getSize());
                });
    }

    @Test
    void getAllAddressesSecondPage() throws Exception {
        for (int i = 0; i < 15; i++) {
            createAddress(sampleContact);
        }

        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + sampleContact.getId() + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Response<List<AddressResponseDto>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<List<AddressResponseDto>>>() {
                            });

                    assertEquals(5, response.getData().size()); // remaining 5 of 15
                    assertEquals(1, response.getPagination().getPage());
                });
    }

    @Test
    void getAllAddressesUsesDefaultPagingWhenParamsOmitted() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAddress(sampleContact);
        }

        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + sampleContact.getId() + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Response<List<AddressResponseDto>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<List<AddressResponseDto>>>() {
                            });

                    assertEquals(3, response.getData().size());
                    assertEquals(0, response.getPagination().getPage()); // default page = 0
                    assertEquals(10, response.getPagination().getSize()); // default size = 10
                });
    }

    @Test
    void getAllAddressesContactNotFound() throws Exception {
        Integer nonExistentContactId = 99999;

        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + nonExistentContactId + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(result -> {
                    Response<String> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<Response<String>>() {
                            });

                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                });
    }

    @Test
    void getAllAddressesForAnotherUsersContactReturnsNotFound() throws Exception {
        Role role = roleRepository.findByName(RoleEnum.USER).orElseThrow();
        User otherUser = new User();
        otherUser.setUsername("other_user");
        otherUser.setName("Other User");
        otherUser.setPassword(encoder.encode(DEFAULT_PASSWORD));
        otherUser.setRole(role);
        userRepository.save(otherUser);

        Contact otherContact = createContact("Someone", "Else", "someone@example.com", "089999999999", otherUser);
        createAddress(otherContact);

        // sampleUser's token trying to list addresses on otherUser's contact
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/contacts/" + otherContact.getId() + "/addresses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
