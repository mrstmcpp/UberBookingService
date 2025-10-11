package org.mrstm.uberbookingservice.services;


import jakarta.ws.rs.NotFoundException;
import org.mrstm.uberbookingservice.apis.LocationServiceApi;
import org.mrstm.uberbookingservice.apis.SocketApi;
import org.mrstm.uberbookingservice.dto.*;
import org.mrstm.uberbookingservice.models.Location;
import org.mrstm.uberbookingservice.repositories.BookingRepository;
import org.mrstm.uberbookingservice.repositories.DriverRepository;
import org.mrstm.uberbookingservice.repositories.OtpRepository;
import org.mrstm.uberbookingservice.repositories.PassengerRepository;
import org.mrstm.uberbookingservice.states.*;
import org.mrstm.uberentityservice.dto.booking.BookingCreatedEvent;
import org.mrstm.uberentityservice.models.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final OtpRepository otpRepository;
    private final DriverRepository driverRepository;
    private final SocketApi socketApi;
    private final KafkaService kafkaService;
    private final RedisService redisService;


    public BookingServiceImpl(BookingRepository bookingRepository,
                              PassengerRepository passengerRepository, OtpRepository otpRepository,
                              DriverRepository driverRepository ,
                              SocketApi socketApi, KafkaService kafkaService, RedisService redisService) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.otpRepository = otpRepository;
        this.driverRepository = driverRepository;
        this.socketApi = socketApi;
        this.kafkaService = kafkaService;
        this.redisService = redisService;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto bookingDetails) {
        try{
            Passenger p = passengerRepository.findById(bookingDetails.getPassengerId()).orElseThrow(() -> new RuntimeException("Passenger not found with id: " + bookingDetails.getPassengerId()));
            if(p.getActiveBooking() != null){
                throw new IllegalArgumentException("Passenger already have active booking.");
            }
            Booking booking = Booking.builder()
                    .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                    .startLocation(bookingDetails.getStartLocation())
                    .endLocation(bookingDetails.getEndLocation())
                    .passenger(p)
                    .build();
            NearbyDriversRequestDto req = NearbyDriversRequestDto.builder()
                    .dropLocation(bookingDetails.getEndLocation())
                    .pickupLocation(bookingDetails.getStartLocation())
                    .build();

            Booking newBooking = bookingRepository.save(booking);
            passengerRepository.setActiveBooking(p.getId() , newBooking);
            //changing to kafka
            processNearbyDriverAsync(req , bookingDetails.getPassengerId() , newBooking.getId());

            return CreateBookingResponseDto.builder()
                    .bookingId(newBooking.getId())
                    .bookingStatus(newBooking.getBookingStatus().toString())
                    .driver(Optional.ofNullable(newBooking.getDriver()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void processNearbyDriverAsync(NearbyDriversRequestDto nearbyDriversRequestDto , Long passengerId , Long bookingId) {
        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(bookingId.toString())
                .passengerId(passengerId.toString())
                .pickupLocation(nearbyDriversRequestDto.getPickupLocation())
                .dropLocation(nearbyDriversRequestDto.getDropLocation())
                .build();
        kafkaService.publishBookingCreated(event);
        System.out.println("BookingCreatedEvent published for bookingId: " + bookingId);
    }


    @Override
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingDetails, Long bookingId) {
        try {
            Driver driver = driverRepository.findById(Long.parseLong(bookingDetails.getDriverId()))
                    .orElseThrow(() -> new NotFoundException("Driver not found with ID: " + bookingDetails.getDriverId()));
            if(driver.getActiveBooking() != null){
                throw new IllegalArgumentException("This driver already have active booking.");
            }
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));

            bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.SCHEDULED, driver);

            redisService.setDriverBookingPair(bookingDetails.getDriverId() , bookingDetails.getBookingId()); //storing in cachee

            OTP otp = OTP.make(booking);
            otpRepository.save(otp);
            booking.setOtp(otp);
            booking.setBookingStatus(BookingStatus.SCHEDULED);
            booking.setDriver(driver);
            bookingRepository.save(booking);

            UpdateBookingResponseDto response = UpdateBookingResponseDto.builder()
                    .bookingId(bookingId)
                    .bookingStatus(booking.getBookingStatus())
                    .build();
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .bookingId(bookingId)
                    .driverId(driver.getId())
                    .fullName(driver.getFullName())
                    .bookingStatus(booking.getBookingStatus().toString())
                    .build();
            System.out.println("Your ride with: " + driver.getFullName() + " is scheduled.");
            return response;

        } catch (RuntimeException e) {
            System.err.println("Error updating booking: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Internal server error occurred while updating booking.");
        }
    }



    private void raiseRideRequestAsync(RideRequestDto rideRequestDto) {
        Call<Boolean> call = socketApi.raiseRideRequests(rideRequestDto);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Boolean res = response.body();
                    System.out.println("Booking successful. " + res.toString());
                }else {
                    System.out.println("Request Failed " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {

                System.out.println("Timeout while searching for driver.");
            }
        });
    }

    @Override
    public String cancelBooking(CancelBookingRequestDto cancelBookingRequestDto) { //api just for development purposes
        Long bookingId = cancelBookingRequestDto.getBooking().getId();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        if (booking.getBookingStatus() == BookingStatus.IN_RIDE || booking.getBookingStatus() == BookingStatus.COMPLETED) {
            return "It is not possible to cancel the booking at this stage.";
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        passengerRepository.clearActiveBooking(booking.getPassenger().getId());
        return "Booking cancelled successfully.";
    }

    @Override
    public String completeBooking(Long bookingId , CompleteBookingRequestDto bookingCompleteRequestDto) { //api just for development purposes
        Optional<Passenger> p = passengerRepository.findById(bookingCompleteRequestDto.getPassengerId());
        p.ifPresent(passenger -> passenger.setActiveBooking(null));
        return "Booking Completed successfully.";
    }


    @Override
    public GetBookingDetailsResponseDTO getBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.getBookingById(bookingId);
        if (booking == null) {
            return null;
        }
//        System.out.println("OTP is : " + booking.getOtp().getCode());

        GetBookingDetailsResponseDTO response = GetBookingDetailsResponseDTO.builder()
                .bookingId(booking.getId())
                .otp(booking.getOtp().getCode())
                .bookingStatus(booking.getBookingStatus().toString())
                .driverId(booking.getDriver().getId())
                .driverName(booking.getDriver().getFullName())
                .startTime(booking.getStartTime())

                .startLocation(Location.builder()
                        .latitude(booking.getStartLocation().getLatitude())
                        .longitude(booking.getStartLocation().getLongitude())
                        .build())

                .endLocation(Location.builder()
                        .latitude(booking.getEndLocation().getLatitude())
                        .longitude(booking.getEndLocation().getLongitude())
                        .build())

                .build();

        return (response);
    }




