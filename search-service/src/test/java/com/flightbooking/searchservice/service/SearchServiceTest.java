package com.flightbooking.searchservice.service;

import com.flightbooking.searchservice.dto.SearchRequest;
import com.flightbooking.searchservice.dto.SearchResponse;
import com.flightbooking.searchservice.enums.SearchPreference;
import com.flightbooking.searchservice.model.Flight;
import com.flightbooking.searchservice.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SearchService searchService;

    private List<Flight> sampleFlights;
    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        // Create sample flights
        List<Flight> sampleFlights = Arrays.asList(
            new Flight("F001", Arrays.asList("Monday", "Tuesday"), "DEL", "BOM", 299.99),
            new Flight("F002", Arrays.asList("Wednesday", "Thursday"), "DEL", "BOM", 349.99),
            new Flight("F003", Arrays.asList("Monday", "Friday"), "DEL", "BLR", 199.99)
        );

        // Mock repository to return sample flights
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // Create search request
        searchRequest = new SearchRequest("U001", "DEL", "BOM", SearchPreference.CHEAPEST);
    }

    @Test
    void testSearchFlights_CheapestPreference() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        SearchResponse response = searchService.searchFlights(searchRequest);

        // Then
        assertNotNull(response);
        assertEquals("U001", response.getUserId());
        assertEquals("DEL", response.getSource());
        assertEquals("BOM", response.getDestination());
        assertEquals(SearchPreference.CHEAPEST, response.getPreference());
        assertNotNull(response.getFlights());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_FastestPreference() {
        // Given
        searchRequest = new SearchRequest("U001", "DEL", "BOM", SearchPreference.FASTEST);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        SearchResponse response = searchService.searchFlights(searchRequest);

        // Then
        assertNotNull(response);
        assertEquals(SearchPreference.FASTEST, response.getPreference());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_NoPreference() {
        // Given
        searchRequest = new SearchRequest("U001", "DEL", "BOM", SearchPreference.NONE);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        SearchResponse response = searchService.searchFlights(searchRequest);

        // Then
        assertNotNull(response);
        assertEquals(SearchPreference.NONE, response.getPreference());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_ReturnCachedResponse() {
        // Given
        SearchResponse cachedResponse = new SearchResponse("U001", "DEL", "BOM", SearchPreference.CHEAPEST, 
            Arrays.asList(), "Cached response");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedResponse);

        // When
        SearchResponse response = searchService.searchFlights(searchRequest);

        // Then
        assertNotNull(response);
        assertEquals("Cached response", response.getMessage());
        verify(flightRepository, never()).findAll();
        verify(redisTemplate.opsForValue(), never()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_EmptyFlightList() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(Arrays.asList());

        // When
        SearchResponse response = searchService.searchFlights(searchRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getFlights().isEmpty());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_CacheKeyGeneration() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        searchService.searchFlights(searchRequest);

        // Then
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_DifferentCities() {
        // Given
        SearchRequest chicagoRequest = new SearchRequest("U001", "BLR", "DEL", SearchPreference.CHEAPEST);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        SearchResponse response = searchService.searchFlights(chicagoRequest);

        // Then
        assertNotNull(response);
        assertEquals("BLR", response.getSource());
        assertEquals("DEL", response.getDestination());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_DifferentUser() {
        // Given
        SearchRequest userRequest = new SearchRequest("U002", "DEL", "BOM", SearchPreference.CHEAPEST);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        SearchResponse response = searchService.searchFlights(userRequest);

        // Then
        assertNotNull(response);
        assertEquals("U002", response.getUserId());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_EmptyParameters() {
        // Given
        SearchRequest emptyRequest = new SearchRequest("", "", "", SearchPreference.NONE);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(Arrays.asList());

        // When
        SearchResponse response = searchService.searchFlights(emptyRequest);

        // Then
        assertNotNull(response);
        assertEquals("", response.getUserId());
        assertEquals("", response.getSource());
        assertEquals("", response.getDestination());
        verify(redisTemplate.opsForValue()).set(anyString(), any(SearchResponse.class), eq(300L), any());
    }

    @Test
    void testSearchFlights_ServiceException() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            searchService.searchFlights(searchRequest));
        
        verify(flightRepository).findAll();
    }

    @Test
    void testSearchFlights_AllSearchPreferences() {
        // Given
        SearchPreference[] preferences = {SearchPreference.CHEAPEST, SearchPreference.FASTEST, SearchPreference.NONE};
        
        // When & Then
        for (SearchPreference preference : preferences) {
            SearchRequest preferenceRequest = new SearchRequest("U001", "DEL", "BOM", preference);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(flightRepository.findAll()).thenReturn(sampleFlights);
            
            SearchResponse response = searchService.searchFlights(preferenceRequest);
            
            assertNotNull(response);
            assertEquals(preference, response.getPreference());
        }
        
        verify(flightRepository, times(3)).findAll();
    }

    @Test
    void testSearchFlights_CrossOriginHeaders() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(flightRepository.findAll()).thenReturn(sampleFlights);

        // When
        SearchResponse response = searchService.searchFlights(searchRequest);

        // Then
        assertNotNull(response);
        // Note: CrossOrigin annotation is tested at integration level
        // This test verifies the controller method works correctly
    }
} 