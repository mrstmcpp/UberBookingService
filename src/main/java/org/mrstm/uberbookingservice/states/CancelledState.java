package org.mrstm.uberbookingservice.states;

import org.mrstm.uberentityservice.models.BookingStatus;

public class CancelledState implements BookingState {
    @Override
    public void updateStatus(BookingContext bookingContext, BookingStatus newStatus) {
        throw new IllegalStateException(
                "Invalid transition: Booking is already CANCELLED. Cannot move to " + newStatus
        );
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }
}
