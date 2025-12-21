package com.apartmentcommunity.booking.repository;

import com.apartmentcommunity.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.amenityId = :amenityId " +
           "AND b.slotStart < :slotEnd AND b.slotEnd > :slotStart")
    List<Booking> findOverlappingBookings(
        @Param("amenityId") Long amenityId,
        @Param("slotStart") LocalDateTime slotStart,
        @Param("slotEnd") LocalDateTime slotEnd
    );
    
    @Query("SELECT b FROM Booking b WHERE b.amenityId = :amenityId " +
           "AND b.slotStart >= :dateStart AND b.slotStart < :dateEnd")
    List<Booking> findBookingsByAmenityAndDate(
        @Param("amenityId") Long amenityId,
        @Param("dateStart") LocalDateTime dateStart,
        @Param("dateEnd") LocalDateTime dateEnd
    );
    
    List<Booking> findByAmenityId(Long amenityId);

    // Count bookings for slot-based amenities (GYM, TENNIS, SWIMMING)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.amenityId = :amenityId " +
           "AND b.bookingDate = :bookingDate AND b.timeSlot = :timeSlot")
    long countByAmenityIdAndBookingDateAndTimeSlot(
        @Param("amenityId") Long amenityId,
        @Param("bookingDate") LocalDate bookingDate,
        @Param("timeSlot") String timeSlot
    );

    // Count bookings for day-based amenities (HALL)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.amenityId = :amenityId " +
           "AND b.bookingDate = :bookingDate")
    long countByAmenityIdAndBookingDate(
        @Param("amenityId") Long amenityId,
        @Param("bookingDate") LocalDate bookingDate
    );

    // Count bookings for BBQ (max 4 per day)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.amenityId = :amenityId " +
           "AND b.bookingDate = :bookingDate")
    long countByAmenityIdAndBookingDateForBBQ(
        @Param("amenityId") Long amenityId,
        @Param("bookingDate") LocalDate bookingDate
    );
}
