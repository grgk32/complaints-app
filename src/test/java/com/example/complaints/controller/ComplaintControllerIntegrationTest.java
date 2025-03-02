package com.example.complaints.controller;

import com.example.complaints.dto.ComplaintCreateRequest;
import com.example.complaints.dto.ComplaintUpdateRequest;
import com.example.complaints.model.Complaint;
import com.example.complaints.repository.ComplaintRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.wait.strategy.Wait;
import java.time.LocalDateTime;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class ComplaintControllerIntegrationTest {

    @Container
    public static GenericContainer<?> postgresContainer = new GenericContainer<>("postgres:14")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "complaintsdb")
            .withEnv("POSTGRES_USER", "user")
            .withEnv("POSTGRES_PASSWORD", "password")
            .waitingFor(Wait.forListeningPort());

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:6")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = "jdbc:postgresql://"
                + postgresContainer.getHost() + ":"
                + postgresContainer.getMappedPort(5432) + "/complaintsdb";
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> "user");
        registry.add("spring.datasource.password", () -> "password");

        registry.add("spring.redis.host", () -> redisContainer.getHost());
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        complaintRepository.deleteAll();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testCreateComplaint() throws Exception {
        ComplaintCreateRequest request = new ComplaintCreateRequest("prod123", "Test complaint", "John Doe");
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is("prod123")))
                .andExpect(jsonPath("$.content", is("Test complaint")))
                .andExpect(jsonPath("$.complainant", is("John Doe")))
                .andExpect(jsonPath("$.counter", is(1)));
    }

    @Test
    public void testUpdateComplaint() throws Exception {
        Complaint complaint = Complaint.builder()
                .productId("prod123")
                .content("Initial content")
                .complainant("John Doe")
                .createdDate(LocalDateTime.now())
                .country("Poland")
                .counter(1)
                .build();
        complaint = complaintRepository.save(complaint);
        ComplaintUpdateRequest updateRequest = new ComplaintUpdateRequest("Updated content");
        mockMvc.perform(put("/api/complaints/{id}", complaint.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(complaint.getId().intValue())))
                .andExpect(jsonPath("$.content", is("Updated content")));
    }

    @Test
    public void testGetAllComplaints() throws Exception {
        Complaint complaint1 = Complaint.builder()
                .productId("prod123")
                .content("Content 1")
                .complainant("John Doe")
                .createdDate(LocalDateTime.now())
                .country("Poland")
                .counter(1)
                .build();
        Complaint complaint2 = Complaint.builder()
                .productId("prod456")
                .content("Content 2")
                .complainant("Jane Doe")
                .createdDate(LocalDateTime.now())
                .country("USA")
                .counter(1)
                .build();
        complaintRepository.save(complaint1);
        complaintRepository.save(complaint2);
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
