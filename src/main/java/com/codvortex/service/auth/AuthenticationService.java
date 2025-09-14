package com.codvortex.service.auth;

import com.codvortex.commands.InitialSignupCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.User;
import com.codvortex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public User registerInitialUser(InitialSignupCommand initialSignupRequest) {

        if (userRepository.existsByEmail(initialSignupRequest.getEmail())) {
            throw new IllegalArgumentException("User already exists, please LogIn");
        }
        User user = new User();
        user.setEmail(initialSignupRequest.getEmail());
        user.setFullName(initialSignupRequest.getFullName());
        user.setPassword(passwordEncoder.encode(initialSignupRequest.getPassword()));
        user.setPhoneNumber(initialSignupRequest.getPhoneNumber());
        user.setActive(false);
        user.setIsAccountManagerAssigned(false);

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            throw new IllegalStateException("Invalid password");
        }
    }

    public Boolean checkActivation(String token) {
//        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        return user.isActive();
        return true;
    }

    public void activateUser(String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
    }


    // admin


    public User loginAdmin(String username, String password) {
        User user = userRepository.findByUsernameAndAdmin(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            throw new IllegalStateException("Invalid password");
        }
    }


    // Employee


    public User loginEmployee(String username, String password) {
        User user = userRepository.findByUsernameAndEmployeeOrAdmin(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            throw new IllegalStateException("Invalid password");
        }
    }

}