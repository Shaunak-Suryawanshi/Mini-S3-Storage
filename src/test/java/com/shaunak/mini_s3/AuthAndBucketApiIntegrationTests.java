package com.shaunak.mini_s3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndBucketApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerRejectsInvalidEmail() throws Exception {
        String requestBody = """
                {
                  "username": "invalid_email_user",
                  "email": "not-an-email",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Invalid email format"));
    }

    @Test
    void protectedBucketApiRejectsMissingJwt() throws Exception {
        mockMvc.perform(get("/api/buckets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanCreateBucket() throws Exception {
        String token = registerAndLogin();
        String bucketName = "bucket-" + UUID.randomUUID().toString().substring(0, 8);
        String requestBody = """
                {
                  "bucketName": "%s",
                  "visibility": "PRIVATE"
                }
                """.formatted(bucketName);

        mockMvc.perform(post("/api/buckets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bucketName").value(bucketName))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void createBucketRejectsInvalidBucketName() throws Exception {
        String token = registerAndLogin();
        String requestBody = """
                {
                  "bucketName": "Invalid_Bucket",
                  "visibility": "PRIVATE"
                }
                """;

        mockMvc.perform(post("/api/buckets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bucketName: Bucket name must start and end with a lowercase letter or number and contain only lowercase letters, numbers, and hyphens"));
    }

    private String registerAndLogin() throws Exception {
        String uniqueValue = UUID.randomUUID().toString().replace("-", "");
        String username = "user_" + uniqueValue.substring(0, 12);
        String email = username + "@example.com";
        String password = "password123";

        String registerRequest = """
                {
                  "username": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(username, email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(loginResponse);
        return responseJson.get("token").asText();
    }
}
