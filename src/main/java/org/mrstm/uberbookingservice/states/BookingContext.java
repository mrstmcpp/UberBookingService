package org.mrstm.uberbookingservice.states;

import org.mrstm.uberentityservice.models.BookingStatus;

public class BookingContext {
    private BookingState state;

    public BookingContext(){
        this.state = new ScheduledState();
    }

    public void setState(BookingState state){
        this.state = state;
    }

    public BookingStatus getStatus(){
        return state.getStatus();
    }

    public void updateStatus(BookingStatus newStatus){
        state.updateStatus(this, newStatus);
    }
}
