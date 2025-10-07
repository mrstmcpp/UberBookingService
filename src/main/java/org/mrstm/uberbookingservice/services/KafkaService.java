package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.dto.KafkaDtos.BookingConfirmedEvent;
import org.mrstm.uberentityservice.dto.booking.BookingCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public interface KafkaService {
    void publishBookingCreated(String bookingId, BookingCreatedEvent bookingCreatedEvent);
    void publishBookingConfirmed(String bookingId , BookingConfirmedEvent bookingConfirmedEvent);
}
