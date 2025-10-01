package org.mrstm.uberbookingservice.states;

import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberentityservice.models.BookingStatus;


public class ArrivedDriverState implements BookingState{
    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus, Long bookingId, UpdateBookingRequestDto updateBookingRequestDto) {
        switch (newStatus){
            case IN_RIDE:
                bookingContext.setState(new InrideState());
                break;
            case CANCELLED:
                bookingContext.setState(new CancelledState());
                break;
            default:
                throw new IllegalStateException("Illegal transition of state at arrivedstate class");
        }
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.ARRIVED;
    }
}
