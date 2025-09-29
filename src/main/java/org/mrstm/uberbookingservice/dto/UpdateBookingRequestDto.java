package org.mrstm.uberbookingservice.dto;

import lombok.*;
import org.mrstm.uberentityservice.models.BookingStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequestDto {
    private Long bookingId;
    private Long driverId;
    private String bookingStatus;
}
