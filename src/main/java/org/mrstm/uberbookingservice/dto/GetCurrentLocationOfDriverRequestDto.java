package org.mrstm.uberbookingservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GetCurrentLocationOfDriverRequestDto {
    private String driverId;
}
