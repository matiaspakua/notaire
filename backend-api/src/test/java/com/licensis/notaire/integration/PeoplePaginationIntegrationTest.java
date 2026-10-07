package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code GET /api/v1/people} is paginated (slice of issue #596): it answers a Spring
 * Data page bounded by {@code size} instead of the whole table.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("People list pagination (issue #596)")
class PeoplePaginationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        for (String number : new String[] {"59601", "59602"}) {
            mockMvc.perform(post("/api/v1/people")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"firstName\": \"Pag\", \"lastName\": \"Ina" + number
                                    + "\", \"identificationNumber\": \"" + number + "\", \"isClient\": true}"))
                    // 409 when an earlier test of this class already created the person
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(201, 409));
        }
    }

    @Test
    @DisplayName("shouldReturnRequestedPageSize")
    void shouldReturnRequestedPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/people").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("shouldDefaultToTwentyPerPage")
    void shouldDefaultToTwentyPerPage() throws Exception {
        mockMvc.perform(get("/api/v1/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content").isArray());
    }
}
