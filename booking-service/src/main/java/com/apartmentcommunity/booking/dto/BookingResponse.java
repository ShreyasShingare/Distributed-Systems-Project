package com.apartmentcommunity.booking.dto;

import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private Long amenityId;
    private Long userId;
    private String userName;
    private String userFullName;
    private String flatNo;
    private String contactNumber;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private LocalDateTime createdAt;

    public BookingResponse() {}

    public BookingResponse(Long id, Long amenityId, Long userId, String userName, 
                          String userFullName, String flatNo, String contactNumber,
                          LocalDateTime slotStart, LocalDateTime slotEnd, LocalDateTime createdAt) {
        this.id = id;
        this.amenityId = amenityId;
        this.userId = userId;
        this.userName = userName;
        this.userFullName = userFullName;
        this.flatNo = flatNo;
        this.contactNumber = contactNumber;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(Long amenityId) {
        this.amenityId = amenityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public void setFlatNo(String flatNo) {
        this.flatNo = flatNo;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDateTime getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(LocalDateTime slotStart) {
        this.slotStart = slotStart;
    }

    public LocalDateTime getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(LocalDateTime slotEnd) {
        this.slotEnd = slotEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

