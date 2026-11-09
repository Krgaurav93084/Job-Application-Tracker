package com.kumargaurav.jobtracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobTrackerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- helpers ----------

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private String register(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private Map<String, String> jobBody(String company, String status) {
        return Map.of("company", company, "role", "Java Developer", "status", status,
                "appliedOn", "2026-11-01", "notes", "via LinkedIn");
    }

    private long createJob(String token, String company, String status) throws Exception {
        String response = mvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(jobBody(company, status))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    // ---------- tests ----------

    @Test
    void protectedEndpointRequiresToken() throws Exception {
        mvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndRejectBadCredentials() throws Exception {
        String email = uniqueEmail();
        register(email);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createListAndFilterByStatus() throws Exception {
        String token = register(uniqueEmail());
        createJob(token, "Siemens", "APPLIED");
        createJob(token, "SAP", "INTERVIEW");

        mvc.perform(get("/api/applications").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        mvc.perform(get("/api/applications?status=INTERVIEW").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].company").value("SAP"));
    }

    @Test
    void usersCannotSeeEachOthersApplications() throws Exception {
        String tokenA = register(uniqueEmail());
        String tokenB = register(uniqueEmail());
        long idOfA = createJob(tokenA, "BMW", "APPLIED");

        mvc.perform(get("/api/applications").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));

        mvc.perform(get("/api/applications/" + idOfA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationRejectsBlankCompany() throws Exception {
        String token = register(uniqueEmail());

        mvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(jobBody("", "APPLIED"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.company").exists());
    }

    @Test
    void updateAndDeleteApplication() throws Exception {
        String token = register(uniqueEmail());
        long id = createJob(token, "Allianz", "APPLIED");

        mvc.perform(put("/api/applications/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(jobBody("Allianz", "OFFER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OFFER"));

        mvc.perform(delete("/api/applications/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/applications/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
