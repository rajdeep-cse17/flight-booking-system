package com.flightbooking.bookingservice.repository;

import com.flightbooking.bookingservice.model.Inventory;
import java.util.List;

public interface InventoryRepository {
    
    Inventory findByFlightIdAndDate(String flightId, String date);
    
    List<Inventory> findByFlightId(String flightId);
    
    List<Inventory> findByDate(String date);
    
    List<Inventory> findByNumberOfSeatsLeftLessThan(int seats);
    
    Inventory save(Inventory inventory);
    
    void deleteById(String id);
    
    Inventory findById(String id);
    
    List<Inventory> findAll();
} 