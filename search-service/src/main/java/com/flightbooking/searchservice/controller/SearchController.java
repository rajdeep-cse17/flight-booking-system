package com.flightbooking.searchservice.controller;

import com.flightbooking.searchservice.dto.SearchRequest;
import com.flightbooking.searchservice.dto.SearchResponse;
import com.flightbooking.searchservice.enums.SearchPreference;
import com.flightbooking.searchservice.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/flights")
    public ResponseEntity<SearchResponse> searchFlights(
            @RequestParam String userId,
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(defaultValue = "cheapest") String preference) {
        
        // Convert string preference to enum
        SearchPreference searchPreference;
        try {
            searchPreference = SearchPreference.fromString(preference);
        } catch (Exception e) {
            searchPreference = SearchPreference.CHEAPEST; // Default fallback
        }
        
        SearchRequest request = new SearchRequest(userId, source, destination, searchPreference);
        SearchResponse response = searchService.searchFlights(request);
        return ResponseEntity.ok(response);
    }
} 