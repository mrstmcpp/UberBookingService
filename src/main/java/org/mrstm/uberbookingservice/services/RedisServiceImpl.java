package org.mrstm.uberbookingservice.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService{
    private final RedisTemplate<String , String> redisTemplate;
    private final RedisService redisService;

    public RedisServiceImpl(RedisTemplate<String, String> redisTemplate, RedisService redisService) {
        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
    }

    @Override
    public void setDriverBookingPair(String driverId, String bookingId) {
        try {
            redisTemplate.opsForValue().set(
                    "driver_booking:" + driverId,
                    bookingId,
                    2, TimeUnit.HOURS // auto expire mapping after 2 hours
            );
            System.out.println("Cached mapping: " + driverId + " " + bookingId);
        } catch (Exception e) {
            System.err.println("Failed to cache driver-booking mapping: " + e.getMessage());
        }
    }

    @Override
    public void deleteDriverBookingPair(String driverId) {
        try{
            redisTemplate.delete("driver_booking:" + driverId);
        } catch (Exception e) {
            System.err.println("Failed to delete driver-booking mapping: " + e.getMessage());
        }
    }
}
