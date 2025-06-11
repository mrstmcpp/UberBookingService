package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.apis.LocationServiceApi;
import org.mrstm.uberbookingservice.apis.SocketApi;
import org.mrstm.uberbookingservice.dto.*;
import org.mrstm.uberbookingservice.repositories.BookingRepository;
import org.mrstm.uberbookingservice.repositories.DriverRepository;
import org.mrstm.uberbookingservice.repositories.PassengerRepository;
import org.mrstm.uberentityservice.models.Booking;
import org.mrstm.uberentityservice.models.BookingStatus;
import org.mrstm.uberentityservice.models.Driver;
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
    private final DriverRepository driverRepository;
    private final SocketApi socketApi;
    @Value("${location.service.url}")
    private String LOCATION_SERVICE;



    public BookingServiceImpl(BookingRepository bookingRepository,
                              PassengerRepository passengerRepository ,
                              RestTemplate restTemplate ,
                              LocationServiceApi locationServiceApi,
                              DriverRepository driverRepository ,
                              SocketApi socketApi) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.restTemplate = restTemplate;
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
        this.socketApi = socketApi;
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

        processNearbyDriverAsync(req , bookingDetails.getPassengerId());

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



    private void processNearbyDriverAsync(NearbyDriversRequestDto nearbyDriversRequestDto , Long passengerId) {
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDriver(nearbyDriversRequestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
//                try{
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
                if(response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                    driverLocations.forEach(location -> {
                        System.out.println(location.getDriverId() + " " + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                    });

                    raiseRideRequestAsync(RideRequestDto.builder()
                            .passengerId(passengerId)
                            .build());
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


    @Override
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingDetails, Long bookingId) {
        Optional<Driver> driver = driverRepository.findById(bookingDetails.getDriverId());
        bookingRepository.updateBookingStatusAndDriverById(bookingId , BookingStatus.SCHEDULED, driver.get());
        Optional<Booking> booking =  bookingRepository.findById(bookingId);
        return UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .bookingStatus(booking.get().getBookingStatus())
                .driver(Optional.ofNullable(booking.get().getDriver()))
                .build();
    }

    private void raiseRideRequestAsync(RideRequestDto rideRequestDto) {
        Call<ResponseEntity<Boolean>> call = socketApi.getNearbyDrivers(rideRequestDto);
        call.enqueue(new Callback<ResponseEntity<Boolean>>() {
            @Override
            public void onResponse(Call<ResponseEntity<Boolean>> call, Response<ResponseEntity<Boolean>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Boolean res = response.body().getBody();
                    System.out.println("Driver response is : " + res);
                }else {
                    System.out.println("Request Failed " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<Boolean>> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
