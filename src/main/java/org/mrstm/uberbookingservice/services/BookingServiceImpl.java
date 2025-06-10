package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.apis.LocationServiceApi;
import org.mrstm.uberbookingservice.dto.CreateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.CreateBookingResponseDto;
import org.mrstm.uberbookingservice.dto.DriverLocationDto;
import org.mrstm.uberbookingservice.dto.NearbyDriversRequestDto;
import org.mrstm.uberbookingservice.feignClient.LocationGettingClient;
import org.mrstm.uberbookingservice.repositories.BookingRepository;
import org.mrstm.uberbookingservice.repositories.PassengerRepository;
import org.mrstm.uberentityservice.models.Booking;
import org.mrstm.uberentityservice.models.BookingStatus;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final RestTemplate restTemplate;
    private final LocationServiceApi locationServiceApi;

    @Value("${location.service.url}")
    private String LOCATION_SERVICE;


    public BookingServiceImpl(BookingRepository bookingRepository, PassengerRepository passengerRepository , RestTemplate restTemplate ,  LocationServiceApi locationServiceApi) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.restTemplate = restTemplate;
        this.locationServiceApi = locationServiceApi;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto bookingDetails) {
        Passenger p = passengerRepository.findById(bookingDetails.getPassengerId()).orElseThrow(() -> new RuntimeException("Passenger not found with id: " + bookingDetails.getPassengerId()));
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .passenger(p)
                .build();

        NearbyDriversRequestDto req = NearbyDriversRequestDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();

        processNearbyDriverAsync(req);

//        ResponseEntity<DriverLocationDto[]> driverList = restTemplate.postForEntity(LOCATION_SERVICE + "/api/location/nearby/drivers", req , DriverLocationDto[].class);
//        if(driverList.getStatusCode().is2xxSuccessful() && driverList.getBody() != null) {
//            List<DriverLocationDto> driverLocations = Arrays.asList(driverList.getBody());
//            driverLocations.forEach(location -> {
//                System.out.println(location.getDriverId() + " " + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
//            });
//        }
//
//
        Booking newBooking = bookingRepository.save(booking);
        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(newBooking.getDriver()))
                .build();
    }


    private void processNearbyDriverAsync(NearbyDriversRequestDto nearbyDriversRequestDto) {
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDriver(nearbyDriversRequestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                try{
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                    driverLocations.forEach(location -> {
                        System.out.println(location.getDriverId() + " " + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                    });
                }else {
                    System.out.println("Request Failed " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        }); //enqueue method is for async request & execute for sync
    }
}
