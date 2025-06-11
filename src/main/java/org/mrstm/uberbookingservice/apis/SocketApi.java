package org.mrstm.uberbookingservice.apis;

import org.mrstm.uberbookingservice.dto.RideRequestDto;
import org.springframework.http.ResponseEntity;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SocketApi {
    @POST("/api/socket/newride")
    Call<ResponseEntity<Boolean>> getNearbyDrivers(@Body RideRequestDto requestDto);
}
