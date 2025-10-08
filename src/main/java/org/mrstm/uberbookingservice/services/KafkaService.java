package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.dto.KafkaDtos.BookingConfirmedEvent;
import org.mrstm.uberentityservice.dto.booking.BookingCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public interface KafkaService {
    void publishBookingCreated(BookingCreatedEvent bookingCreatedEvent);
    void publishBookingConfirmedNotification(String bookingId , BookingConfirmedEvent bookingConfirmedEvent);
}
