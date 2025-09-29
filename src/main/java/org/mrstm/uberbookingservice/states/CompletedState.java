package org.mrstm.uberbookingservice.states;

import org.mrstm.uberentityservice.models.BookingStatus;

public class CompletedState implements BookingState{
    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus) {
        throw new IllegalStateException(
                "Invalid transition: Booking is already COMPLETED. Cannot move to " + newStatus
        );
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.COMPLETED;
    }
}
