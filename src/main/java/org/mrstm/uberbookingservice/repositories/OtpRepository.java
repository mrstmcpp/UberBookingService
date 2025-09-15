package org.mrstm.uberbookingservice.repositories;

import org.mrstm.uberentityservice.models.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OTP , Long> {
}
