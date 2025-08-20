package com.flightbooking.searchservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.flightbooking.searchservice.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
class DynamoDBFlightRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @InjectMocks
    private DynamoDBFlightRepository flightRepository;

    private List<Flight> sampleFlights;

    @BeforeEach
    void setUp() {
        sampleFlights = Arrays.asList(
            new Flight("F001", Arrays.asList("Monday", "Tuesday"), "DEL", "BOM", 299.99),
            new Flight("F002", Arrays.asList("Wednesday", "Thursday"), "DEL", "BOM", 349.99),
            new Flight("F003", Arrays.asList("Monday", "Friday"), "DEL", "BLR", 199.99)
        );
    }
    
    private PaginatedScanList<Flight> createMockPaginatedScanList(List<Flight> flights) {
        // Create a mock that delegates to a real ArrayList to avoid stubbing issues
        PaginatedScanList<Flight> mockList = mock(PaginatedScanList.class);
        
        // Use lenient stubbing to avoid unnecessary stubbing errors
        lenient().when(mockList.size()).thenReturn(flights.size());
        lenient().when(mockList.isEmpty()).thenReturn(flights.isEmpty());
        lenient().when(mockList.iterator()).thenReturn(flights.iterator());
        lenient().when(mockList.get(anyInt())).thenAnswer(invocation -> {
            int index = invocation.getArgument(0);
            return flights.get(index);
        });
        lenient().when(mockList.toArray()).thenReturn(flights.toArray());
        lenient().when(mockList.toArray(any(Flight[].class))).thenAnswer(invocation -> {
            Flight[] array = invocation.getArgument(0);
            return flights.toArray(array);
        });
        lenient().when(mockList.contains(any())).thenAnswer(invocation -> {
            Object obj = invocation.getArgument(0);
            return flights.contains(obj);
        });
        lenient().when(mockList.containsAll(any())).thenAnswer(invocation -> {
            Collection<?> collection = invocation.getArgument(0);
            return flights.containsAll(collection);
        });
        lenient().when(mockList.indexOf(any())).thenAnswer(invocation -> {
            Object obj = invocation.getArgument(0);
            return flights.indexOf(obj);
        });
        lenient().when(mockList.lastIndexOf(any())).thenAnswer(invocation -> {
            Object obj = invocation.getArgument(0);
            return flights.indexOf(obj);
        });
        lenient().when(mockList.subList(anyInt(), anyInt())).thenAnswer(invocation -> {
            int fromIndex = invocation.getArgument(0);
            int toIndex = invocation.getArgument(1);
            return flights.subList(fromIndex, toIndex);
        });
        lenient().when(mockList.stream()).thenReturn(flights.stream());
        lenient().when(mockList.parallelStream()).thenReturn(flights.parallelStream());
        
        return mockList;
    }

    @Test
    void testFindAll() {
        // Given
        PaginatedScanList<Flight> mockPaginatedList = createMockPaginatedScanList(sampleFlights);
        when(dynamoDBMapper.scan(eq(Flight.class), any(DynamoDBScanExpression.class)))
            .thenReturn(mockPaginatedList);

        // When
        List<Flight> result = flightRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(dynamoDBMapper).scan(eq(Flight.class), any(DynamoDBScanExpression.class));
    }

    @Test
    void testFindByFlightId() {
        // Given
        String flightId = "F001";
        Flight expectedFlight = sampleFlights.get(0);
        when(dynamoDBMapper.load(Flight.class, flightId)).thenReturn(expectedFlight);

        // When
        Flight result = flightRepository.findByFlightId(flightId);

        // Then
        assertNotNull(result);
        assertEquals(flightId, result.getFlightId());
        verify(dynamoDBMapper).load(Flight.class, flightId);
    }

    @Test
    void testFindByFlightId_NotFound() {
        // Given
        String flightId = "NONEXISTENT";
        when(dynamoDBMapper.load(Flight.class, flightId)).thenReturn(null);

        // When
        Flight result = flightRepository.findByFlightId(flightId);

        // Then
        assertNull(result);
        verify(dynamoDBMapper).load(Flight.class, flightId);
    }

    @Test
    void testFindBySourceAndDestination() {
        // Given
        String source = "DEL";
        String destination = "BOM";
        List<Flight> expectedFlights = Arrays.asList(sampleFlights.get(0), sampleFlights.get(1));
        
        PaginatedScanList<Flight> mockPaginatedList = createMockPaginatedScanList(expectedFlights);
        when(dynamoDBMapper.scan(eq(Flight.class), any(DynamoDBScanExpression.class)))
            .thenReturn(mockPaginatedList);

        // When
        List<Flight> result = flightRepository.findBySourceAndDestination(source, destination);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f -> f.getSource().equals(source) && f.getDestination().equals(destination)));
        verify(dynamoDBMapper).scan(eq(Flight.class), any(DynamoDBScanExpression.class));
    }

    @Test
    void testFindBySourceAndDestination_NoResults() {
        // Given
        String source = "INVALID";
        String destination = "DESTINATION";
        
        PaginatedScanList<Flight> mockPaginatedList = createMockPaginatedScanList(Arrays.<Flight>asList());
        when(dynamoDBMapper.scan(eq(Flight.class), any(DynamoDBScanExpression.class)))
            .thenReturn(mockPaginatedList);

        // When
        List<Flight> result = flightRepository.findBySourceAndDestination(source, destination);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dynamoDBMapper).scan(eq(Flight.class), any(DynamoDBScanExpression.class));
    }

    @Test
    void testSave() {
        // Given
        Flight flightToSave = new Flight("F004", Arrays.asList("Friday"), "BOM", "HYD", 199.99);

        // When
        Flight result = flightRepository.save(flightToSave);

        // Then
        assertNotNull(result);
        assertEquals(flightToSave.getFlightId(), result.getFlightId());
        verify(dynamoDBMapper).save(flightToSave);
    }

    @Test
    void testDelete() {
        // Given
        String flightId = "F001";

        // When
        flightRepository.delete(flightId);

        // Then
        verify(dynamoDBMapper).delete(any(Flight.class));
    }

    @Test
    void testFindBySourceAndDestination_FilterExpression() {
        // Given
        String source = "DEL";
        String destination = "BOM";
        List<Flight> expectedFlights = Arrays.asList(sampleFlights.get(0), sampleFlights.get(1));
        
        PaginatedScanList<Flight> mockPaginatedList = createMockPaginatedScanList(expectedFlights);
        when(dynamoDBMapper.scan(eq(Flight.class), any(DynamoDBScanExpression.class)))
            .thenReturn(mockPaginatedList);

        // When
        flightRepository.findBySourceAndDestination(source, destination);

        // Then
        verify(dynamoDBMapper).scan(eq(Flight.class), argThat(scanExpression -> {
            String filterExpression = scanExpression.getFilterExpression();
            return filterExpression != null && 
                   filterExpression.contains("source = :source") && 
                   filterExpression.contains("destination = :destination");
        }));
    }

    @Test
    void testFindBySourceAndDestination_ExpressionAttributeValues() {
        // Given
        String source = "DEL";
        String destination = "BOM";
        
        PaginatedScanList<Flight> mockPaginatedList = createMockPaginatedScanList(sampleFlights);
        when(dynamoDBMapper.scan(eq(Flight.class), any(DynamoDBScanExpression.class)))
            .thenReturn(mockPaginatedList);
        // When
        flightRepository.findBySourceAndDestination(source, destination);

        // Then
        verify(dynamoDBMapper).scan(eq(Flight.class), argThat(scanExpression -> {
            var expressionValues = scanExpression.getExpressionAttributeValues();
            return expressionValues != null && 
                   expressionValues.containsKey(":source") && 
                   expressionValues.containsKey(":destination") &&
                   expressionValues.get(":source").getS().equals(source) &&
                   expressionValues.get(":destination").getS().equals(destination);
        }));
    }

    @Test
    void testFindAll_EmptyResult() {
        // Given
        PaginatedScanList<Flight> mockPaginatedList = createMockPaginatedScanList(Arrays.<Flight>asList());
        when(dynamoDBMapper.scan(eq(Flight.class), any(DynamoDBScanExpression.class)))
            .thenReturn(mockPaginatedList);

        // When
        List<Flight> result = flightRepository.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dynamoDBMapper).scan(eq(Flight.class), any(DynamoDBScanExpression.class));
    }

    @Test
    void testSave_NullFlight() {
        // Given
        Flight nullFlight = null;
        doThrow(new NullPointerException("Flight cannot be null"))
            .when(dynamoDBMapper).save(nullFlight);

        // When & Then
        assertThrows(NullPointerException.class, () -> flightRepository.save(nullFlight));
    }

    @Test
    void testDelete_EmptyFlightId() {
        // Given
        String emptyFlightId = "";

        // When
        flightRepository.delete(emptyFlightId);

        // Then
        verify(dynamoDBMapper).delete(any(Flight.class));
    }
} 