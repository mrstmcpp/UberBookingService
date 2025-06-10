package org.mrstm.uberbookingservice.feignClient;

import org.mrstm.uberbookingservice.dto.DriverLocationDto;
import org.mrstm.uberbookingservice.dto.NearbyDriversRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "UberLocationService" , url = "http://localhost:3004" , path = "/api/location")
public interface LocationGettingClient {
    @GetMapping("/nearby/drivers")
    public ResponseEntity<DriverLocationDto[]> createBookingUsingFeign(@RequestBody NearbyDriversRequestDto nearbyDriversRequestDto);
}
