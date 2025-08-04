package org.mrstm.uberbookingservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mrstm.uberentityservice.models.Booking;

@Getter
@Setter
@Builder
public class CancelBookingRequestDto {
    private Booking booking;
}
