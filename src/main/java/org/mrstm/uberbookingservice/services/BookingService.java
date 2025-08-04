package org.mrstm.uberbookingservice.services;


import org.mrstm.uberbookingservice.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface BookingService {
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto bookingDetails);

    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingDetails , Long bookingId);

    public String cancelBooking(CancelBookingRequestDto cancelBookingRequestDto);

    public void completeBooking(CompleteBookingRequestDto bookingCompleteRequestDto);
}
