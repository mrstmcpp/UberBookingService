package org.mrstm.uberbookingservice.services;


import org.mrstm.uberbookingservice.dto.*;
import org.mrstm.uberentityservice.models.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface BookingService {
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto bookingDetails);

    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingDetails , Long bookingId);

    public String cancelBooking(CancelBookingRequestDto cancelBookingRequestDto); //api just for development purposes

    public String completeBooking(Long bookingId, CompleteBookingRequestDto bookingCompleteRequestDto); //api just for development purposes

    public GetBookingDetailsResponseDTO getBookingDetails(Long bookingId);

    public Long getActiveBooking(Long passengerId);

    public UpdateBookingResponseDto updateStatus(UpdateBookingRequestDto bookingRequestDto);

}
