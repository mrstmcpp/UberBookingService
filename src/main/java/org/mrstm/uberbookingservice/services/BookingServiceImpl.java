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

        Booking newBooking = bookingRepository.save(booking);
        processNearbyDriverAsync(req , bookingDetails.getPassengerId() , newBooking.getId());

//        ResponseEntity<DriverLocationDto[]> driverList = restTemplate.postForEntity(LOCATION_SERVICE + "/api/location/nearby/drivers", req , DriverLocationDto[].class);
//        if(driverList.getStatusCode().is2xxSuccessful() && driverList.getBody() != null) {
//            List<DriverLocationDto> driverLocations = Arrays.asList(driverList.getBody());
//            driverLocations.forEach(location -> {
//                System.out.println(location.getDriverId() + " " + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
//            });
//        }
//
//
        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(newBooking.getDriver()))
                .build();
    }



    private void processNearbyDriverAsync(NearbyDriversRequestDto nearbyDriversRequestDto , Long passengerId , Long bookingId) {
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

                    try{
                        raiseRideRequestAsync(RideRequestDto.builder()
                                .passengerId(passengerId)
                                .bookingId(bookingId)
                                .build());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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
        Driver driver = driverRepository.findById(bookingDetails.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + bookingDetails.getDriverId()));

        bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.SCHEDULED, driver);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
        UpdateBookingResponseDto res = UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .bookingStatus(booking.getBookingStatus())
                .driver((booking.getDriver()))
                .build();
        System.out.println("Your ride with : " + driver.getFullName() + " is scheduled.");
        return res;
    }


    private void raiseRideRequestAsync(RideRequestDto rideRequestDto) {
        Call<Boolean> call = socketApi.raiseRideRequests(rideRequestDto);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Boolean res = response.body();
                    System.out.println("Booking successful. " + res.toString());
                }else {
                    System.out.println("Request Failed " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
