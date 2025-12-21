package com.apartmentcommunity.booking.service;

import com.apartmentcommunity.booking.exception.BookingValidationException;
import com.apartmentcommunity.booking.model.AmenityType;
import com.apartmentcommunity.booking.model.Booking;
import com.apartmentcommunity.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final NotificationPublisher notificationPublisher;
    
    // Pattern for time slot format: "HH:mm-HH:mm" (e.g., "18:00-19:00")
    private static final Pattern TIME_SLOT_PATTERN = Pattern.compile("^(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})$");

    @Autowired
    public BookingService(BookingRepository bookingRepository, NotificationPublisher notificationPublisher) {
        this.bookingRepository = bookingRepository;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional
    public Booking createBooking(Long amenityId, Long userId, AmenityType amenityType, 
                                 LocalDate bookingDate, String timeSlot,
                                 LocalDateTime slotStart, LocalDateTime slotEnd) {
        // Step 1: Global time validation
        validateBookingTime(bookingDate, timeSlot, slotEnd, amenityType);
        
        // Step 2: Amenity-specific validation
        validateAmenityConstraints(amenityId, amenityType, bookingDate, timeSlot);
        
        // Step 3: Create and save booking
        try {
            Booking booking = new Booking(amenityId, userId, amenityType, bookingDate, timeSlot, slotStart, slotEnd);
            booking = bookingRepository.save(booking);
            
            // Publish event asynchronously
            notificationPublisher.publishBookingCreated(booking);
            
            return booking;
        } catch (DataIntegrityViolationException e) {
            // Unique constraint violation - slot already booked
            throw new BookingValidationException("Slot is already booked");
        }
    }

    /**
     * Global time validation: Reject past bookings and bookings where slot end time has passed
     */
    private void validateBookingTime(LocalDate bookingDate, String timeSlot, LocalDateTime slotEnd, AmenityType amenityType) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // Reject if booking date is in the past
        if (bookingDate.isBefore(today)) {
            throw new BookingValidationException("Cannot book for a past date");
        }
        
        // For slot-based amenities (GYM, TENNIS, SWIMMING, PARKING)
        if (amenityType == AmenityType.GYM || amenityType == AmenityType.TENNIS || 
            amenityType == AmenityType.SWIMMING || amenityType == AmenityType.PARKING) {
            // If booking date is today, check if slot end time has passed (<= current time)
            if (bookingDate.equals(today) && !slotEnd.isAfter(now)) {
                throw new BookingValidationException("Cannot book a time slot that has already passed");
            }
        }
        
        // For amenities without time slots (BBQ, HALL)
        if (amenityType == AmenityType.BBQ || amenityType == AmenityType.HALL) {
            // Reject if booking date is before today (allow same-day bookings)
            if (bookingDate.isBefore(today)) {
                throw new BookingValidationException("Cannot book for a past date");
            }
        }
    }

    /**
     * Validate amenity-specific constraints
     */
    private void validateAmenityConstraints(Long amenityId, AmenityType amenityType, 
                                           LocalDate bookingDate, String timeSlot) {
        switch (amenityType) {
            case GYM:
            case SWIMMING:
                validateSlotBasedBooking(amenityId, bookingDate, timeSlot, 10, "Gym/Swimming Pool");
                break;
            case TENNIS:
                validateSlotBasedBooking(amenityId, bookingDate, timeSlot, 2, "Tennis Court");
                break;
            case PARKING:
                validateSlotBasedBooking(amenityId, bookingDate, timeSlot, 10, "Guest Car Parking");
                break;
            case HALL:
                validateDayBasedBooking(amenityId, bookingDate, 1, "Community Hall");
                break;
            case BBQ:
                validateDayBasedBooking(amenityId, bookingDate, 4, "BBQ Area");
                break;
            default:
                throw new BookingValidationException("Unknown amenity type: " + amenityType);
        }
    }

    /**
     * Validate slot-based bookings (GYM, TENNIS, SWIMMING)
     */
    private void validateSlotBasedBooking(Long amenityId, LocalDate bookingDate, String timeSlot, 
                                         int maxCapacity, String amenityName) {
        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            throw new BookingValidationException("Time slot is required for " + amenityName);
        }
        
        // Validate time slot format
        if (!isValidTimeSlotFormat(timeSlot)) {
            throw new BookingValidationException("Invalid time slot format. Expected format: HH:mm-HH:mm (e.g., 18:00-19:00)");
        }
        
        long currentCount = bookingRepository.countByAmenityIdAndBookingDateAndTimeSlot(
            amenityId, bookingDate, timeSlot);
        
        if (currentCount >= maxCapacity) {
            throw new BookingValidationException(amenityName + " slot is fully booked for this time");
        }
    }

    /**
     * Validate day-based bookings (HALL, BBQ)
     */
    private void validateDayBasedBooking(Long amenityId, LocalDate bookingDate, int maxCapacity, String amenityName) {
        long currentCount = bookingRepository.countByAmenityIdAndBookingDate(amenityId, bookingDate);
        
        if (currentCount >= maxCapacity) {
            if (maxCapacity == 1) {
                throw new BookingValidationException(amenityName + " is already booked for this date");
            } else {
                throw new BookingValidationException(amenityName + " has reached maximum capacity (" + maxCapacity + ") for this date");
            }
        }
    }

    /**
     * Validate time slot format: "HH:mm-HH:mm"
     */
    private boolean isValidTimeSlotFormat(String timeSlot) {
        if (timeSlot == null) {
            return false;
        }
        Matcher matcher = TIME_SLOT_PATTERN.matcher(timeSlot.trim());
        if (!matcher.matches()) {
            return false;
        }
        
        // Validate that times are valid (hours 0-23, minutes 0-59)
        try {
            int startHour = Integer.parseInt(matcher.group(1));
            int startMinute = Integer.parseInt(matcher.group(2));
            int endHour = Integer.parseInt(matcher.group(3));
            int endMinute = Integer.parseInt(matcher.group(4));
            
            if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59 ||
                endHour < 0 || endHour > 23 || endMinute < 0 || endMinute > 59) {
                return false;
            }
            
            // Validate that end time is after start time
            LocalTime start = LocalTime.of(startHour, startMinute);
            LocalTime end = LocalTime.of(endHour, endMinute);
            return end.isAfter(start);
        } catch (Exception e) {
            return false;
        }
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to cancel this booking");
        }
        
        bookingRepository.delete(booking);
        
        // Publish event asynchronously
        notificationPublisher.publishBookingCancelled(booking);
    }

    public List<LocalTime> getAvailableSlots(Long amenityId, LocalDate date) {
        // Generate hourly slots from 09:00 to 17:00
        List<LocalTime> allSlots = IntStream.range(9, 17)
            .mapToObj(hour -> LocalTime.of(hour, 0))
            .collect(Collectors.toList());

        // Get existing bookings for this amenity and date
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        List<Booking> existingBookings = bookingRepository.findBookingsByAmenityAndDate(amenityId, dateStart, dateEnd);

        // Extract booked time slots
        List<LocalTime> bookedSlots = existingBookings.stream()
            .map(booking -> booking.getSlotStart().toLocalTime())
            .collect(Collectors.toList());

        // Return available slots
        return allSlots.stream()
            .filter(slot -> !bookedSlots.contains(slot))
            .collect(Collectors.toList());
    }

    public List<Booking> getBookingsForDate(Long amenityId, LocalDate date) {
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        return bookingRepository.findBookingsByAmenityAndDate(amenityId, dateStart, dateEnd);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByAmenity(Long amenityId) {
        return bookingRepository.findByAmenityId(amenityId);
    }

    // Legacy method for backward compatibility - will be deprecated
    @Deprecated
    @Transactional
    public Booking createBooking(Long amenityId, Long userId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        // This method is kept for backward compatibility but should not be used
        // Extract date and time slot from slotStart/slotEnd
        LocalDate bookingDate = slotStart.toLocalDate();
        String timeSlot = formatTimeSlot(slotStart.toLocalTime(), slotEnd.toLocalTime());
        
        // Default to GYM if amenity type is not provided (legacy behavior)
        return createBooking(amenityId, userId, AmenityType.GYM, bookingDate, timeSlot, slotStart, slotEnd);
    }

    /**
     * Format LocalTime start and end into "HH:mm-HH:mm" format
     */
    private String formatTimeSlot(LocalTime start, LocalTime end) {
        return String.format("%02d:%02d-%02d:%02d", 
            start.getHour(), start.getMinute(), 
            end.getHour(), end.getMinute());
    }
}
