package org.mrstm.uberbookingservice.services;

import org.mrstm.uberbookingservice.dto.CreateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.CreateBookingResponseDto;
import org.mrstm.uberbookingservice.dto.DriverLocationDto;
import org.mrstm.uberbookingservice.dto.NearbyDriversRequestDto;
import org.mrstm.uberbookingservice.feignClient.LocationGettingClient;
import org.mrstm.uberbookingservice.repositories.DriverRepository;
import org.mrstm.uberbookingservice.repositories.PassengerRepository;
import org.mrstm.uberentityservice.models.Booking;
import org.mrstm.uberentityservice.models.BookingStatus;
import org.mrstm.uberentityservice.models.Driver;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingServiceUsingFeignClient {
    private final LocationGettingClient locationGettingClient;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public BookingServiceUsingFeignClient(LocationGettingClient locationGettingClient, PassengerRepository passengerRepository, DriverRepository driverRepository) {
        this.locationGettingClient = locationGettingClient;
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    public ResponseEntity<CreateBookingResponseDto> createBooking(CreateBookingRequestDto createBookingRequestDto) {
        Passenger passenger = passengerRepository.findById(createBookingRequestDto.getPassengerId()).get();
        NearbyDriversRequestDto nearbyDriversRequestDto = NearbyDriversRequestDto.builder()
                .latitude(createBookingRequestDto.getStartLocation().getLatitude())
                .longitude(createBookingRequestDto.getStartLocation().getLongitude())
                .build();
        ResponseEntity<DriverLocationDto[]> driverList = locationGettingClient.createBookingUsingFeign(nearbyDriversRequestDto);
        Long topDriverId = Long.parseLong(driverList.getBody()[0].getDriverId());
        System.out.println("topDriverId: " + topDriverId);
        Driver driver = driverRepository.findById(topDriverId).get();

        Booking newBooking = Booking.builder()
                .driver(driver)
                .startLocation(createBookingRequestDto.getStartLocation())
                .endLocation(createBookingRequestDto.getEndLocation())
                .passenger(passenger)
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .build();
        CreateBookingResponseDto finalResponseDto = CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(driver))
                .build();
        return new ResponseEntity<>(finalResponseDto, HttpStatus.OK);
    }
}
