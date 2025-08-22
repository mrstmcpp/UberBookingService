package org.mrstm.uberbookingservice.apis;


import org.mrstm.uberbookingservice.dto.DriverLocationDto;
import org.mrstm.uberbookingservice.dto.GetCurrentLocationOfDriverRequestDto;
import org.mrstm.uberbookingservice.dto.NearbyDriversRequestDto;
import org.mrstm.uberbookingservice.models.Location;
import org.springframework.web.bind.annotation.PathVariable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LocationServiceApi {
    @POST("/api/v1/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDriver(@Body NearbyDriversRequestDto nearbyDriversRequestDto);


    @GET("/api/v1/location/driver/{driverId}")
    Call<Location> getCurrentLocationOfDriver(@PathVariable("driverId") long driverId);
}
