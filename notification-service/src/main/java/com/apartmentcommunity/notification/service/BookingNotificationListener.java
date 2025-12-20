package com.apartmentcommunity.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BookingNotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationListener.class);

    @RabbitListener(queues = "booking.created.queue")
    public void handleBookingCreated(Map<String, Object> event) {
        logger.info("📧 EMAIL NOTIFICATION: Booking Created");
        logger.info("   Booking ID: {}", event.get("bookingId"));
        logger.info("   Amenity ID: {}", event.get("amenityId"));
        logger.info("   User ID: {}", event.get("userId"));
        logger.info("   Slot: {} to {}", event.get("slotStart"), event.get("slotEnd"));
        logger.info("   --- Simulated email sent to user ---");
    }

    @RabbitListener(queues = "booking.cancelled.queue")
    public void handleBookingCancelled(Map<String, Object> event) {
        logger.info("📧 EMAIL NOTIFICATION: Booking Cancelled");
        logger.info("   Booking ID: {}", event.get("bookingId"));
        logger.info("   Amenity ID: {}", event.get("amenityId"));
        logger.info("   User ID: {}", event.get("userId"));
        logger.info("   Slot: {} to {}", event.get("slotStart"), event.get("slotEnd"));
        logger.info("   --- Simulated email sent to user ---");
    }
}
