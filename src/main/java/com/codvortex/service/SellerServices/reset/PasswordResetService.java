package com.codvortex.service.SellerServices.reset;

import com.codvortex.domain.Otp;
import com.codvortex.domain.User;
import com.codvortex.repository.OtpRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.service.SellerServices.email.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpRepository otpRepository;

    public void requestOtp(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email address"));

        String otpValue = generateOtp();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        Otp otp = new Otp();
        otp.setUser(user);
        otp.setOtp(otpValue);
        otp.setExpiryDate(expiryDate);

        otpRepository.save(otp);

        emailService.sendOtpEmail(email, otpValue, user.getUsername(), user.getFullName());
    }

    public String verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Optional<Otp> otpOptional = otpRepository.findByUserAndOtp(user, otp);

        if (otpOptional.isEmpty()) {
            throw new IllegalArgumentException("Wrong OTP");
        }

        Otp otpData = otpOptional.get();

        if (otpData.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);

        otpRepository.delete(otpData);

        return updatedUser.getEmail();
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}