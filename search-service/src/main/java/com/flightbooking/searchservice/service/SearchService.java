package com.flightbooking.searchservice.service;

import com.flightbooking.searchservice.dto.SearchRequest;
import com.flightbooking.searchservice.dto.SearchResponse;
import com.flightbooking.searchservice.enums.SearchPreference;
import com.flightbooking.searchservice.model.Flight;
import com.flightbooking.searchservice.repository.FlightRepository;
import com.flightbooking.searchservice.util.FlightFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "flight_search:";
    private static final int CACHE_TTL = 300; // 5 minutes

    public SearchResponse searchFlights(SearchRequest request) {
        logger.info("Starting flight search for request: {}", request);
        
        try {
            // Check cache first
            String cacheKey = CACHE_PREFIX + request.getSource() + ":" + request.getDestination() + ":" + request.getPreference();
            logger.debug("Checking cache with key: {}", cacheKey);
            
            SearchResponse cachedResponse = null;
            try {
                cachedResponse = (SearchResponse) redisTemplate.opsForValue().get(cacheKey);
                if (cachedResponse != null) {
                    logger.info("Cache hit, returning cached response");
                    return cachedResponse;
                }
            } catch (Exception e) {
                logger.warn("Redis cache operation failed, continuing without cache: {}", e.getMessage());
            }

            logger.debug("Cache miss, fetching from database");
            
            // Get flights from database and apply search logic
            List<Flight> allFlights;
            try {
                allFlights = flightRepository.findAll();
                logger.info("Retrieved {} flights from database", allFlights.size());
            } catch (Exception e) {
                logger.error("Failed to retrieve flights from database: {}", e.getMessage(), e);
                throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
            }

            if (allFlights == null || allFlights.isEmpty()) {
                logger.warn("No flights found in database");
                return createEmptyResponse(request, "No flights available");
            }

            FlightFinder flightFinder = new FlightFinder();
            
            // Build flight graph for efficient route finding
            for (Flight flight : allFlights) {
                flightFinder.addFlight(flight);
            }
            logger.debug("Flight graph built with {} flights", allFlights.size());

            List<Flight> searchResults;
            try {
                switch (request.getPreference()) {
                    case CHEAPEST:
                        searchResults = flightFinder.findTopCheapestRoutes(request.getSource(), request.getDestination(), 10);
                        break;
                    case FASTEST:
                        searchResults = flightFinder.findTopShortestRoutes(request.getSource(), request.getDestination(), 10);
                        break;
                    case NONE:
                    default:
                        List<List<Flight>> routes = flightFinder.findAllRoutes(request.getSource(), request.getDestination(), 5);
                        searchResults = routes.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                        break;
                }
                logger.info("Search completed, found {} results", searchResults.size());
            } catch (Exception e) {
                logger.error("Flight search algorithm failed: {}", e.getMessage(), e);
                throw new RuntimeException("Search algorithm failed: " + e.getMessage(), e);
            }

            // Create response - convert to shared Flight type
            List<com.flightbooking.shared.models.Flight> sharedFlights;
            try {
                sharedFlights = searchResults.stream()
                    .map(flight -> new com.flightbooking.shared.models.Flight(
                        flight.getFlightId(), flight.getDaysOfWeekAsList(), flight.getSource(), 
                        flight.getDestination(), flight.getCostAsDouble()))
                    .collect(Collectors.toList());
                logger.debug("Converted {} flights to shared model", sharedFlights.size());
            } catch (Exception e) {
                logger.error("Failed to convert flights to shared model: {}", e.getMessage(), e);
                throw new RuntimeException("Model conversion failed: " + e.getMessage(), e);
            }
                
            SearchResponse response = new SearchResponse(
                request.getUserId(),
                request.getSource(),
                request.getDestination(),
                request.getPreference(),
                sharedFlights,
                "Search completed successfully"
            );

            // Cache the response
            try {
                redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.SECONDS);
                logger.debug("Response cached successfully");
            } catch (Exception e) {
                logger.warn("Failed to cache response: {}", e.getMessage());
                // Don't fail the request if caching fails
            }

            logger.info("Flight search completed successfully for {} to {}", request.getSource(), request.getDestination());
            return response;

        } catch (Exception e) {
            logger.error("Unexpected error in flight search: {}", e.getMessage(), e);
            throw new RuntimeException("Flight search failed: " + e.getMessage(), e);
        }
    }

    private SearchResponse createEmptyResponse(SearchRequest request, String message) {
        return new SearchResponse(
            request.getUserId(),
            request.getSource(),
            request.getDestination(),
            request.getPreference(),
            List.of(),
            message
        );
    }
} 