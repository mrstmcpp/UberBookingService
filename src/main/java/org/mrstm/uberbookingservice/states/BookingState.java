package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;

public interface BookingState {
    void updateStatus(BookingContext bookingContext , BookingStatus newStatus , Long bookingId, UpdateBookingRequestDto updateBookingRequestDto);
    BookingStatus getStatus();
}
