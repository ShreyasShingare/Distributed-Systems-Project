package com.apartmentcommunity.booking.controller;

import com.apartmentcommunity.booking.dto.BookingRequest;
import com.apartmentcommunity.booking.dto.BookingResponse;
import com.apartmentcommunity.booking.exception.BookingValidationException;
import com.apartmentcommunity.booking.model.AmenityType;
import com.apartmentcommunity.booking.model.Booking;
import com.apartmentcommunity.booking.service.BookingService;
import com.apartmentcommunity.booking.service.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final UserServiceClient userServiceClient;
    private final BookingService bookingService;

    @Autowired
    public BookingController(UserServiceClient userServiceClient, BookingService bookingService) {
        this.userServiceClient = userServiceClient;
        this.bookingService = bookingService;
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getAvailability(
            @RequestParam("amenityId") Long amenityId,
            @RequestParam("date") String date) {
        try {
            System.out.println("=== Availability Request ===");
            System.out.println("AmenityId: " + amenityId);
            System.out.println("Date: " + date);

            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            System.out.println("Parsed date: " + localDate);

            List<Booking> bookings = bookingService.getBookingsForDate(amenityId, localDate);
            System.out.println("Bookings count: " + bookings.size());

            Map<String, Object> response = new HashMap<>();
            response.put("amenityId", amenityId);
            response.put("date", date);

            // Check if this is a day-based amenity (HALL or BBQ)
            boolean isDayBased = false;
            if (!bookings.isEmpty()) {
                // If bookings exist, check the amenity type from the first booking
                AmenityType firstBookingType = bookings.get(0).getAmenityType();
                isDayBased = firstBookingType == AmenityType.HALL || firstBookingType == AmenityType.BBQ;
            } else {
                // If no bookings exist, check all bookings for this amenity across all dates
                // to determine the amenity type
                List<Booking> allAmenityBookings = bookingService.getBookingsByAmenity(amenityId);
                if (!allAmenityBookings.isEmpty()) {
                    AmenityType firstBookingType = allAmenityBookings.get(0).getAmenityType();
                    isDayBased = firstBookingType == AmenityType.HALL || firstBookingType == AmenityType.BBQ;
                }
                // If still no bookings found, we can't determine from bookings
                // The UI will use its own getAmenityType() function as fallback
                // We'll return isDayBased as false, and UI will override based on amenity name
            }

            if (isDayBased) {
                // For day-based amenities, return simple booked status
                response.put("isDayBased", true);
                response.put("isBooked", !bookings.isEmpty());
                response.put("bookingCount", bookings.size());
            } else {
                // For slot-based amenities, return time slot information with counts
                response.put("isDayBased", false);

                List<LocalTime> availableSlots = bookingService.getAvailableSlots(amenityId, localDate);
                List<String> availableSlotsStr = availableSlots.stream()
                        .map(LocalTime::toString)
                        .collect(Collectors.toList());
                response.put("availableSlots", availableSlotsStr);

                // Group bookings by time slot and count them
                Map<String, Integer> bookedSlotsCount = new HashMap<>();
                Map<String, List<Long>> bookedSlotsUsers = new HashMap<>();

                for (Booking booking : bookings) {
                    String timeSlot = booking.getTimeSlot();
                    if (timeSlot != null && !timeSlot.isEmpty()) {
                        // Extract start time from timeSlot (format: "HH:mm-HH:mm")
                        String slotStartTime = timeSlot.split("-")[0];
                        bookedSlotsCount.put(slotStartTime, bookedSlotsCount.getOrDefault(slotStartTime, 0) + 1);
                        bookedSlotsUsers.computeIfAbsent(slotStartTime, k -> new ArrayList<>())
                                .add(booking.getUserId());
                    } else {
                        // Fallback to slotStart time if timeSlot is null
                        String slotTime = booking.getSlotStart().toLocalTime().toString();
                        bookedSlotsCount.put(slotTime, bookedSlotsCount.getOrDefault(slotTime, 0) + 1);
                        bookedSlotsUsers.computeIfAbsent(slotTime, k -> new ArrayList<>()).add(booking.getUserId());
                    }
                }

                response.put("bookedSlots", bookedSlotsCount);
            }

            System.out.println("Response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in getAvailability:");
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @RequestBody BookingRequest request) {

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserServiceClient.SessionInfo> sessionOpt = userServiceClient.getSessionInfo(sessionToken);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Validate required fields
            if (request.getAmenityId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Amenity ID is required"));
            }
            if (request.getAmenityType() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Amenity type is required"));
            }
            if (request.getBookingDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Booking date is required"));
            }

            Booking booking = bookingService.createBooking(
                    request.getAmenityId(),
                    sessionOpt.get().getUserId(),
                    request.getAmenityType(),
                    request.getBookingDate(),
                    request.getTimeSlot(),
                    request.getSlotStart(),
                    request.getSlotEnd());
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (BookingValidationException e) {
            // Validation error - return 409 Conflict with error message
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            // Slot already booked - concurrency conflict
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Slot is already booked"));
        } catch (Exception e) {
            // Other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while creating the booking: " + e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getUserBookings(
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserServiceClient.SessionInfo> sessionOpt = userServiceClient.getSessionInfo(sessionToken);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Booking> bookings = bookingService.getUserBookings(sessionOpt.get().getUserId());
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @PathVariable Long id) {

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserServiceClient.SessionInfo> sessionOpt = userServiceClient.getSessionInfo(sessionToken);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            bookingService.cancelBooking(id, sessionOpt.get().getUserId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Admin endpoints
    @GetMapping("/admin/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserServiceClient.SessionInfo> sessionOpt = userServiceClient.getSessionInfo(sessionToken);
        if (sessionOpt.isEmpty() || !sessionOpt.get().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Booking> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(booking -> {
                    Optional<UserServiceClient.UserInfo> bookingUserOpt = userServiceClient
                            .getUserInfo(booking.getUserId());
                    UserServiceClient.UserInfo bookingUser = bookingUserOpt.orElse(null);
                    return new BookingResponse(
                            booking.getId(),
                            booking.getAmenityId(),
                            booking.getUserId(),
                            bookingUser != null ? bookingUser.getUsername() : "Unknown",
                            bookingUser != null ? bookingUser.getName() : null,
                            bookingUser != null ? bookingUser.getFlatNo() : null,
                            bookingUser != null ? bookingUser.getContactNumber() : null,
                            booking.getSlotStart(),
                            booking.getSlotEnd(),
                            booking.getCreatedAt());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/bookings/amenity/{amenityId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByAmenity(
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @PathVariable Long amenityId) {

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserServiceClient.SessionInfo> sessionOpt = userServiceClient.getSessionInfo(sessionToken);
        if (sessionOpt.isEmpty() || !sessionOpt.get().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Booking> bookings = bookingService.getBookingsByAmenity(amenityId);
        List<BookingResponse> responses = bookings.stream()
                .map(booking -> {
                    Optional<UserServiceClient.UserInfo> bookingUserOpt = userServiceClient
                            .getUserInfo(booking.getUserId());
                    UserServiceClient.UserInfo bookingUser = bookingUserOpt.orElse(null);
                    return new BookingResponse(
                            booking.getId(),
                            booking.getAmenityId(),
                            booking.getUserId(),
                            bookingUser != null ? bookingUser.getUsername() : "Unknown",
                            bookingUser != null ? bookingUser.getName() : null,
                            bookingUser != null ? bookingUser.getFlatNo() : null,
                            bookingUser != null ? bookingUser.getContactNumber() : null,
                            booking.getSlotStart(),
                            booking.getSlotEnd(),
                            booking.getCreatedAt());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats(
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<UserServiceClient.SessionInfo> sessionOpt = userServiceClient.getSessionInfo(sessionToken);
        if (sessionOpt.isEmpty() || !sessionOpt.get().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Booking> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(booking -> {
                    Optional<UserServiceClient.UserInfo> bookingUserOpt = userServiceClient
                            .getUserInfo(booking.getUserId());
                    UserServiceClient.UserInfo bookingUser = bookingUserOpt.orElse(null);
                    return new BookingResponse(
                            booking.getId(),
                            booking.getAmenityId(),
                            booking.getUserId(),
                            bookingUser != null ? bookingUser.getUsername() : "Unknown",
                            bookingUser != null ? bookingUser.getName() : null,
                            bookingUser != null ? bookingUser.getFlatNo() : null,
                            bookingUser != null ? bookingUser.getContactNumber() : null,
                            booking.getSlotStart(),
                            booking.getSlotEnd(),
                            booking.getCreatedAt());
                })
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", bookings.size());
        stats.put("bookings", responses);

        return ResponseEntity.ok(stats);
    }

    /**
     * Helper method to create error response map
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }
}
