package com.flightbooking.searchservice.repository;

import com.flightbooking.searchservice.model.Flight;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightRepository {
    List<Flight> findAll();
    Flight findByFlightId(String flightId);
    List<Flight> findBySourceAndDestination(String source, String destination);
    Flight save(Flight flight);
    void delete(String flightId);
} 