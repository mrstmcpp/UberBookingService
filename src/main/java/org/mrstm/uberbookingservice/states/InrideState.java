package org.mrstm.uberbookingservice.states;

import org.mrstm.uberentityservice.models.BookingStatus;

public class InrideState implements BookingState{
    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus) {
        switch (newStatus){
            case COMPLETED:
                bookingContext.setState(new CompletedState());
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
