package org.mrstm.uberbookingservice.apis;

import org.mrstm.uberbookingservice.dto.NotificationDTO;
import org.mrstm.uberbookingservice.dto.RideRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SocketApi {
    @POST("/api/socket/newride")
    Call<Boolean> raiseRideRequests(@Body RideRequestDto requestDto);

    @POST("api/socket/notify/{passengerId}")
    Call<Boolean> notifyPassenger(@Body NotificationDTO notificationDTO , @Path("passengerId") String passengerId);


}
