package com.flightbooking.searchservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class FlightTest {

    @Test
    void testFlightCreation() {
        Flight flight = new Flight("F001", Arrays.asList("Monday", "Tuesday"), "DEL", "BOM", 299.99);
        
        assertEquals("F001", flight.getFlightId());
        assertEquals("Monday,Tuesday", flight.getDaysOfWeek());
        assertEquals("DEL", flight.getSource());
        assertEquals("BOM", flight.getDestination());
        assertEquals("299.99", flight.getCost());
    }

    @Test
    void testFlightCreationWithEmptyDays() {
        Flight flight = new Flight("F002", Arrays.asList(), "DEL", "BOM", 199.99);
        
        assertEquals("F002", flight.getFlightId());
        assertEquals("", flight.getDaysOfWeek());
        assertEquals("DEL", flight.getSource());
        assertEquals("BOM", flight.getDestination());
        assertEquals("199.99", flight.getCost());
    }

    @Test
    void testFlightCreationWithNullDays() {
        Flight flight = new Flight("F003", null, "DEL", "BOM", 399.99);
        
        assertEquals("F003", flight.getFlightId());
        assertEquals("", flight.getDaysOfWeek());
        assertEquals("DEL", flight.getSource());
        assertEquals("BOM", flight.getDestination());
        assertEquals("399.99", flight.getCost());
    }

    @Test
    void testDaysOfWeekAsList() {
        Flight flight = new Flight("F001", Arrays.asList("Monday", "Tuesday", "Wednesday"), "DEL", "BOM", 299.99);
        
        List<String> daysList = flight.getDaysOfWeekAsList();
        assertEquals(3, daysList.size());
        assertEquals("Monday", daysList.get(0));
        assertEquals("Tuesday", daysList.get(1));
        assertEquals("Wednesday", daysList.get(2));
    }

    @Test
    void testDaysOfWeekAsListWithEmptyString() {
        Flight flight = new Flight();
        flight.setDaysOfWeek("");
        
        List<String> daysList = flight.getDaysOfWeekAsList();
        assertTrue(daysList.isEmpty());
    }

    @Test
    void testDaysOfWeekAsListWithNullString() {
        Flight flight = new Flight();
        flight.setDaysOfWeek(null);
        
        List<String> daysList = flight.getDaysOfWeekAsList();
        assertTrue(daysList.isEmpty());
    }

    @Test
    void testSetDaysOfWeekFromList() {
        Flight flight = new Flight();
        List<String> daysList = Arrays.asList("Friday", "Saturday");
        
        flight.setDaysOfWeekFromList(daysList);
        assertEquals("Friday,Saturday", flight.getDaysOfWeek());
    }

    @Test
    void testSetDaysOfWeekFromListWithNull() {
        Flight flight = new Flight();
        flight.setDaysOfWeekFromList(null);
        assertEquals("", flight.getDaysOfWeek());
    }

    @Test
    void testCostAsDouble() {
        Flight flight = new Flight("F001", Arrays.asList("Monday"), "DEL", "BOM", 299.99);
        
        assertEquals(299.99, flight.getCostAsDouble(), 0.001);
    }

    @Test
    void testCostAsDoubleWithInvalidString() {
        Flight flight = new Flight();
        flight.setCost("invalid");
        
        assertEquals(0.0, flight.getCostAsDouble(), 0.001);
    }

    @Test
    void testCostAsDoubleWithNullString() {
        Flight flight = new Flight();
        flight.setCost(null);
        
        assertEquals(0.0, flight.getCostAsDouble(), 0.001);
    }

    @Test
    void testSetCostFromDouble() {
        Flight flight = new Flight();
        flight.setCostFromDouble(199.99);
        
        assertEquals("199.99", flight.getCost());
    }

    @Test
    void testSettersAndGetters() {
        Flight flight = new Flight();
        
        flight.setFlightId("F004");
        flight.setDaysOfWeek("Sunday");
        flight.setSource("HYD");
        flight.setDestination("BLR");
        flight.setCost("150.50");
        
        assertEquals("F004", flight.getFlightId());
        assertEquals("Sunday", flight.getDaysOfWeek());
        assertEquals("HYD", flight.getSource());
        assertEquals("BLR", flight.getDestination());
        assertEquals("150.50", flight.getCost());
    }

    @Test
    void testToString() {
        Flight flight = new Flight("F001", Arrays.asList("Monday"), "DEL", "BOM", 299.99);
        String flightString = flight.toString();
        
        assertTrue(flightString.contains("F001"));
        assertTrue(flightString.contains("Monday"));
        assertTrue(flightString.contains("DEL"));
        assertTrue(flightString.contains("BOM"));
        assertTrue(flightString.contains("299.99"));
    }

    @Test
    void testDefaultConstructor() {
        Flight flight = new Flight();
        
        assertNull(flight.getFlightId());
        assertNull(flight.getDaysOfWeek());
        assertNull(flight.getSource());
        assertNull(flight.getDestination());
        assertNull(flight.getCost());
    }
} 