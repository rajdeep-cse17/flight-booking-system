package com.flightbooking.searchservice.dto;

import com.flightbooking.searchservice.enums.SearchPreference;

public class SearchRequest {
    private String userId;
    private String source;
    private String destination;
    private SearchPreference preference;

    public SearchRequest() {}

    public SearchRequest(String userId, String source, String destination, SearchPreference preference) {
        this.userId = userId;
        this.source = source;
        this.destination = destination;
        this.preference = preference;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public SearchPreference getPreference() { return preference; }
    public void setPreference(SearchPreference preference) { this.preference = preference; }
} 