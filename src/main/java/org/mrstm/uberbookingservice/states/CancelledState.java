package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;

public class CancelledState implements BookingState {

    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus, Long bookingId, UpdateBookingRequestDto completeBookingRequestDto) {
        if(newStatus == BookingStatus.CANCELLED){
            bookingContext.getBookingRepository().updateBookingStatus(bookingId , BookingStatus.CANCELLED);
            bookingContext.getPassengerRepository().clearActiveBooking(Long.parseLong(completeBookingRequestDto.getPassengerId()));
            bookingContext.getDriverRepository().clearActiveBooking(Long.parseLong(completeBookingRequestDto.getDriverId()));
            bookingContext.getRedisService().deleteDriverBookingPair(completeBookingRequestDto.getDriverId());
            bookingContext.setState(this);
        }
        else {
            throw new IllegalStateException(
                    "Invalid transition: Booking is already CANCELLED. Cannot move to " + newStatus
            );
        }
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }
}
