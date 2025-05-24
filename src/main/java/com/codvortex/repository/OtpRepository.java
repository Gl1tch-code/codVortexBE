package com.codvortex.repository;

import com.codvortex.domain.Otp;
import com.codvortex.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByUserAndOtp(User user, String otp);

    void deleteByExpiryDateLessThan(LocalDateTime now);

    Optional<Otp> findByUser(User user);
}