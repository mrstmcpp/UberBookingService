package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.dto.KafkaDtos.BookingConfirmedEvent;
import org.mrstm.uberentityservice.dto.booking.BookingCreatedEvent;

import org.mrstm.uberentityservice.kafkaTopics.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaServiceImpl implements KafkaService {
    private final KafkaTemplate<String , Object> kafkaTemplate;

    public KafkaServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishBookingCreated( BookingCreatedEvent bookingCreatedEvent) {
        kafkaTemplate.send(KafkaTopics.BOOKING_CREATED , bookingCreatedEvent);
    }

    @Override
    public void publishBookingConfirmedNotification(String bookingId, BookingConfirmedEvent bookingConfirmedEvent) {
    }
}
