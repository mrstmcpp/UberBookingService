package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.dto.CreateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.CreateBookingResponseDto;
import org.mrstm.uberbookingservice.dto.DriverLocationDto;
import org.mrstm.uberbookingservice.dto.NearbyDriversRequestDto;
import org.mrstm.uberbookingservice.repositories.BookingRepository;
import org.mrstm.uberbookingservice.repositories.PassengerRepository;
import org.mrstm.uberentityservice.models.Booking;
import org.mrstm.uberentityservice.models.BookingStatus;
import org.mrstm.uberentityservice.models.Driver;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final RestTemplate restTemplate;
    @Value("${location.service.url}")
    private String LOCATION_SERVICE;

    public BookingServiceImpl(BookingRepository bookingRepository, PassengerRepository passengerRepository , RestTemplate restTemplate) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.restTemplate = restTemplate;
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

        ResponseEntity<DriverLocationDto[]> driverList = restTemplate.postForEntity(LOCATION_SERVICE + "/api/location/nearby/drivers", req , DriverLocationDto[].class);
        if(driverList.getStatusCode().is2xxSuccessful() && driverList.getBody() != null) {
            List<DriverLocationDto> driverLocations = Arrays.asList(driverList.getBody());
            driverLocations.forEach(location -> {
                System.out.println(location.getDriverId() + " " + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
            });
        }


        Booking newBooking = bookingRepository.save(booking);
        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(newBooking.getDriver()))
                .build();
    }
}
