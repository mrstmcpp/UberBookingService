package org.mrstm.uberbookingservice.apis;


import org.mrstm.uberbookingservice.dto.DriverLocationDto;
import org.mrstm.uberbookingservice.dto.NearbyDriversRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LocationServiceApi {
    @POST("/api/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDriver(@Body NearbyDriversRequestDto nearbyDriversRequestDto);

}
