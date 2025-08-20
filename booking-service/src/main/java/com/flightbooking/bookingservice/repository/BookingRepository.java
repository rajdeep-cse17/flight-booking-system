package com.flightbooking.bookingservice.repository;

import com.flightbooking.bookingservice.model.Booking;
import java.util.List;

public interface BookingRepository {
    
    List<Booking> findByUserId(String userId);
    
    List<Booking> findByStatus(String status);
    
    List<Booking> findByUserIdAndStatus(String userId, String status);
    
    Booking save(Booking booking);
    
    void deleteById(String id);
    
    Booking findById(String id);
    
    List<Booking> findAll();
} 