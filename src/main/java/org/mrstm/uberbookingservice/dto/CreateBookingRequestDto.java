package org.mrstm.uberbookingservice.dto;

import lombok.*;
import org.mrstm.uberentityservice.models.ExactLocation;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDto {
    private Long passengerId;
    private ExactLocation startLocation;
    private ExactLocation endLocation;
}
