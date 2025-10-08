package org.mrstm.uberbookingservice.dto;

import lombok.*;
import org.mrstm.uberentityservice.models.ExactLocation;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearbyDriversRequestDto {
    ExactLocation pickupLocation;
    ExactLocation dropLocation;

}
