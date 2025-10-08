package org.mrstm.uberbookingservice.controllers;

import org.mrstm.uberbookingservice.dto.*;
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

    @GetMapping("/{bookingId}")
    public ResponseEntity<GetBookingDetailsResponseDTO> getBookingDetails(@PathVariable Long bookingId){
        return new ResponseEntity<>(bookingService.getBookingDetails(bookingId) , HttpStatus.OK);
    }

    @GetMapping("/active/{passengerId}")
    public ResponseEntity<Long> getActiveBookingOfPassenger(@PathVariable Long passengerId){
        return new ResponseEntity<>(bookingService.getActiveBooking(passengerId) , HttpStatus.OK);
    }

    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<String> completeBooking(@PathVariable Long bookingId, @RequestBody CompleteBookingRequestDto completeBookingRequestDto){
        return new ResponseEntity<>(bookingService.completeBooking(bookingId , completeBookingRequestDto) , HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<UpdateBookingResponseDto> updateBookingStatus(@RequestBody UpdateBookingRequestDto updateBookingRequestDto){
        return new ResponseEntity<>(bookingService.updateStatus(updateBookingRequestDto) , HttpStatus.OK);
    }
}
