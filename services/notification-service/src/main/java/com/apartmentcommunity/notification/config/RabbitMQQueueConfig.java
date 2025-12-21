package com.apartmentcommunity.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQQueueConfig {
    
    @Bean
    public Queue bookingCreatedQueue() {
        return new Queue("booking.created.queue", true);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return new Queue("booking.cancelled.queue", true);
    }

    @Bean
    public Binding bookingCreatedBinding(@Qualifier("bookingCreatedQueue") Queue bookingCreatedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingCreatedQueue)
            .to(bookingExchange)
            .with("booking.created");
    }

    @Bean
    public Binding bookingCancelledBinding(@Qualifier("bookingCancelledQueue") Queue bookingCancelledQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingCancelledQueue)
            .to(bookingExchange)
            .with("booking.cancelled");
    }
}
