package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;

public class ScheduledState implements BookingState{

    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus, Long bookingId, UpdateBookingRequestDto completeBookingRequestDto) {
        switch (newStatus){
            case ARRIVED:
                bookingContext.setState(new ArrivedDriverState());
                break;
            case CANCELLED:
                bookingContext.setState(new CancelledState());
                break;
            default:
                throw new IllegalStateException("Invalid transition to state.");
        }
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.SCHEDULED;
    }
}
