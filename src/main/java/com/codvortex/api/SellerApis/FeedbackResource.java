package com.codvortex.api.SellerApis;

import com.codvortex.commands.RatingFeedbackCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Feedback;
import com.codvortex.domain.User;
import com.codvortex.repository.FeedbackRepository;
import com.codvortex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackResource {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;


    @PostMapping
    public ResponseEntity<Void> receiveFeedback(@RequestBody RatingFeedbackCommand dto, @RequestHeader("Authorization") String authHeader) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(authHeader)).orElseThrow(() -> new RuntimeException("User Not Found"));

        Feedback feedback = Feedback.builder()
                .rating(dto.getRating())
                .feedback(dto.getFeedback())
                .user(user)
                .build();

        feedbackRepository.save(feedback);
        return ResponseEntity.ok().build();
    }
}
