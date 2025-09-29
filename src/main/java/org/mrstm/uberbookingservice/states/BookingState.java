package org.mrstm.uberbookingservice.states;

import org.mrstm.uberentityservice.models.BookingStatus;

public interface BookingState {
    void updateStatus(BookingContext bookingContext , BookingStatus newStatus);
    BookingStatus getStatus();
}
