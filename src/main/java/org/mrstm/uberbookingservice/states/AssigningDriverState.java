package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;

public class AssigningDriverState implements BookingState{


    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus, Long bookingId, UpdateBookingRequestDto completeBookingRequestDto) {
        switch (newStatus){
            case SCHEDULED:
                bookingContext.setState(new ScheduledState());
                break;
            case CANCELLED:
                bookingContext.setState(new CancelledState());
                break;
            default:
                throw new IllegalStateException("Invalid transition");
        }
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.ASSIGNING_DRIVER;
    }
}
