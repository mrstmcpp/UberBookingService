package org.mrstm.uberbookingservice.services;

import org.springframework.stereotype.Service;

@Service
public interface RedisService {
    void setDriverBookingPair(String driverId, String passengerId);
    void deleteDriverBookingPair(String driverId);

}
