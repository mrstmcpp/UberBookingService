package org.mrstm.uberbookingservice.states;

import lombok.Getter;
import lombok.Setter;
import org.mrstm.uberbookingservice.dto.CompleteBookingRequestDto;
import org.mrstm.uberbookingservice.dto.UpdateBookingRequestDto;
import org.mrstm.uberbookingservice.repositories.BookingRepository;
import org.mrstm.uberbookingservice.repositories.DriverRepository;
import org.mrstm.uberbookingservice.repositories.PassengerRepository;
import org.mrstm.uberbookingservice.services.RedisService;
import org.mrstm.uberentityservice.models.BookingStatus;

@Getter
public class BookingContext {
    @Setter
    private BookingState state;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final RedisService redisService;


    public BookingContext(BookingRepository bookingRepository, PassengerRepository passengerRepository, DriverRepository driverRepository, RedisService redisService){
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
        this.redisService = redisService;
        this.state = new ScheduledState();
    }

    public BookingStatus getStatus(){
        return state.getStatus();
    }

    public void updateStatus(BookingStatus newStatus , Long bookingId, UpdateBookingRequestDto updateBookingRequestDto){

        state.updateStatus(this, newStatus , bookingId , updateBookingRequestDto);
    }

}
