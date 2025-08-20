package com.flightbooking.bookingservice.service;

import com.flightbooking.bookingservice.client.PaymentServiceClient;
import com.flightbooking.bookingservice.client.UserServiceClient;
import com.flightbooking.bookingservice.dto.BookingRequest;
import com.flightbooking.bookingservice.dto.BookingResponse;
import com.flightbooking.bookingservice.enums.BookingStatus;
import com.flightbooking.bookingservice.exception.OptimisticLockingException;
import com.flightbooking.bookingservice.model.Booking;
import com.flightbooking.bookingservice.model.Inventory;
import com.flightbooking.bookingservice.repository.BookingRepository;
import com.flightbooking.bookingservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 100;

    public BookingResponse bookFlight(BookingRequest request) {
        System.err.println("=== BOOKING SERVICE ENTRY ===");
        System.err.println("Request received: " + request);
        
        try {
            System.err.println("About to check inventory...");
            // 1. Check inventory first
            if (!checkInventory(request.getFlightIds(), request.getDate(), request.getNumberOfPassengers())) {
                System.err.println("Inventory check failed - insufficient seats");
                return new BookingResponse(null, BookingStatus.FAILED, "Insufficient seats available", 0.0);
            }
            System.err.println("Inventory check passed");

            System.err.println("About to lock inventory...");
            // 2. Decrease available seats and put a lock using optimistic locking
            lockInventoryOptimistic(request.getFlightIds(), request.getDate(), request.getNumberOfPassengers());
            System.err.println("Inventory locked successfully");

            System.err.println("About to create booking...");
            // 3. Create booking ID with PROCESSING status
            String bookingId = UUID.randomUUID().toString();
            double totalCost = calculateTotalCost(request.getFlightIds(), request.getNumberOfPassengers());
            
            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setUserId(request.getUserId());
            booking.setFlightIdsAsList(request.getFlightIds());
            booking.setDate(request.getDate());
            booking.setSource(request.getSource());
            booking.setDestination(request.getDestination());
            booking.setStatus(BookingStatus.PROCESSING.name());
            booking.setCostAsDouble(totalCost);
            
            System.err.println("About to save booking to repository...");
            bookingRepository.save(booking);
            System.err.println("Booking saved successfully");

            // 4. Start async payment processing
            CompletableFuture.runAsync(() -> 
                processPaymentAsync(bookingId, totalCost, request.getFlightIds(), request.getDate(), request.getNumberOfPassengers()));

            return new BookingResponse(bookingId, BookingStatus.PROCESSING, 
                "Booking initiated successfully. Use booking ID to check status.", totalCost);

        } catch (Exception e) {
            // Log the full error details
            System.err.println("=== FULL ERROR DETAILS ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            System.err.println("Stack trace:");
            e.printStackTrace(System.err);
            System.err.println("=== END ERROR DETAILS ===");
            
            // Release inventory lock in case of any error
            releaseInventoryLock(request.getFlightIds(), request.getDate(), request.getNumberOfPassengers());
            return new BookingResponse(null, BookingStatus.FAILED, "Error processing booking: " + e.getMessage(), 0.0);
        }
    }

    private void lockInventoryOptimistic(List<String> flightIds, String date, int passengers) {
        for (String flightId : flightIds) {
            boolean locked = false;
            int retries = 0;
            
            while (!locked && retries < MAX_RETRIES) {
                try {
                    // Get current inventory with version
                    Inventory inventory = inventoryRepository.findByFlightIdAndDate(flightId, date);
                    
                    if (inventory == null) {
                        throw new RuntimeException("Inventory not found for flight: " + flightId);
                    }
                    
                    if (inventory.getNumberOfSeatsLeftAsInt() < passengers) {
                        throw new RuntimeException("Insufficient seats for flight: " + flightId);
                    }
                    
                    // Store current version for optimistic locking
                    Long currentVersion = inventory.getVersionAsLong();
                    
                    // Update seats
                    inventory.setNumberOfSeatsLeftAsInt(inventory.getNumberOfSeatsLeftAsInt() - passengers);
                    inventory.setVersionAsLong(currentVersion + 1);
                    
                    // Try to save with version check
                    try {
                        inventoryRepository.save(inventory);
                        locked = true;
                    } catch (Exception e) {
                        // If save fails, it might be due to version conflict
                        // We'll retry in the next iteration
                        retries++;
                        if (retries >= MAX_RETRIES) {
                            throw new OptimisticLockingException("Failed to lock inventory for flight: " + flightId + " after " + MAX_RETRIES + " retries");
                        }
                        
                        // Small delay before retry to reduce contention
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted while retrying inventory lock", ie);
                        }
                    }
                    
                } catch (RuntimeException e) {
                    // Don't retry for business logic errors
                    throw e;
                }
            }
            
            if (!locked) {
                throw new OptimisticLockingException("Failed to lock inventory for flight: " + flightId + " after " + MAX_RETRIES + " retries");
            }
        }
    }

    private void processPaymentAsync(String bookingId, double totalCost, 
                                   List<String> flightIds, String date, int passengers) {
        try {
            // Process payment by calling external Payment Service
            String paymentStatus = paymentServiceClient.processPayment(totalCost);

            // Update booking status based on payment result
            if ("SUCCESS".equals(paymentStatus)) {
                updateBookingStatus(bookingId, BookingStatus.SUCCESS);
                // Update user's total booking value by calling external User Service
                userServiceClient.updateTotalBookingValue(bookingId, totalCost);
            } else {
                updateBookingStatus(bookingId, BookingStatus.FAILED);
            }

            // Release inventory lock
            releaseInventoryLock(flightIds, date, passengers);

        } catch (Exception e) {
            // Handle any errors during async processing
            updateBookingStatus(bookingId, BookingStatus.FAILED);
            releaseInventoryLock(flightIds, date, passengers);
        }
    }

    private void updateBookingStatus(String bookingId, BookingStatus status) {
        try {
            Booking booking = bookingRepository.findById(bookingId);
            if (booking != null) {
                booking.setStatus(status.name());
                bookingRepository.save(booking);
            }
        } catch (Exception e) {
            // Log error but don't throw - this is async processing
            System.err.println("Error updating booking status: " + e.getMessage());
        }
    }

    public BookingStatus getBookingStatus(String bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId);
            return booking != null ? BookingStatus.valueOf(booking.getStatus()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Booking getBookingDetails(String bookingId) {
        try {
            return bookingRepository.findById(bookingId);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkInventory(List<String> flightIds, String date, int passengers) {
        System.err.println("=== CHECKING INVENTORY ===");
        System.err.println("Flight IDs: " + flightIds);
        System.err.println("Date: " + date);
        System.err.println("Passengers: " + passengers);
        
        for (String flightId : flightIds) {
            System.err.println("Checking inventory for flight: " + flightId);
            Inventory inventory = inventoryRepository.findByFlightIdAndDate(flightId, date);
            System.err.println("Found inventory: " + (inventory != null ? "YES" : "NO"));
            if (inventory != null) {
                System.err.println("Inventory details: " + inventory);
                System.err.println("Seats left: " + inventory.getNumberOfSeatsLeftAsInt());
            }
            if (inventory == null || inventory.getNumberOfSeatsLeftAsInt() < passengers) {
                System.err.println("Insufficient inventory for flight: " + flightId);
                return false;
            }
        }
        System.err.println("=== INVENTORY CHECK COMPLETE ===");
        return true;
    }

    private void releaseInventoryLock(List<String> flightIds, String date, int passengers) {
        for (String flightId : flightIds) {
            boolean released = false;
            int retries = 0;
            
            while (!released && retries < MAX_RETRIES) {
                try {
                    Inventory inventory = inventoryRepository.findByFlightIdAndDate(flightId, date);
                    if (inventory != null) {
                        // Store current version for optimistic locking
                        Long currentVersion = inventory.getVersionAsLong();
                        
                        // Restore seats
                        inventory.setNumberOfSeatsLeftAsInt(inventory.getNumberOfSeatsLeftAsInt() + passengers);
                        inventory.setVersionAsLong(currentVersion + 1);
                        
                        try {
                            inventoryRepository.save(inventory);
                            released = true;
                        } catch (Exception e) {
                            // If save fails, retry
                            retries++;
                            if (retries >= MAX_RETRIES) {
                                System.err.println("Failed to release inventory lock for flight: " + flightId + " after " + MAX_RETRIES + " retries");
                                break;
                            }
                            
                            try {
                                Thread.sleep(RETRY_DELAY_MS);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } else {
                        // Inventory not found, consider it released
                        released = true;
                    }
                } catch (Exception e) {
                    System.err.println("Error releasing inventory lock for flight: " + e.getMessage());
                    break;
                }
            }
        }
    }

    private double calculateTotalCost(List<String> flightIds, int passengers) {
        // This would typically involve getting flight costs from the search service
        // For now, using a placeholder calculation
        return flightIds.size() * 100.0 * passengers; // $100 per flight per passenger
    }
} 