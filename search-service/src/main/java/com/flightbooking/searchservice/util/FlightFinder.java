package com.flightbooking.searchservice.util;

import com.flightbooking.searchservice.model.Flight;
import java.util.*;

public class FlightFinder {
    private List<Flight> flights;
    private Map<String, Set<String>> flightGraph;
    private Map<String, List<Flight>> flightMap;
    
    public FlightFinder() {
        this.flights = new ArrayList<>();
        this.flightGraph = new HashMap<>();
        this.flightMap = new HashMap<>();
    }
    
    public void addFlight(Flight flight) {
        flights.add(flight);
        
        // Build adjacency list for graph representation
        flightGraph.computeIfAbsent(flight.getSource(), k -> new HashSet<>())
                  .add(flight.getDestination());
        
        // Build direct mapping for quick lookups
        String key = flight.getSource() + "->" + flight.getDestination();
        flightMap.computeIfAbsent(key, k -> new ArrayList<>()).add(flight);
    }
    
    public List<Flight> findTopCheapestRoutes(String source, String destination, int limit) {
        if (source.equals(destination)) {
            return new ArrayList<>();
        }
        
        List<RouteResult> allRoutes = new ArrayList<RouteResult>();
        
        // Find all routes with different numbers of stops
        for (int maxStops = 0; maxStops <= 5; maxStops++) {
            List<List<Flight>> routes = findAllRoutes(source, destination, maxStops);
            for (List<Flight> route : routes) {
                double totalCost = route.stream().mapToDouble(Flight::getCostAsDouble).sum();
                allRoutes.add(new RouteResult(route, totalCost));
            }
        }
        
        // Sort by total cost and return top results
        allRoutes.sort(Comparator.comparingDouble(RouteResult::getTotalCost));
        return allRoutes.stream()
                       .limit(limit)
                       .map(RouteResult::getRoute)
                       .flatMap(List::stream)
                       .distinct()
                       .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<Flight> findTopShortestRoutes(String source, String destination, int limit) {
        if (source.equals(destination)) {
            return new ArrayList<>();
        }
        
        List<RouteResult> allRoutes = new ArrayList<RouteResult>();
        
        // Find all routes with different numbers of stops
        for (int maxStops = 0; maxStops <= 5; maxStops++) {
            List<List<Flight>> routes = findAllRoutes(source, destination, maxStops);
            for (List<Flight> route : routes) {
                double totalCost = route.stream().mapToDouble(Flight::getCostAsDouble).sum();
                allRoutes.add(new RouteResult(route, totalCost));
            }
        }
        
                // Sort by number of flights first, then by cost for tie-breaking
        allRoutes.sort(Comparator.<RouteResult>comparingInt(route -> route.getRoute().size())
                               .thenComparingDouble(route -> route.getTotalCost()));
        
        return allRoutes.stream()
                       .limit(limit)
                       .map(RouteResult::getRoute)
                       .flatMap(List::stream)
                       .distinct()
                       .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<List<Flight>> findAllRoutes(String source, String destination, int maxStops) {
        if (source.equals(destination)) {
            return new ArrayList<>();
        }
        
        List<List<Flight>> routes = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        dfs(source, destination, new ArrayList<>(), routes, visited, 0, maxStops);
        
        // Return routes directly
        return routes;
    }
    
    private void dfs(String current, String destination, List<Flight> path, 
                    List<List<Flight>> routes, Set<String> visited, 
                    int stops, int maxStops) {
        if (stops > maxStops) {
            return;
        }
        
        if (current.equals(destination)) {
            if (!path.isEmpty() || stops == 0) {
                routes.add(new ArrayList<>(path));
            }
            return;
        }
        
        visited.add(current);
        
        Set<String> destinations = flightGraph.getOrDefault(current, new HashSet<>());
        for (String nextDest : destinations) {
            if (!visited.contains(nextDest)) {
                String key = current + "->" + nextDest;
                List<Flight> flights = flightMap.get(key);
                if (flights != null) {
                    for (Flight flight : flights) {
                        path.add(flight);
                        dfs(nextDest, destination, path, routes, visited, stops + 1, maxStops);
                        path.remove(path.size() - 1);
                    }
                }
            }
        }
        
        visited.remove(current);
    }
    
    private static class RouteResult {
        private List<Flight> route;
        private double totalCost;
        
        public RouteResult(List<Flight> route, double totalCost) {
            this.route = route;
            this.totalCost = totalCost;
        }
        
        public List<Flight> getRoute() { return route; }
        public double getTotalCost() { return totalCost; }
    }
} 