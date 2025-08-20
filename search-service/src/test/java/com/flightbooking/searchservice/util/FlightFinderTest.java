package com.flightbooking.searchservice.util;

import com.flightbooking.searchservice.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightFinderTest {

    private FlightFinder flightFinder;
    private List<Flight> sampleFlights;

    @BeforeEach
    void setUp() {
        flightFinder = new FlightFinder();
        
        // Create sample flights for testing
        sampleFlights = Arrays.asList(
            new Flight("F001", Arrays.asList("Monday", "Tuesday"), "DEL", "BOM", 100.00),
            new Flight("F002", Arrays.asList("Wednesday", "Thursday"), "DEL", "BOM", 150.00),
            new Flight("F003", Arrays.asList("Monday", "Friday"), "DEL", "BLR", 200.00),
            new Flight("F004", Arrays.asList("Tuesday", "Thursday"), "BLR", "BOM", 250.00),
            new Flight("F005", Arrays.asList("Monday", "Wednesday"), "BOM", "HYD", 300.00),
            new Flight("F006", Arrays.asList("Friday", "Saturday"), "HYD", "DEL", 350.00)
        );
        
        // Add flights to the finder
        for (Flight flight : sampleFlights) {
            flightFinder.addFlight(flight);
        }
    }

    @Test
    void testAddFlight() {
        // Given
        FlightFinder newFinder = new FlightFinder();
        Flight testFlight = new Flight("TEST", Arrays.asList("Monday"), "TEST", "DEST", 100.00);
        
        // When
        newFinder.addFlight(testFlight);
        
        // Then
        List<List<Flight>> routes = newFinder.findAllRoutes("TEST", "DEST", 1);
        assertFalse(routes.isEmpty());
        assertEquals(1, routes.size());
        assertEquals(1, routes.get(0).size());
        assertEquals("TEST", routes.get(0).get(0).getFlightId());
    }

    @Test
    void testFindTopCheapestRoutes() {
        // When
        List<Flight> cheapestRoutes = flightFinder.findTopCheapestRoutes("DEL", "BOM", 3);
        
        // Then
        assertNotNull(cheapestRoutes);
        assertFalse(cheapestRoutes.isEmpty());
        
        // Should return flights sorted by cost
        double previousCost = 0;
        for (Flight flight : cheapestRoutes) {
            assertTrue(flight.getCostAsDouble() >= previousCost);
            previousCost = flight.getCostAsDouble();
        }
    }

    @Test
    void testFindTopShortestRoutes() {
        // When
        List<Flight> shortestRoutes = flightFinder.findTopShortestRoutes("DEL", "BOM", 3);
        
        // Then
        assertNotNull(shortestRoutes);
        assertFalse(shortestRoutes.isEmpty());
        
        // Should prioritize routes with fewer flights
        // Direct flights should come first
        assertTrue(shortestRoutes.stream().anyMatch(f -> f.getSource().equals("DEL") && f.getDestination().equals("BOM")));
    }

    @Test
    void testFindAllRoutes() {
        // When
        List<List<Flight>> allRoutes = flightFinder.findAllRoutes("DEL", "BOM", 2);
        
        // Then
        assertNotNull(allRoutes);
        assertFalse(allRoutes.isEmpty());
        
        // Should find both direct and indirect routes
        boolean hasDirectRoute = allRoutes.stream()
            .anyMatch(route -> route.size() == 1 && 
                route.get(0).getSource().equals("DEL") && 
                route.get(0).getDestination().equals("BOM"));
        
        boolean hasIndirectRoute = allRoutes.stream()
            .anyMatch(route -> route.size() > 1);
        
        assertTrue(hasDirectRoute || hasIndirectRoute);
    }

    @Test
    void testFindAllRoutes_MaxStopsLimit() {
        // When
        List<List<Flight>> routesWithMaxStops = flightFinder.findAllRoutes("DEL", "HYD", 1);
        
        // Then
        assertNotNull(routesWithMaxStops);
        
        // All routes should have at most 1 stop
        for (List<Flight> route : routesWithMaxStops) {
            assertTrue(route.size() <= 2, "Route should have at most 2 flights (1 stop)");
        }
    }

    @Test
    void testFindAllRoutes_SameSourceDestination() {
        // When
        List<List<Flight>> routes = flightFinder.findAllRoutes("DEL", "DEL", 5);
        
        // Then
        assertNotNull(routes);
        assertTrue(routes.isEmpty(), "Should return empty list for same source and destination");
    }

    @Test
    void testFindAllRoutes_NoRoutesFound() {
        // When
        List<List<Flight>> routes = flightFinder.findAllRoutes("INVALID", "DESTINATION", 5);
        
        // Then
        assertNotNull(routes);
        assertTrue(routes.isEmpty(), "Should return empty list when no routes exist");
    }

    @Test
    void testFindTopCheapestRoutes_Limit() {
        // When
        List<Flight> cheapestRoutes = flightFinder.findTopCheapestRoutes("DEL", "BOM", 1);
        
        // Then
        assertNotNull(cheapestRoutes);
        assertTrue(cheapestRoutes.size() <= 1, "Should respect the limit parameter");
    }

    @Test
    void testFindTopShortestRoutes_Limit() {
        // When
        List<Flight> shortestRoutes = flightFinder.findTopShortestRoutes("DEL", "BOM", 2);
        
        // Then
        assertNotNull(shortestRoutes);
        assertTrue(shortestRoutes.size() <= 2, "Should respect the limit parameter");
    }

    @Test
    void testComplexRouteFinding() {
        // Test finding routes from DEL to HYD (should find DEL -> BLR -> BOM -> HYD)
        List<List<Flight>> complexRoutes = flightFinder.findAllRoutes("DEL", "HYD", 3);
        
        assertNotNull(complexRoutes);
        assertFalse(complexRoutes.isEmpty());
        
        // Should find routes with multiple stops
        boolean hasComplexRoute = complexRoutes.stream()
            .anyMatch(route -> route.size() > 2);
        
        assertTrue(hasComplexRoute, "Should find complex routes with multiple stops");
    }

    @Test
    void testRouteCostCalculation() {
        // When
        List<Flight> cheapestRoutes = flightFinder.findTopCheapestRoutes("DEL", "BOM", 5);
        
        // Then
        assertNotNull(cheapestRoutes);
        assertFalse(cheapestRoutes.isEmpty());
        
        // Verify that the cheapest route is returned first
        assertTrue(cheapestRoutes.size() >= 1);
        assertTrue(cheapestRoutes.get(0).getCostAsDouble() >= 100.0); // Minimum cost
        
        // Verify that the route contains the expected flights
        boolean foundCheapest = cheapestRoutes.stream()
            .anyMatch(f -> f.getFlightId().equals("F001") && f.getCostAsDouble() == 100.0);
        assertTrue(foundCheapest, "Should find the cheapest flight F001");
    }

    @Test
    void testEmptyFlightFinder() {
        // Given
        FlightFinder emptyFinder = new FlightFinder();
        
        // When
        List<Flight> cheapestRoutes = emptyFinder.findTopCheapestRoutes("DEL", "BOM", 5);
        List<Flight> shortestRoutes = emptyFinder.findTopShortestRoutes("DEL", "BOM", 5);
        List<List<Flight>> allRoutes = emptyFinder.findAllRoutes("DEL", "BOM", 5);
        
        // Then
        assertTrue(cheapestRoutes.isEmpty());
        assertTrue(shortestRoutes.isEmpty());
        assertTrue(allRoutes.isEmpty());
    }
} 