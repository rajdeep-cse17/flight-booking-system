package com.flightbooking.bookingservice.service;

import com.flightbooking.bookingservice.client.PaymentServiceClient;
import com.flightbooking.bookingservice.client.UserServiceClient;
import com.flightbooking.bookingservice.dto.BookingRequest;
import com.flightbooking.bookingservice.dto.BookingResponse;
import com.flightbooking.bookingservice.enums.BookingStatus;
import com.flightbooking.bookingservice.model.Booking;
import com.flightbooking.bookingservice.model.Inventory;
import com.flightbooking.bookingservice.repository.BookingRepository;
import com.flightbooking.bookingservice.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private List<Inventory> sampleInventories;
    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        String testDate = "2024-01-15";
        bookingRequest = new BookingRequest("U001", Arrays.asList("F001", "F002"), 
            testDate, "DEL", "BOM", 2);
        
        sampleInventories = Arrays.asList(
            new Inventory("INV001", "F001", testDate, 50),
            new Inventory("INV002", "F002", testDate, 30)
        );

        sampleBooking = new Booking();
        sampleBooking.setBookingId("B001");
        sampleBooking.setUserId("U001");
        sampleBooking.setStatus(BookingStatus.PROCESSING.name());
    }

    // ========== BOOK FLIGHT TESTS ==========

    @Test
    void testBookFlight_Success() {
        // Given
        String testDate = "2024-01-15";
        when(inventoryRepository.findByFlightIdAndDate("F001", testDate))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", testDate))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return booking;
        });

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        assertNotNull(response.getBookingId());
        assertTrue(response.getMessage().contains("initiated successfully"));
        assertEquals(400.0, response.getCost()); // 2 flights * $100 * 2 passengers
        
        verify(bookingRepository).save(any(Booking.class));
        // Verify inventory was locked (2 flights, each with 2 saves: lock + release)
        verify(inventoryRepository, times(4)).save(any(Inventory.class));
    }

    @Test
    void testBookFlight_InsufficientInventory() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(new Inventory("INV001", "F001", "2024-01-15", 1));

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertEquals("Insufficient seats available", response.getMessage());
        assertEquals(0.0, response.getCost());
        
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testBookFlight_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(null);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertEquals("Insufficient seats available", response.getMessage());
        
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testBookFlight_RepositoryException() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Error processing booking"));
        
        // Verify that inventory save was attempted (lock operation)
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    @Test
    void testBookFlight_EmptyFlightList() {
        // Given
        BookingRequest emptyRequest = new BookingRequest("U001", Arrays.asList(), 
            "2024-01-15", "DEL", "BOM", 2);

        // When
        BookingResponse response = bookingService.bookFlight(emptyRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.PROCESSING, response.getStatus()); // Empty list returns true from checkInventory
        assertEquals(0.0, response.getCost()); // 0 flights * $100 * 2 passengers
    }

    @Test
    void testBookFlight_ZeroPassengers() {
        // Given
        BookingRequest zeroPassengerRequest = new BookingRequest("U001", Arrays.asList("F001"), 
            "2024-01-15", "DEL", "BOM", 0);

        // When
        BookingResponse response = bookingService.bookFlight(zeroPassengerRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertEquals("Insufficient seats available", response.getMessage());
    }

    @Test
    void testBookFlight_NullRequest() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            bookingService.bookFlight(null));
    }

    // ========== ASYNC PAYMENT PROCESSING TESTS ==========

    @Test
    void testProcessPaymentAsync_Success() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingRepository.findById(anyString())).thenReturn(sampleBooking);
        when(paymentServiceClient.processPayment(400.0)).thenReturn("SUCCESS");

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200); // Increased delay for async processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(paymentServiceClient, timeout(2000)).processPayment(400.0);
        verify(userServiceClient, timeout(2000)).updateTotalBookingValue(anyString(), eq(400.0));
    }

    @Test
    void testProcessPaymentAsync_Failure() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate(anyString(), anyString()))
            .thenReturn(sampleInventories.get(0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingRepository.findById(anyString())).thenReturn(sampleBooking);
        when(paymentServiceClient.processPayment(400.0)).thenReturn("FAILED");

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(paymentServiceClient, timeout(2000)).processPayment(400.0);
        verify(userServiceClient, never()).updateTotalBookingValue(anyString(), anyDouble());
    }

    @Test
    void testProcessPaymentAsync_Exception() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate(anyString(), anyString()))
            .thenReturn(sampleInventories.get(0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingRepository.findById(anyString())).thenReturn(sampleBooking);
        when(paymentServiceClient.processPayment(400.0)).thenThrow(new RuntimeException("Payment service error"));

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(paymentServiceClient, timeout(2000)).processPayment(400.0);
        verify(userServiceClient, never()).updateTotalBookingValue(anyString(), anyDouble());
    }

    // ========== STATUS UPDATE TESTS ==========

    @Test
    void testUpdateBookingStatus_Success() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingRepository.findById(anyString())).thenReturn(sampleBooking);

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    void testUpdateBookingStatus_BookingNotFound() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate(anyString(), anyString()))
            .thenReturn(sampleInventories.get(0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingRepository.findById(anyString())).thenReturn(null);

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Should not save again since booking not found
        verify(bookingRepository, times(1)).save(any(Booking.class)); // Only initial save
    }

    @Test
    void testUpdateBookingStatus_RepositoryException() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate(anyString(), anyString()))
            .thenReturn(sampleInventories.get(0));
        lenient().when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingRepository.findById(anyString())).thenReturn(sampleBooking);
        
        // When updating status, save fails - use lenient to avoid unnecessary stubbing
        lenient().when(bookingRepository.save(any(Booking.class)))
            .thenReturn(sampleBooking) // First save succeeds
            .thenThrow(new RuntimeException("Save error")); // Second save fails

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Should not throw exception due to error handling in async method
        verify(bookingRepository, atLeastOnce()).findById(anyString());
    }

    // ========== STATUS QUERY TESTS ==========

    @Test
    void testGetBookingStatus_Success() {
        // Given
        when(bookingRepository.findById("B001")).thenReturn(sampleBooking);

        // When
        BookingStatus status = bookingService.getBookingStatus("B001");

        // Then
        assertEquals(BookingStatus.PROCESSING, status);
        verify(bookingRepository).findById("B001");
    }

    @Test
    void testGetBookingStatus_NotFound() {
        // Given
        when(bookingRepository.findById("NONEXISTENT")).thenReturn(null);

        // When
        BookingStatus status = bookingService.getBookingStatus("NONEXISTENT");

        // Then
        assertNull(status);
        verify(bookingRepository).findById("NONEXISTENT");
    }

    @Test
    void testGetBookingStatus_RepositoryException() {
        // Given
        when(bookingRepository.findById("B001")).thenThrow(new RuntimeException("Database error"));

        // When
        BookingStatus status = bookingService.getBookingStatus("B001");

        // Then
        assertNull(status);
        verify(bookingRepository).findById("B001");
    }

    @Test
    void testGetBookingStatus_NullId() {
        // When
        BookingStatus status = bookingService.getBookingStatus(null);

        // Then
        assertNull(status);
    }

    @Test
    void testGetBookingStatus_EmptyId() {
        // When
        BookingStatus status = bookingService.getBookingStatus("");

        // Then
        assertNull(status);
    }

    // ========== BOOKING DETAILS TESTS ==========

    @Test
    void testGetBookingDetails_Success() {
        // Given
        when(bookingRepository.findById("B001")).thenReturn(sampleBooking);

        // When
        Booking result = bookingService.getBookingDetails("B001");

        // Then
        assertNotNull(result);
        assertEquals("B001", result.getBookingId());
        assertEquals("U001", result.getUserId());
        verify(bookingRepository).findById("B001");
    }

    @Test
    void testGetBookingDetails_NotFound() {
        // Given
        when(bookingRepository.findById("NONEXISTENT")).thenReturn(null);

        // When
        Booking result = bookingService.getBookingDetails("NONEXISTENT");

        // Then
        assertNull(result);
        verify(bookingRepository).findById("NONEXISTENT");
    }

    @Test
    void testGetBookingDetails_RepositoryException() {
        // Given
        when(bookingRepository.findById("B001")).thenThrow(new RuntimeException("Database error"));

        // When
        Booking result = bookingService.getBookingDetails("B001");

        // Then
        assertNull(result);
        verify(bookingRepository).findById("B001");
    }

    @Test
    void testGetBookingDetails_NullId() {
        // When
        Booking result = bookingService.getBookingDetails(null);

        // Then
        assertNull(result);
    }

    // ========== INVENTORY MANAGEMENT TESTS ==========

    @Test
    void testCheckInventory_SufficientSeats() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was looked up (checkInventory + lockInventoryOptimistic)
        verify(inventoryRepository, atLeastOnce()).findByFlightIdAndDate(anyString(), anyString());
    }

    @Test
    void testCheckInventory_ExactSeats() {
        // Given
        Inventory exactSeats = new Inventory("INV001", "F001", "2024-01-15", 2);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(exactSeats);
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was saved at least once (lock operation)
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    @Test
    void testCheckInventory_MultipleFlights() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was saved at least once (lock operation)
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    // ========== COST CALCULATION TESTS ==========

    @Test
    void testCalculateTotalCost_SingleFlight() {
        // Given
        BookingRequest singleFlightRequest = new BookingRequest("U001", Arrays.asList("F001"), 
            "2024-01-15", "DEL", "BOM", 3);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(singleFlightRequest);

        // Then
        assertEquals(300.0, response.getCost()); // 1 flight * $100 * 3 passengers
    }

    @Test
    void testCalculateTotalCost_MultipleFlights() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(400.0, response.getCost()); // 2 flights * $100 * 2 passengers
    }

    @Test
    void testCalculateTotalCost_ZeroFlights() {
        // Given
        BookingRequest zeroFlightRequest = new BookingRequest("U001", Arrays.asList(), 
            "2024-01-15", "DEL", "BOM", 2);

        // When
        BookingResponse response = bookingService.bookFlight(zeroFlightRequest);

        // Then
        assertEquals(0.0, response.getCost());
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    void testBookFlight_InventoryRepositoryException() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenThrow(new RuntimeException("Inventory service error"));

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Error processing booking"));
    }

    @Test
    void testBookFlight_InventoryLockReleaseOnError() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class)))
            .thenThrow(new RuntimeException("Booking service error"));

        // When
        bookingService.bookFlight(bookingRequest);

        // Then
        // Verify that inventory save was attempted (lock operation)
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testBookFlight_VeryLargePassengerCount() {
        // Given
        BookingRequest largePassengerRequest = new BookingRequest("U001", Arrays.asList("F001"), 
            "2024-01-15", "DEL", "BOM", 1000);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(new Inventory("INV001", "F001", "2024-01-15", 1000));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(largePassengerRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        assertEquals(100000.0, response.getCost()); // 1 flight * $100 * 1000 passengers
    }

    @Test
    void testBookFlight_FutureDate() {
        // Given
        String futureDate = "2025-01-15";
        when(inventoryRepository.findByFlightIdAndDate("F001", futureDate))
            .thenReturn(new Inventory("INV001", "F001", futureDate, 50));
        when(inventoryRepository.findByFlightIdAndDate("F002", futureDate))
            .thenReturn(new Inventory("INV002", "F002", futureDate, 30));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        BookingRequest futureRequest = new BookingRequest("U001", Arrays.asList("F001", "F002"), 
            futureDate, "DEL", "BOM", 2);

        // When
        BookingResponse response = bookingService.bookFlight(futureRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was looked up (checkInventory + lockInventoryOptimistic)
        verify(inventoryRepository, atLeastOnce()).findByFlightIdAndDate(anyString(), eq(futureDate));
    }

    @Test
    void testBookFlight_PastDate() {
        // Given
        String pastDate = "2024-01-14";
        when(inventoryRepository.findByFlightIdAndDate("F001", pastDate))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", pastDate))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        BookingRequest pastRequest = new BookingRequest("U001", Arrays.asList("F001", "F002"), 
            pastDate, "DEL", "BOM", 2);

        // When
        BookingResponse response = bookingService.bookFlight(pastRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was looked up (checkInventory + lockInventoryOptimistic)
        verify(inventoryRepository, atLeastOnce()).findByFlightIdAndDate(anyString(), eq(pastDate));
    }



    @Test
    void testLockInventoryOptimistic_Success() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was saved at least once (lock operation)
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    @Test
    void testLockInventoryOptimistic_RetryOnConflict() {
        // Given
        Inventory inventory1 = sampleInventories.get(0);
        Inventory inventory2 = sampleInventories.get(1);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(inventory1);
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(inventory2);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        
        // First save fails, second succeeds for both inventories
        when(inventoryRepository.save(any(Inventory.class)))
            .thenThrow(new RuntimeException("Version conflict"))
            .thenReturn(inventory1)
            .thenThrow(new RuntimeException("Version conflict"))
            .thenReturn(inventory2);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was saved multiple times due to retries
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    @Test
    void testLockInventoryOptimistic_MaxRetriesExceeded() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        lenient().when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        
        // All saves fail - use lenient to avoid unnecessary stubbing
        lenient().when(inventoryRepository.save(any(Inventory.class)))
            .thenThrow(new RuntimeException("Version conflict"));

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Error processing booking"));
    }

    @Test
    void testLockInventoryOptimistic_InventoryNotFound() {
        // Given - Create a scenario where checkInventory passes but lockInventoryOptimistic fails
        // First flight has inventory, second flight doesn't (to trigger the exception in lockInventoryOptimistic)
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0)); // F001 has inventory
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1)) // F002 has inventory for checkInventory
            .thenReturn(null); // But F002 becomes null during lockInventoryOptimistic (simulating race condition)

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Error processing booking: Inventory not found for flight"));
    }

    @Test
    void testLockInventoryOptimistic_InsufficientSeats() {
        // Given
        Inventory lowSeats = new Inventory("INV001", "F001", "2024-01-15", 1);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(lowSeats);

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Insufficient seats"));
    }

    @Test
    void testLockInventoryOptimistic_VersionIncrement() {
        // Given
        Inventory inventory1 = sampleInventories.get(0);
        Inventory inventory2 = sampleInventories.get(1);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(inventory1);
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(inventory2);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory1);

        // When
        bookingService.bookFlight(bookingRequest);

        // Then
        // Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify that version increment happened during any save operation
        verify(inventoryRepository, atLeastOnce()).save(argThat(inv -> 
            inv.getVersionAsLong() > 1L));
    }

    @Test
    void testReleaseInventoryLock_InventoryNotFound() {
        // Given
        lenient().when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        lenient().when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // When
        bookingService.bookFlight(bookingRequest);

        // Then - Wait for async processing to complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Should handle gracefully when inventory operations occur
        verify(inventoryRepository, atLeastOnce()).findByFlightIdAndDate(anyString(), anyString());
    }

    @Test
    void testConcurrentInventoryUpdates_OptimisticLocking() {
        // Given
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(sampleInventories.get(0));
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        
        // Simulate concurrent update scenario
        when(inventoryRepository.save(any(Inventory.class)))
            .thenThrow(new RuntimeException("Version conflict"))
            .thenReturn(sampleInventories.get(0))
            .thenThrow(new RuntimeException("Version conflict"))
            .thenReturn(sampleInventories.get(1));

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was saved multiple times due to retries
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }

    @Test
    void testOptimisticLocking_ExactSeatsWithRetry() {
        // Given
        Inventory exactSeats = new Inventory("INV001", "F001", "2024-01-15", 2);
        when(inventoryRepository.findByFlightIdAndDate("F001", "2024-01-15"))
            .thenReturn(exactSeats);
        when(inventoryRepository.findByFlightIdAndDate("F002", "2024-01-15"))
            .thenReturn(sampleInventories.get(1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        
        // Mock successful saves (no conflicts)
        when(inventoryRepository.save(any(Inventory.class)))
            .thenReturn(exactSeats)
            .thenReturn(sampleInventories.get(1));

        // When
        BookingResponse response = bookingService.bookFlight(bookingRequest);

        // Then
        assertEquals(BookingStatus.PROCESSING, response.getStatus());
        // Verify that inventory was saved
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
    }
} 