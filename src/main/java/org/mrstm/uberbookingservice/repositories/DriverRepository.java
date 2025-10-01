package org.mrstm.uberbookingservice.repositories;

import org.mrstm.uberentityservice.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Driver d SET d.activeBooking = null WHERE d.id = :driverId")
    void clearActiveBooking(@Param("driverId") Long driverId);
}
