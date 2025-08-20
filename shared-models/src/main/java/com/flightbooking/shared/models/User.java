package com.flightbooking.shared.models;

import java.time.LocalDateTime;

public class User {
    private String userId;
    private LocalDateTime memberSince;
    private String name;
    private double totalBookingValue;

    public User() {}

    public User(String userId, LocalDateTime memberSince, String name, double totalBookingValue) {
        this.userId = userId;
        this.memberSince = memberSince;
        this.name = name;
        this.totalBookingValue = totalBookingValue;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getMemberSince() { return memberSince; }
    public void setMemberSince(LocalDateTime memberSince) { this.memberSince = memberSince; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTotalBookingValue() { return totalBookingValue; }
    public void setTotalBookingValue(double totalBookingValue) { this.totalBookingValue = totalBookingValue; }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", memberSince=" + memberSince +
                ", name='" + name + '\'' +
                ", totalBookingValue=" + totalBookingValue +
                '}';
    }
} 