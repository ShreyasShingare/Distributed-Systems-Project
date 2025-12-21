package com.apartmentcommunity.booking.service;

import com.apartmentcommunity.booking.model.Booking;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationPublisher {
    private static final String EXCHANGE_NAME = "booking_events";
    private static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";
    private static final String BOOKING_CANCELLED_ROUTING_KEY = "booking.cancelled";

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookingCreated(Booking booking) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "BOOKING_CREATED");
            event.put("bookingId", booking.getId());
            event.put("amenityId", booking.getAmenityId());
            event.put("userId", booking.getUserId());
            event.put("slotStart", booking.getSlotStart().toString());
            event.put("slotEnd", booking.getSlotEnd().toString());
            
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, BOOKING_CREATED_ROUTING_KEY, event);
        } catch (Exception e) {
            // Log error but don't fail the booking if RabbitMQ is unavailable
            System.err.println("Warning: Failed to publish booking created event: " + e.getMessage());
        }
    }

    public void publishBookingCancelled(Booking booking) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "BOOKING_CANCELLED");
            event.put("bookingId", booking.getId());
            event.put("amenityId", booking.getAmenityId());
            event.put("userId", booking.getUserId());
            event.put("slotStart", booking.getSlotStart().toString());
            event.put("slotEnd", booking.getSlotEnd().toString());
            
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, BOOKING_CANCELLED_ROUTING_KEY, event);
        } catch (Exception e) {
            // Log error but don't fail the cancellation if RabbitMQ is unavailable
            System.err.println("Warning: Failed to publish booking cancelled event: " + e.getMessage());
        }
    }
}
