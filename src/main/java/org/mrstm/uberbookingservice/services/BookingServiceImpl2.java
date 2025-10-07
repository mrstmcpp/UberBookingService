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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl2 implements BookingService {
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final OtpRepository otpRepository;
    private final RestTemplate restTemplate;
    private final LocationServiceApi locationServiceApi;
    private final DriverRepository driverRepository;
    private final SocketApi socketApi;
    private final KafkaService kafkaService;


    public BookingServiceImpl2(BookingRepository bookingRepository,
                               PassengerRepository passengerRepository, OtpRepository otpRepository,
                               RestTemplate restTemplate ,
                               LocationServiceApi locationServiceApi,
                               DriverRepository driverRepository ,
                               SocketApi socketApi, KafkaService kafkaService) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.otpRepository = otpRepository;
        this.restTemplate = restTemplate;
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
        this.socketApi = socketApi;
        this.kafkaService = kafkaService;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto bookingDetails) {
        Passenger p = passengerRepository.findById(bookingDetails.getPassengerId()).orElseThrow(() -> new RuntimeException("Passenger not found with id: " + bookingDetails.getPassengerId()));
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .passenger(p)
                .build();
        NearbyDriversRequestDto req = NearbyDriversRequestDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();

        Booking newBooking = bookingRepository.save(booking);
        passengerRepository.setActiveBooking(p.getId() , newBooking);


        //changing to kafka
        processNearbyDriverAsync(req , bookingDetails.getPassengerId() , newBooking.getId());

//        ResponseEntity<DriverLocationDto[]> driverList = restTemplate.postForEntity(LOCATION_SERVICE + "/api/location/nearby/drivers", req , DriverLocationDto[].class);
//        if(driverList.getStatusCode().is2xxSuccessful() && driverList.getBody() != null) {
//            List<DriverLocationDto> driverLocations = Arrays.asList(driverList.getBody());
//            driverLocations.forEach(location -> {
//                System.out.println(location.getDriverId() + " " + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
//            });
//        }
//
//
        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(newBooking.getDriver()))
                .build();
    }



    private void processNearbyDriverAsync(NearbyDriversRequestDto nearbyDriversRequestDto , Long passengerId , Long bookingId) {
        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(bookingId.toString())
                .passengerId(passengerId.toString())
                .latitude(nearbyDriversRequestDto.getLatitude())
                .longitude(nearbyDriversRequestDto.getLongitude())
                .build();
        kafkaService.publishBookingCreated(bookingId.toString() , event);
        System.out.println("BookingCreatedEvent published for bookingId: " + bookingId);
    }


    @Override
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingDetails, Long bookingId) {
        try {
            Driver driver = driverRepository.findById(bookingDetails.getDriverId())
                    .orElseThrow(() -> new NotFoundException("Driver not found with ID: " + bookingDetails.getDriverId()));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));

            if (booking.getBookingStatus() == BookingStatus.SCHEDULED) {
                System.out.println("Driver is already assigned for this booking.");
                return null;
            }

            bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.SCHEDULED, driver);
            //otp generation

            OTP otp = OTP.make(booking);
            otpRepository.save(otp);
//            System.out.println(otp.getCode());
            // update in-memory object for consistency
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
            notifyPassenger(notificationDTO , booking.getPassenger().getId().toString());
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
    public String cancelBooking(CancelBookingRequestDto cancelBookingRequestDto) {
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
    public String completeBooking(Long bookingId , CompleteBookingRequestDto bookingCompleteRequestDto) {
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




    private void notifyPassenger(NotificationDTO notificationDTO , String passengerId){
        Call<Boolean> call = socketApi.notifyPassenger(notificationDTO , passengerId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Boolean res = response.body();
                    System.out.println("Passenger Notified. " + res);
                }else {
                    System.out.println("Notification Failed " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {
                System.out.println("Failed to send notification to passenger.");
            }
        });
    }


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
        BookingContext booking = new BookingContext(bookingRepository , passengerRepository, driverRepository);

//        Booking dbBooking = bookingRepository.getBookingById(bookingRequestDto.getBookingId());
        BookingStatus currentStatus = bookingRepository.getBookingStatusById(bookingRequestDto.getBookingId());

//        System.out.println("Current for : " + dbBooking.getId() + " -> " + currentStatus);
        System.out.println("Requested : " + bookingRequestDto.getBookingStatus());


        booking.setState(getStateObject(currentStatus)); // database state would be here

        try {
            booking.updateStatus(bookingRequestDto.getBookingStatus() , bookingRequestDto.getBookingId() , bookingRequestDto);
            bookingRepository.updateBookingStatus(
                    bookingRequestDto.getBookingId(),
                    bookingRequestDto.getBookingStatus()
            );

            return UpdateBookingResponseDto.builder()
                    .bookingStatus(booking.getStatus())
                    .bookingId(bookingRequestDto.getBookingId())
                    .build();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Transition not allowed: " + currentStatus + " -> " + bookingRequestDto.getBookingStatus());
        }
    }



    public BookingState getStateObject(BookingStatus bookingStatus){
        switch (bookingStatus){
            case SCHEDULED : return new ScheduledState();
            case ASSIGNING_DRIVER: return new AssigningDriverState();
            case ARRIVED: return new ArrivedDriverState();
            case IN_RIDE: return new InrideState();
            case COMPLETED: return new CompletedState();
            case CANCELLED: return new CancelledState();
            default: throw new IllegalStateException("Unknown state of booking");
        }
    }
}
