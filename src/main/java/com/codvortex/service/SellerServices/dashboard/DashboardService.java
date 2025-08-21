package com.codvortex.service.SellerServices.dashboard;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Order;
import com.codvortex.domain.Sourcing;
import com.codvortex.domain.User;
import com.codvortex.dto.DashboardOrdersSummaryDTO;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.SourcingRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final SourcingRepository sourcingRepository;

    public Integer getUserPerf(String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        YearMonth currentMonth = YearMonth.now();

        return orderRepository.countByUserIdAndShippingStatusAndOrderDateBetween(
                user.getId(),
                OrderShippinStatusEnum.DELIVERED,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
    }

    public BigDecimal getBalance(String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal balance = BigDecimal.ZERO;

        BigDecimal minus = new BigDecimal("4500");
        BigDecimal divisor = new BigDecimal("65");

        for (Order order : orderRepository.findAllByUserId(user.getId())) {
            if (order.getShippingStatus() == OrderShippinStatusEnum.DELIVERED) {

                if (order.getUser().getId().equals(user.getId())) {
                    if (order.getProduct().getUser().getId().equals(user.getId())) {
                        balance = balance.add(order.getPrice()
                                .subtract(minus)
                                .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        if (!order.getIsSellerPayed()) {
                            balance = balance.add(order.getPrice()
                                    .subtract(minus)
                                    .subtract(order.getProduct().getPrice())
                                    .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                        }
                    }
                } else if (order.getProduct().getUser().getId().equals(user.getId())) {
                    if (!order.getIsProductOwnerPayed()) {
                        balance = balance.add(order.getProduct().getPrice()
                                .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                    }
                }
            }
        }

        for (Sourcing userSourcing : sourcingRepository.findAllByUserIdAndIsShippingFeesPayedFalse(user.getId())) {
            balance = balance.subtract(userSourcing.getShippingFees().divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
        }

        return balance;
    }

    public DashboardOrdersSummaryDTO getOrdersSummary(String token, LocalDateTime startDate, LocalDateTime endDate, String country) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findAllByUserIdAndUpdatedAtBetweenAndCountryKey(user.getId(), startDate, endDate, country);

        return DashboardOrdersSummaryDTO.builder()
                .total(orders.size())
                .confirmed(orders.stream().filter(o -> o.getStatus() == OrderStatusEnum.CONFIRMED).toList().size())
                .pending(orders.stream().filter(o -> o.getStatus() == OrderStatusEnum.PENDING).toList().size())
                .canceled(orders.stream().filter(o -> o.getStatus() == OrderStatusEnum.CANCELLED).toList().size())
                .delivered(orders.stream().filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.DELIVERED).toList().size())
                .shipping(orders.stream().filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.SHIPPED).toList().size())
                .returned(orders.stream().filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.RETURNED).toList().size())
                .reprogrammed(orders.stream().filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.REPROGRAMMED).toList().size())
                .build();

    }

}
