package com.flightbooking.searchservice.controller;

import com.flightbooking.searchservice.dto.SearchRequest;
import com.flightbooking.searchservice.dto.SearchResponse;
import com.flightbooking.searchservice.enums.SearchPreference;
import com.flightbooking.searchservice.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private SearchRequest searchRequest;
    private SearchResponse searchResponse;

    @BeforeEach
    void setUp() {
        searchRequest = new SearchRequest("U001", "DEL", "BOM", SearchPreference.CHEAPEST);
        searchResponse = new SearchResponse("U001", "DEL", "BOM", SearchPreference.CHEAPEST, 
            Arrays.asList(), "Search completed successfully");
    }

    @Test
    void testSearchFlights_Success() {
        // Given
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(searchResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U001", "DEL", "BOM", "CHEAPEST");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("U001", response.getBody().getUserId());
        assertEquals("DEL", response.getBody().getSource());
        assertEquals("BOM", response.getBody().getDestination());
        assertEquals(SearchPreference.CHEAPEST, response.getBody().getPreference());
        
        verify(searchService).searchFlights(any(SearchRequest.class));
    }

    @Test
    void testSearchFlights_FastestPreference() {
        // Given
        SearchResponse fastestResponse = new SearchResponse("U001", "DEL", "BOM", SearchPreference.FASTEST, 
            Arrays.asList(), "Fastest route found");
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(fastestResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U001", "DEL", "BOM", "FASTEST");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SearchPreference.FASTEST, response.getBody().getPreference());
        assertEquals("Fastest route found", response.getBody().getMessage());
    }

    @Test
    void testSearchFlights_NoPreference() {
        // Given
        SearchResponse noPreferenceResponse = new SearchResponse("U001", "DEL", "BOM", SearchPreference.NONE, 
            Arrays.asList(), "All routes found");
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(noPreferenceResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U001", "DEL", "BOM", "NONE");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SearchPreference.NONE, response.getBody().getPreference());
        assertEquals("All routes found", response.getBody().getMessage());
    }

    @Test
    void testSearchFlights_DefaultPreference() {
        // Given
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(searchResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U001", "DEL", "BOM", null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchService).searchFlights(any(SearchRequest.class));
    }

    @Test
    void testSearchFlights_DifferentCities() {
        // Given
        SearchResponse bangaloreResponse = new SearchResponse("U001", "BLR", "HYD", SearchPreference.CHEAPEST, 
            Arrays.asList(), "Bangalore to Hyderabad route");
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(bangaloreResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U001", "BLR", "HYD", "CHEAPEST");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("BLR", response.getBody().getSource());
        assertEquals("HYD", response.getBody().getDestination());
        assertEquals("Bangalore to Hyderabad route", response.getBody().getMessage());
    }

    @Test
    void testSearchFlights_DifferentUser() {
        // Given
        SearchResponse userResponse = new SearchResponse("U002", "DEL", "BOM", SearchPreference.CHEAPEST, 
            Arrays.asList(), "Different user search");
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(userResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U002", "DEL", "BOM", "CHEAPEST");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("U002", response.getBody().getUserId());
        assertEquals("Different user search", response.getBody().getMessage());
    }

    @Test
    void testSearchFlights_EmptyParameters() {
        // Given
        SearchResponse emptyResponse = new SearchResponse("", "", "", SearchPreference.NONE, 
            Arrays.asList(), "Empty parameters");
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(emptyResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "", "", "", "NONE");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("", response.getBody().getUserId());
        assertEquals("", response.getBody().getSource());
        assertEquals("", response.getBody().getDestination());
    }

    @Test
    void testSearchFlights_ServiceException() {
        // Given
        when(searchService.searchFlights(any(SearchRequest.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            searchController.searchFlights("U001", "DEL", "BOM", "CHEAPEST"));
        
        verify(searchService).searchFlights(any(SearchRequest.class));
    }

    @Test
    void testSearchFlights_AllSearchPreferences() {
        // Given
        SearchPreference[] preferences = {SearchPreference.CHEAPEST, SearchPreference.FASTEST, SearchPreference.NONE};
        
        // When & Then
        for (SearchPreference preference : preferences) {
            SearchResponse preferenceResponse = new SearchResponse("U001", "DEL", "BOM", preference, 
                Arrays.asList(), "Search completed for " + preference.name());
            when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(preferenceResponse);
            
            ResponseEntity<SearchResponse> response = searchController.searchFlights(
                "U001", "DEL", "BOM", preference.name());
            
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(preference, response.getBody().getPreference());
        }
        
        verify(searchService, times(3)).searchFlights(any(SearchRequest.class));
    }

    @Test
    void testSearchFlights_CrossOriginHeaders() {
        // Given
        when(searchService.searchFlights(any(SearchRequest.class))).thenReturn(searchResponse);

        // When
        ResponseEntity<SearchResponse> response = searchController.searchFlights(
            "U001", "DEL", "BOM", "CHEAPEST");

        // Then
        assertNotNull(response);
        // Note: CrossOrigin annotation is tested at integration level
        // This test verifies the controller method works correctly
    }
} 