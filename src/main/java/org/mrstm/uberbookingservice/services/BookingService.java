package org.mrstm.uberbookingservice.services;


import org.mrstm.uberbookingservice.dto.CreateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.CreateBookingResponseDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface BookingService {
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto bookingDetails);

    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingDetails , Long bookingId);
}
