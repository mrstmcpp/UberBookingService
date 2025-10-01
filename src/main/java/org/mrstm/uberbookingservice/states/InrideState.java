package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;

public class InrideState implements BookingState{


    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus, Long bookingId, UpdateBookingRequestDto completeBookingRequestDto) {
        switch (newStatus){
            case COMPLETED:
                bookingContext.setState(CompletedState.completeBooking(bookingContext , bookingId , completeBookingRequestDto));
                break;
            default:
                throw new IllegalStateException("Invalid transition of states");
        }
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.IN_RIDE;
    }
}
