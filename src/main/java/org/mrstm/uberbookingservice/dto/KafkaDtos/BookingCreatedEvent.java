package org.mrstm.uberbookingservice.dto.KafkaDtos;

import lombok.*;
import org.mrstm.uberentityservice.models.ExactLocation;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BookingCreatedEvent {
    private String bookingId;
    private String passengerId;
    private double latitude;
    private double longitude;
}
