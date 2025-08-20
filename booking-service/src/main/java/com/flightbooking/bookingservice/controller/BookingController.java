package com.flightbooking.bookingservice.controller;

import com.flightbooking.bookingservice.dto.BookingRequest;
import com.flightbooking.bookingservice.dto.BookingResponse;
import com.flightbooking.bookingservice.enums.BookingStatus;
import com.flightbooking.bookingservice.model.Booking;
import com.flightbooking.bookingservice.model.Inventory;
import com.flightbooking.bookingservice.repository.InventoryRepository;
import com.flightbooking.bookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping("/flight/book")
    public ResponseEntity<BookingResponse> bookFlight(@RequestBody BookingRequest request) {
        System.err.println("=== CONTROLLER ENTRY ===");
        System.err.println("Received booking request: " + request);
        
        try {
            System.err.println("About to call bookingService.bookFlight()...");
            BookingResponse response = bookingService.bookFlight(request);
            System.err.println("BookingService call completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== CONTROLLER EXCEPTION ===");
            System.err.println("Exception in controller: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace(System.err);
            System.err.println("=== END CONTROLLER EXCEPTION ===");
            throw e; // Re-throw to maintain original behavior
        }
    }

    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatus> getBookingStatus(@PathVariable String bookingId) {
        BookingStatus status = bookingService.getBookingStatus(bookingId);
        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBookingDetails(@PathVariable String bookingId) {
        Booking booking = bookingService.getBookingDetails(bookingId);
        if (booking != null) {
            return ResponseEntity.ok(booking);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test/inventory")
    public ResponseEntity<String> testInventory() {
        System.err.println("=== TEST INVENTORY ENDPOINT ===");
        
        try {
            System.err.println("About to call inventoryRepository.findAll()...");
            List<Inventory> inventories = inventoryRepository.findAll();
            System.err.println("InventoryRepository call completed successfully. Found " + inventories.size() + " items");
            return ResponseEntity.ok("Success: Found " + inventories.size() + " inventory items");
        } catch (Exception e) {
            System.err.println("=== TEST INVENTORY EXCEPTION ===");
            System.err.println("Exception class: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace(System.err);
            System.err.println("=== END TEST INVENTORY EXCEPTION ===");
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
} 