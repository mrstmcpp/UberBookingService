package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;

public class CompletedState implements BookingState{

    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus, Long bookingId, UpdateBookingRequestDto completeBookingRequestDto) {
        throw new IllegalStateException(
                "Invalid transition: Booking is already COMPLETED. Cannot move to " + newStatus
        );
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.COMPLETED;
    }

    public static CompletedState completeBooking(BookingContext bookingContext , Long bookingId, UpdateBookingRequestDto updateBookingRequestDto){
        bookingContext.getBookingRepository().updateBookingStatus(bookingId , BookingStatus.COMPLETED);
        bookingContext.getPassengerRepository().clearActiveBooking(Long.parseLong(updateBookingRequestDto.getPassengerId()));
        bookingContext.getDriverRepository().clearActiveBooking(Long.parseLong(updateBookingRequestDto.getDriverId()));

        return new CompletedState();
    }
}
