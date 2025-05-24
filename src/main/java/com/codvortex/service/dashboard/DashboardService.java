package com.codvortex.service.dashboard;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Order;
import com.codvortex.domain.User;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public BigDecimal getBalance(String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal balance = BigDecimal.ZERO;

        for (Order order : orderRepository.findAllByUserId(user.getId())) {
            balance = balance.add(order.getPrice());
        }

        //TODO: reduce withdraw amount

        return balance;
    }


}
