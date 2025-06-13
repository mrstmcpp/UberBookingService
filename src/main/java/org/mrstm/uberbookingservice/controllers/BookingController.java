package org.mrstm.uberbookingservice.controllers;

import org.mrstm.uberbookingservice.dto.CreateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.CreateBookingResponseDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingResponseDto;
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

    @PostMapping()
    public ResponseEntity<CreateBookingResponseDto> createBooking(@RequestBody CreateBookingRequestDto createBookingRequestDto) {
        CreateBookingResponseDto response = bookingService.createBooking(createBookingRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/feign")
    public ResponseEntity<CreateBookingResponseDto> createBookingFeign(@RequestBody CreateBookingRequestDto createBookingRequestDto) {
        CreateBookingResponseDto response = bookingService.createBooking(createBookingRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> updateBooking(@RequestBody UpdateBookingRequestDto requestDto , @PathVariable Long bookingId){
        return new ResponseEntity<>(bookingService.updateBooking(requestDto , bookingId) , HttpStatus.OK);
    }
}
