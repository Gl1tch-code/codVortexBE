package com.codvortex.api;

import com.codvortex.commands.InitialSignupCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.User;
import com.codvortex.dto.AuthDTO;
import com.codvortex.service.auth.AuthenticationService;
import com.codvortex.service.reset.PasswordResetService;
import com.codvortex.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/auth")
public class AuthResource {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTO> login(@RequestParam String username, @RequestParam String password) throws NoSuchAlgorithmException {
        User user = authenticationService.login(username, password);
        String token = jwtTokenService.generateToken(user.getEmail());
        AuthDTO authDTO = AuthDTO.builder().email(user.getEmail()).role(user.getRole()).username(user.getFullName()).token(token).build();

        return ResponseEntity.ok(authDTO);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Object> initialSignup(@RequestBody InitialSignupCommand initialSignupRequest) {
        try {
            User user = authenticationService.registerInitialUser(initialSignupRequest);
            String token = jwtTokenService.generateToken(user.getEmail());
            AuthDTO authDTO = AuthDTO.builder().email(user.getEmail()).role(user.getRole()).username(user.getFullName()).token(token).build();

            return ResponseEntity.ok(authDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/otp-request")
    public ResponseEntity<String> requestOtp(@RequestBody String email) {
        try {
            passwordResetService.requestOtp(email);
            return ResponseEntity.ok("OTP sent to your email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred.");
        }
    }

    @PostMapping("/otp-verify")
    public ResponseEntity<AuthDTO> verifyOtp(@RequestParam String email, @RequestParam String otp, @RequestBody String newPassword) throws NoSuchAlgorithmException {
        String token = jwtTokenService.generateToken(passwordResetService.verifyOtpAndResetPassword(email, otp, newPassword));
        User user = userService.findByEmail(email);
        AuthDTO authDTO = AuthDTO.builder().email(user.getEmail()).username(user.getFullName()).token(token).build();
        return ResponseEntity.ok(authDTO);
    }
}
