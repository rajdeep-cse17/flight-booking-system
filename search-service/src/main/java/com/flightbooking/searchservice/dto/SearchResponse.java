package com.flightbooking.searchservice.dto;

import com.flightbooking.searchservice.enums.SearchPreference;
import com.flightbooking.shared.models.Flight;
import java.util.List;

public class SearchResponse {
    private String userId;
    private String source;
    private String destination;
    private SearchPreference preference;
    private List<Flight> flights;
    private String message;

    public SearchResponse() {}

    public SearchResponse(String userId, String source, String destination, SearchPreference preference, 
                        List<Flight> flights, String message) {
        this.userId = userId;
        this.source = source;
        this.destination = destination;
        this.preference = preference;
        this.flights = flights;
        this.message = message;
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

    public List<Flight> getFlights() { return flights; }
    public void setFlights(List<Flight> flights) { this.flights = flights; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 