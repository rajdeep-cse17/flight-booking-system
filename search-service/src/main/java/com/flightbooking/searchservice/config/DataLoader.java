package com.flightbooking.searchservice.config;

import com.flightbooking.searchservice.model.Flight;
import com.flightbooking.searchservice.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

// Temporarily disabled for manual testing
// @Component
@Profile({"default", "dev", "prod"}) // Only run in default, dev, or prod profiles, not in test profiles
public class DataLoader implements CommandLineRunner {

    @Autowired
    private FlightRepository flightRepository;

    @Override
    public void run(String... args) throws Exception {
        // Load sample flight data
        List<Flight> sampleFlights = Arrays.asList(
            new Flight("F001", Arrays.asList("Monday", "Tuesday", "Wednesday"), "DEL", "BOM", 299.99),
            new Flight("F002", Arrays.asList("Thursday", "Friday", "Saturday"), "DEL", "BOM", 349.99),
            new Flight("F003", Arrays.asList("Monday", "Wednesday", "Friday"), "DEL", "BLR", 199.99),
            new Flight("F004", Arrays.asList("Tuesday", "Thursday", "Saturday"), "BOM", "BLR", 399.99),
            new Flight("F005", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday"), "BLR", "HYD", 249.99)
        );

        for (Flight flight : sampleFlights) {
            flightRepository.save(flight);
        }
        
        System.out.println("Sample flight data loaded successfully!");
    }
} 