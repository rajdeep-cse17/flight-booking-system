package com.flightbooking.searchservice.dto;

import com.flightbooking.searchservice.enums.SearchPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchRequestTest {

    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        searchRequest = new SearchRequest("U001", "DEL", "BOM", SearchPreference.CHEAPEST);
    }

    @Test
    void testSearchRequestConstructor() {
        // Then
        assertEquals("U001", searchRequest.getUserId());
        assertEquals("DEL", searchRequest.getSource());
        assertEquals("BOM", searchRequest.getDestination());
        assertEquals(SearchPreference.CHEAPEST, searchRequest.getPreference());
    }

    @Test
    void testDefaultConstructor() {
        // When
        SearchRequest defaultRequest = new SearchRequest();

        // Then
        assertNotNull(defaultRequest);
        assertNull(defaultRequest.getUserId());
        assertNull(defaultRequest.getSource());
        assertNull(defaultRequest.getDestination());
        assertNull(defaultRequest.getPreference());
    }

    @Test
    void testSetAndGetUserId() {
        // Given
        String newUserId = "U002";

        // When
        searchRequest.setUserId(newUserId);

        // Then
        assertEquals(newUserId, searchRequest.getUserId());
    }

    @Test
    void testSetAndGetSource() {
        // Given
        String newSource = "BLR";

        // When
        searchRequest.setSource(newSource);

        // Then
        assertEquals(newSource, searchRequest.getSource());
    }

    @Test
    void testSetAndGetDestination() {
        // Given
        String newDestination = "HYD";

        // When
        searchRequest.setDestination(newDestination);

        // Then
        assertEquals(newDestination, searchRequest.getDestination());
    }

    @Test
    void testSetAndGetPreference() {
        // Given
        SearchPreference newPreference = SearchPreference.FASTEST;

        // When
        searchRequest.setPreference(newPreference);

        // Then
        assertEquals(newPreference, searchRequest.getPreference());
    }

    @Test
    void testSetUserId_EmptyString() {
        // When
        searchRequest.setUserId("");

        // Then
        assertEquals("", searchRequest.getUserId());
    }

    @Test
    void testSetUserId_Null() {
        // When
        searchRequest.setUserId(null);

        // Then
        assertNull(searchRequest.getUserId());
    }

    @Test
    void testSetSource_EmptyString() {
        // When
        searchRequest.setSource("");

        // Then
        assertEquals("", searchRequest.getSource());
    }

    @Test
    void testSetSource_Null() {
        // When
        searchRequest.setSource(null);

        // Then
        assertNull(searchRequest.getSource());
    }

    @Test
    void testSetDestination_EmptyString() {
        // When
        searchRequest.setDestination("");

        // Then
        assertEquals("", searchRequest.getDestination());
    }

    @Test
    void testSetDestination_Null() {
        // When
        searchRequest.setDestination(null);

        // Then
        assertNull(searchRequest.getDestination());
    }

    @Test
    void testSetPreference_Null() {
        // When
        searchRequest.setPreference(null);

        // Then
        assertNull(searchRequest.getPreference());
    }

    @Test
    void testAllSearchPreferences() {
        // Given
        SearchPreference[] preferences = {SearchPreference.CHEAPEST, SearchPreference.FASTEST, SearchPreference.NONE};

        // When & Then
        for (SearchPreference preference : preferences) {
            searchRequest.setPreference(preference);
            assertEquals(preference, searchRequest.getPreference());
        }
    }

    @Test
    void testSearchRequestEquality() {
        // Given
        SearchRequest request1 = new SearchRequest("U001", "DEL", "BOM", SearchPreference.CHEAPEST);
        SearchRequest request2 = new SearchRequest("U001", "DEL", "BOM", SearchPreference.CHEAPEST);

        // When & Then
        assertEquals(request1.getUserId(), request2.getUserId());
        assertEquals(request1.getSource(), request2.getSource());
        assertEquals(request1.getDestination(), request2.getDestination());
        assertEquals(request1.getPreference(), request2.getPreference());
    }

    @Test
    void testSearchRequestInequality() {
        // Given
        SearchRequest request1 = new SearchRequest("U001", "DEL", "BOM", SearchPreference.CHEAPEST);
        SearchRequest request2 = new SearchRequest("U002", "DEL", "BOM", SearchPreference.CHEAPEST);

        // When & Then
        assertNotEquals(request1.getUserId(), request2.getUserId());
    }

    @Test
    void testSearchRequestWithDifferentCities() {
        // Given
        String[] sources = {"DEL", "BLR", "BOM", "HYD", "CCU"};
        String[] destinations = {"BOM", "HYD", "DEL", "BLR", "BOM"};

        // When & Then
        for (int i = 0; i < sources.length; i++) {
            searchRequest.setSource(sources[i]);
            searchRequest.setDestination(destinations[i]);
            assertEquals(sources[i], searchRequest.getSource());
            assertEquals(destinations[i], searchRequest.getDestination());
        }
    }

    @Test
    void testSearchRequestWithDifferentUsers() {
        // Given
        String[] userIds = {"U001", "U002", "U003", "ADMIN", "GUEST"};

        // When & Then
        for (String userId : userIds) {
            searchRequest.setUserId(userId);
            assertEquals(userId, searchRequest.getUserId());
        }
    }

    @Test
    void testSearchRequestEdgeCases() {
        // Given
        SearchRequest edgeRequest = new SearchRequest();

        // When & Then
        edgeRequest.setUserId("VERY_LONG_USER_ID_123456789");
        assertEquals("VERY_LONG_USER_ID_123456789", edgeRequest.getUserId());

        edgeRequest.setSource("A");
        assertEquals("A", edgeRequest.getSource());

        edgeRequest.setDestination("ZZZ");
        assertEquals("ZZZ", edgeRequest.getDestination());

        edgeRequest.setPreference(SearchPreference.NONE);
        assertEquals(SearchPreference.NONE, edgeRequest.getPreference());
    }

    @Test
    void testSearchRequestToString() {
        // When
        String result = searchRequest.toString();

        // Then
        assertNotNull(result);
        // Note: toString() implementation may vary, so we just check it's not null
    }
} 