//    private void notifyPassenger(NotificationDTO notificationDTO , String passengerId){
//        Call<Boolean> call = socketApi.notifyPassenger(notificationDTO , passengerId);
//
//        call.enqueue(new Callback<>() {
//            @Override
//            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
//                if(response.isSuccessful() && response.body() != null) {
//                    Boolean res = response.body();
//                    System.out.println("Passenger Notified. " + res);
//                }else {
//                    System.out.println("Notification Failed " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Boolean> call, Throwable throwable) {
//                System.out.println("Failed to send notification to passenger.");
//            }
//        });
//    }


    @Override
    public Long getActiveBooking(Long passengerId) {
        try{
            return passengerRepository.getActiveBookingByPassengerId(passengerId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UpdateBookingResponseDto updateStatus(UpdateBookingRequestDto bookingRequestDto) {
        BookingContext booking = new BookingContext(bookingRepository , passengerRepository, driverRepository , redisService);
//        Booking dbBooking = bookingRepository.getBookingById(bookingRequestDto.getBookingId());
        BookingStatus currentStatus = bookingRepository.getBookingStatusById(Long.parseLong(bookingRequestDto.getBookingId()));

//        System.out.println("Current for : " + dbBooking.getId() + " -> " + currentStatus);
        System.out.println("Requested : " + bookingRequestDto.getBookingStatus());

        booking.setState(getStateObject(currentStatus)); // database state would be here

        try {
            booking.updateStatus(bookingRequestDto.getBookingStatus() , Long.parseLong(bookingRequestDto.getBookingId() ), bookingRequestDto);
            bookingRepository.updateBookingStatus(
                    Long.parseLong(bookingRequestDto.getBookingId()),
                    bookingRequestDto.getBookingStatus()
            );

            return UpdateBookingResponseDto.builder()
                    .bookingStatus(booking.getStatus())
                    .bookingId(Long.parseLong(bookingRequestDto.getBookingId()))
                    .build();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Transition not allowed: " + currentStatus + " -> " + bookingRequestDto.getBookingStatus());
        }
    }



    public BookingState getStateObject(BookingStatus bookingStatus){
        switch (bookingStatus){
            case ASSIGNING_DRIVER: return new AssigningDriverState();
            case SCHEDULED : return new ScheduledState();
            case ARRIVED: return new ArrivedDriverState();
            case IN_RIDE: return new InrideState();
            case COMPLETED: return new CompletedState();
            case CANCELLED: return new CancelledState();
            default: throw new IllegalStateException("Unknown state of booking");
        }
    }
}
