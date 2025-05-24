package com.codvortex.service.auth;

import com.codvortex.commands.InitialSignupCommand;
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

    public User registerInitialUser(InitialSignupCommand initialSignupRequest) {

        if (userRepository.existsByEmail(initialSignupRequest.getEmail())){
            throw new IllegalArgumentException("هذا البريد موجود، المرجو تسجيل الدخول");
        }
        User user = new User();
        user.setEmail(initialSignupRequest.getEmail());
        user.setFullName(initialSignupRequest.getFullName());
        user.setPassword(passwordEncoder.encode(initialSignupRequest.getPassword()));

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
}