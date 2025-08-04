package org.mrstm.uberbookingservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mrstm.uberentityservice.models.Booking;
import org.mrstm.uberentityservice.models.ExactLocation;

@Getter
@Setter
@Builder
public class CompleteBookingRequestDto {
    private Booking booking;
    private ExactLocation currLocation;
}
