package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import danielwattimury.rest_api.dto.AddressRequestDto;
import danielwattimury.rest_api.dto.AddressResponseDto;
import danielwattimury.rest_api.dto.WebResponseDto;
import danielwattimury.rest_api.entity.Address;
import danielwattimury.rest_api.entity.Contact;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.enums.ResponseStatus;
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
        token = loginAndGetToken(sampleUser.getUsername(), DEFAULT_PASSWORD);
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
                    WebResponseDto<Map<String, String>> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponseDto<Map<String, String>>>() {
                            });

                    assertEquals(ResponseStatus.ERROR, response.getStatus());
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
                    WebResponseDto<AddressResponseDto> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<WebResponseDto<AddressResponseDto>>() {
                            });

                    assertEquals(ResponseStatus.SUCCESS, response.getStatus());
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

}
