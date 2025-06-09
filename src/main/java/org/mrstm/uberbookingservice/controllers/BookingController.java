package org.mrstm.uberbookingservice.controllers;

import org.mrstm.uberbookingservice.dto.CreateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.CreateBookingResponseDto;
import org.mrstm.uberbookingservice.services.BookingService;
import org.mrstm.uberbookingservice.services.BookingServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {
    private final BookingServiceImpl bookingService;
    public BookingController(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/")
    public ResponseEntity<CreateBookingResponseDto> createBooking(@RequestBody CreateBookingRequestDto createBookingRequestDto) {
        CreateBookingResponseDto response = bookingService.createBooking(createBookingRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
