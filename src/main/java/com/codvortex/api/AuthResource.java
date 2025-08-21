package com.codvortex.api;

import com.codvortex.commands.InitialSignupCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.User;
import com.codvortex.dto.AuthDTO;
import com.codvortex.dto.UserBillings;
import com.codvortex.repository.UserRepository;
import com.codvortex.service.auth.AuthenticationService;
import com.codvortex.service.SellerServices.reset.PasswordResetService;
import com.codvortex.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthDTO> login(@RequestParam String username, @RequestParam String password) {
        User user = authenticationService.login(username, password);
        String token = jwtTokenService.generateToken(user.getEmail());
        AuthDTO authDTO = AuthDTO.builder()
                .isAccountManagerAssigned(user.getIsAccountManagerAssigned())
                .rib(user.getRib())
                .bankName(user.getBankName())
                .email(user.getEmail()).role(user.getRole()).username(user.getFullName()).token(token).build();

        return ResponseEntity.ok(authDTO);
    }

    @PostMapping("/update-billings")
    public ResponseEntity<AuthDTO> updateBillings(@RequestHeader("Authorization") String authHeader, @RequestBody UserBillings userBillings) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(authHeader))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenService.generateToken(user.getEmail());

        user.setRib(userBillings.getRib());
        user.setBankName(userBillings.getBankName());
        userRepository.save(user);

        AuthDTO authDTO = AuthDTO.builder()
                .isAccountManagerAssigned(user.getIsAccountManagerAssigned())
                .rib(user.getRib())
                .bankName(user.getBankName())
                .email(user.getEmail()).role(user.getRole()).username(user.getFullName()).token(token).build();

        return ResponseEntity.ok(authDTO);

    }

    @PostMapping("/sign-up")
    public ResponseEntity<Object> initialSignup(@RequestBody InitialSignupCommand initialSignupRequest) {
        try {
            User user = authenticationService.registerInitialUser(initialSignupRequest);
            String token = jwtTokenService.generateToken(user.getEmail());
            AuthDTO authDTO = AuthDTO.builder()
                    .isAccountManagerAssigned(user.getIsAccountManagerAssigned())
                    .rib(user.getRib())
                    .bankName(user.getBankName())
                    .email(user.getEmail()).role(user.getRole()).username(user.getFullName()).token(token).build();

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
    public ResponseEntity<AuthDTO> verifyOtp(@RequestParam String email, @RequestParam String otp, @RequestBody String newPassword) {
        String token = jwtTokenService.generateToken(passwordResetService.verifyOtpAndResetPassword(email, otp, newPassword));
        User user = userService.findByEmail(email);
        AuthDTO authDTO = AuthDTO.builder()
                .isAccountManagerAssigned(user.getIsAccountManagerAssigned())
                .rib(user.getRib())
                .bankName(user.getBankName())
                .email(user.getEmail()).username(user.getFullName()).token(token).build();
        return ResponseEntity.ok(authDTO);
    }

    @GetMapping("/check-token")
    public ResponseEntity<Boolean> checkToken(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(jwtTokenService.validateToken(authHeader));
    }

    @GetMapping("/check-account-manager")
    public ResponseEntity<Boolean> checkAccountManagerAssigned(@RequestHeader("Authorization") String authHeader) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(authHeader))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user.getIsAccountManagerAssigned());
    }

    @GetMapping("/check-activation")
    public ResponseEntity<Boolean> checkActivation(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authenticationService.checkActivation(authHeader));
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@RequestHeader("Authorization") String authHeader) {
        authenticationService.activateUser(authHeader);
        return ResponseEntity.ok().build();
    }


    // admin


    @PostMapping("/login/admin")
    public ResponseEntity<AuthDTO> loginAdmin(@RequestParam String username, @RequestParam String password) {
        User user = authenticationService.loginAdmin(username, password);
        String token = jwtTokenService.generateToken(user.getEmail());
        AuthDTO authDTO = AuthDTO.builder()
                .isAccountManagerAssigned(user.getIsAccountManagerAssigned())
                .rib(user.getRib())
                .bankName(user.getBankName())
                .email(user.getEmail()).role(user.getRole()).username(user.getFullName()).token(token).build();

        return ResponseEntity.ok(authDTO);
    }

    @GetMapping("/check-token-admin")
    public ResponseEntity<Boolean> checkTokenAdmin(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(jwtTokenService.validateTokenAdmin(authHeader));
    }

}
