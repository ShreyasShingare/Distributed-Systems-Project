package com.apartmentcommunity.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amenity_id", nullable = false)
    private Long amenityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "amenity_type", nullable = false)
    private AmenityType amenityType;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "time_slot", length = 20)
    private String timeSlot;

    @Column(name = "slot_start", nullable = false)
    private LocalDateTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private LocalDateTime slotEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Booking() {
    }

    public Booking(Long amenityId, Long userId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        this.amenityId = amenityId;
        this.userId = userId;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
        // Set default values for new fields (backward compatibility)
        this.amenityType = AmenityType.GYM;
        this.bookingDate = slotStart != null ? slotStart.toLocalDate() : LocalDate.now();
        this.timeSlot = (slotStart != null && slotEnd != null) ? String.format("%02d:%02d-%02d:%02d",
                slotStart.toLocalTime().getHour(), slotStart.toLocalTime().getMinute(),
                slotEnd.toLocalTime().getHour(), slotEnd.toLocalTime().getMinute()) : null;
    }

    public Booking(Long amenityId, Long userId, AmenityType amenityType, LocalDate bookingDate,
            String timeSlot, LocalDateTime slotStart, LocalDateTime slotEnd) {
        this.amenityId = amenityId;
        this.userId = userId;
        this.amenityType = amenityType;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
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
}
