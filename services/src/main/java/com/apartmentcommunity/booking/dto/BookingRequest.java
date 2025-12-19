package com.apartmentcommunity.booking.dto;

import com.apartmentcommunity.booking.model.AmenityType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingRequest {
    private Long amenityId;
    private AmenityType amenityType;
    private LocalDate bookingDate;
    private String timeSlot;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;

    public BookingRequest() {}

    public BookingRequest(Long amenityId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        this.amenityId = amenityId;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
    }

    public Long getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(Long amenityId) {
        this.amenityId = amenityId;
    }

    public AmenityType getAmenityType() {
        return amenityType;
    }

    public void setAmenityType(AmenityType amenityType) {
        this.amenityType = amenityType;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
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
}
