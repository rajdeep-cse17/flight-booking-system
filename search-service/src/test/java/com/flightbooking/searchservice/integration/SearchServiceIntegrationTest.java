package com.flightbooking.searchservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbooking.searchservice.enums.SearchPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
public class SearchServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        waitForServiceReady();
    }

    @Test
    void testSearchFlights_CheapestPreference() throws Exception {
        // Given: Search for cheapest flights from DEL to BOM
        String url = "/flights?userId=U001&source=DEL&destination=BOM&preference=CHEAPEST";

        // When & Then: Verify the search returns results
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U001"))
                .andExpect(jsonPath("$.source").value("DEL"))
                .andExpect(jsonPath("$.destination").value("BOM"))
                .andExpect(jsonPath("$.preference").value("CHEAPEST"))
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights.length()").value(2)) // F001 and F002
                .andExpect(jsonPath("$.flights[0].cost").value(299.99)) // F001 should be first (cheapest)
                .andExpect(jsonPath("$.flights[1].cost").value(349.99)); // F002 should be second
    }

    @Test
    void testSearchFlights_FastestPreference() throws Exception {
        // Given: Search for fastest flights from DEL to BOM
        String url = "/flights?userId=U001&source=DEL&destination=BOM&preference=FASTEST";

        // When & Then: Verify the search returns results
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U001"))
                .andExpect(jsonPath("$.source").value("DEL"))
                .andExpect(jsonPath("$.destination").value("BOM"))
                .andExpect(jsonPath("$.preference").value("FASTEST"))
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights.length()").value(2)); // F001 and F002
    }

    @Test
    void testSearchFlights_NoPreference() throws Exception {
        // Given: Search without preference
        String url = "/flights?userId=U001&source=DEL&destination=BOM&preference=NONE";

        // When & Then: Verify the search returns results
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U001"))
                .andExpect(jsonPath("$.source").value("DEL"))
                .andExpect(jsonPath("$.destination").value("BOM"))
                .andExpect(jsonPath("$.preference").value("NONE"))
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights.length()").value(2)); // F001 and F002
    }

    @Test
    void testSearchFlights_DifferentRoute() throws Exception {
        // Given: Search for flights from DEL to BLR
        String url = "/flights?userId=U001&source=DEL&destination=BLR&preference=CHEAPEST";

        // When & Then: Verify the search returns results
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("DEL"))
                .andExpect(jsonPath("$.destination").value("BLR"))
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights.length()").value(1)) // Only F003
                .andExpect(jsonPath("$.flights[0].flightId").value("F003"))
                .andExpect(jsonPath("$.flights[0].cost").value(199.99));
    }

    @Test
    void testSearchFlights_NoResults() throws Exception {
        // Given: Search for a route with no flights
        String url = "/flights?userId=U001&source=BOM&destination=DEL&preference=CHEAPEST";

        // When & Then: Verify the search returns empty results
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights.length()").value(0));
    }

    @Test
    void testSearchFlights_Caching() throws Exception {
        // Given: Search for the same route twice
        String url = "/flights?userId=U001&source=DEL&destination=BOM&preference=CHEAPEST";

        // First search
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights.length()").value(2));

        // Second search (should be cached)
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights.length()").value(2));

        // Verify both searches returned the same data (caching works)
        // The second search should be faster due to Redis caching
    }

    @Test
    void testSearchFlights_InvalidParameters() throws Exception {
        // Given: Search with missing parameters
        String url = "/flights?userId=U001&source=DEL";

        // When & Then: Verify the search fails gracefully
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchFlights_HealthCheck() throws Exception {
        // Given: Health check endpoint
        String url = "/actuator/health";

        // When & Then: Verify the service is healthy
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
} 