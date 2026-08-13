package danielwattimury.rest_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import danielwattimury.rest_api.BaseIntegrationTest;
import danielwattimury.rest_api.constants.ApiConstants;
import danielwattimury.rest_api.dto.LoginRequestDto;
import danielwattimury.rest_api.dto.LoginResponseDto;
import danielwattimury.rest_api.dto.RegisterRequestDto;
import danielwattimury.rest_api.dto.UserResponseDto;
import danielwattimury.rest_api.entity.RefreshToken;
import danielwattimury.rest_api.entity.User;
import danielwattimury.rest_api.responses.Response;
import tools.jackson.core.type.TypeReference;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest extends BaseIntegrationTest {

        private User sampleUser;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
                refreshTokenRepository.deleteAll();

                sampleUser = registerUser();
        }

        @Test
        void testRegisterSuccess() throws Exception {
                RegisterRequestDto registerUserRequest = new RegisterRequestDto();
                registerUserRequest.setName("Jane Doe");
                registerUserRequest.setUsername("jane_doe");
                registerUserRequest.setPassword("password123");

                String jsonRequest = objectMapper.writeValueAsString(registerUserRequest);

                mockMvc.perform(
                                post(ApiConstants.API_BASE_PATH + "/auth/register")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(jsonRequest))
                                .andExpectAll(
                                                status().isCreated())
                                .andDo(result -> {
                                        Response<UserResponseDto> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<UserResponseDto>>() {
                                                        });

                                        assertEquals(HttpStatus.CREATED, response.getStatusCode());
                                        assertEquals("Jane Doe", response.getData().getName());
                                        assertEquals("jane_doe", response.getData().getUsername());
                                });
        }

        @Test
        void testLoginSuccess() throws Exception {
                LoginResponseDto data = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                assertNotNull(data.getAccessToken());
                assertNotNull(data.getRefreshToken());
                assertNotNull(data.getRefreshTokenExpiresAt());
                assertTrue(data.getRefreshTokenExpiresAt().isAfter(Instant.now()));
        }

        @Test
        void loginPersistsRefreshTokenInDb() throws Exception {
                LoginResponseDto data = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                RefreshToken saved = refreshTokenRepository.findFirstByToken(data.getRefreshToken())
                                .orElseThrow();

                assertFalse(saved.isRevoked());
                assertEquals(sampleUser.getUsername(), saved.getUser().getUsername());
                long diffMillis = Math.abs(
                                saved.getExpiresAt().toEpochMilli() - data.getRefreshTokenExpiresAt().toEpochMilli());
                assertTrue(diffMillis < 1000,
                                "Expected timestamps to match within 1 second, but differed by " + diffMillis + "ms");
        }

        @Test
        void loginWithWrongPasswordFails() throws Exception {
                LoginRequestDto request = new LoginRequestDto();
                request.setUsername(sampleUser.getUsername());
                request.setPassword("wrong-password");

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ---------------------------------------------------------------
        // REFRESH
        // ---------------------------------------------------------------

        @Test
        void refreshSuccessReturnsNewTokenPair() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andDo(result -> {
                                        Response<LoginResponseDto> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<Response<LoginResponseDto>>() {
                                                        });

                                        assertEquals(HttpStatus.OK, response.getStatusCode());
                                        assertNotNull(response.getData().getAccessToken());
                                        assertNotNull(response.getData().getRefreshToken());

                                        // rotation: new refresh token must differ from the old one
                                        assertNotEquals(loginData.getRefreshToken(),
                                                        response.getData().getRefreshToken());
                                        // new access token should differ too (different iat/exp at minimum)
                                        assertNotEquals(loginData.getAccessToken(),
                                                        response.getData().getAccessToken());
                                });
        }

        @Test
        void refreshRevokesOldTokenAndPersistsNewOne() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                MvcResult result = mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andReturn();

                Response<LoginResponseDto> response = objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                new TypeReference<Response<LoginResponseDto>>() {
                                });

                // old token: must now be revoked
                RefreshToken oldToken = refreshTokenRepository.findFirstByToken(loginData.getRefreshToken())
                                .orElseThrow();
                assertTrue(oldToken.isRevoked());

                // new token: must exist, not revoked
                RefreshToken newToken = refreshTokenRepository.findFirstByToken(response.getData().getRefreshToken())
                                .orElseThrow();
                assertFalse(newToken.isRevoked());
                assertEquals(sampleUser.getUsername(), newToken.getUser().getUsername());
        }

        @Test
        void refreshWithAlreadyRotatedTokenFails() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                // first refresh — rotates and revokes the original token
                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // reusing the original (now-revoked) refresh token must fail
                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void refreshWithRevokedTokenFails() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                RefreshToken token = refreshTokenRepository.findFirstByToken(loginData.getRefreshToken())
                                .orElseThrow();
                token.setRevoked(true);
                refreshTokenRepository.save(token);

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void refreshWithExpiredTokenFails() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                RefreshToken token = refreshTokenRepository.findFirstByToken(loginData.getRefreshToken())
                                .orElseThrow();
                token.setExpiresAt(Instant.now().minusSeconds(60)); // force expiry in the DB record
                refreshTokenRepository.save(token);

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        // ---------------------------------------------------------------
        // LOGOUT
        // ---------------------------------------------------------------

        @Test
        void logoutRevokesRefreshToken() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/logout")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                RefreshToken token = refreshTokenRepository.findFirstByToken(loginData.getRefreshToken())
                                .orElseThrow();
                assertTrue(token.isRevoked());
        }

        @Test
        void logoutThenRefreshFails() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/logout")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void logoutWithAlreadyRevokedTokenFails() throws Exception {
                LoginResponseDto loginData = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                // first logout succeeds
                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/logout")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // second logout with the same (now-revoked) token must fail
                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/logout")
                                .header("Authorization", "Bearer " + loginData.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void logoutDoesNotAffectOtherSessionsRefreshToken() throws Exception {
                // simulate a second login/session for the same user (e.g. a second device)
                LoginResponseDto session1 = login(sampleUser.getUsername(), DEFAULT_PASSWORD);
                LoginResponseDto session2 = login(sampleUser.getUsername(), DEFAULT_PASSWORD);

                // logging out session1 only
                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/logout")
                                .header("Authorization", "Bearer " + session1.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // session2's refresh token must still work
                mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/auth/refresh")
                                .header("Authorization", "Bearer " + session2.getRefreshToken())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }
}
