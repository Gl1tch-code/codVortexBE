package com.codvortex.service.SellerServices.dashboard;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Order;
import com.codvortex.domain.Sourcing;
import com.codvortex.domain.User;
import com.codvortex.dto.DashboardOrdersSummaryDTO;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.SourcingRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.Constants;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import com.codvortex.utils.RoleEnum;
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

        BigDecimal minus = Constants.DELIVERY_FEES;
        BigDecimal divisor = Constants.CHANGE;

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

    public DashboardOrdersSummaryDTO getOrdersSummary(
            String token,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String country,
            Long productId,
            RoleEnum apiRole) {

        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (apiRole == RoleEnum.ADMIN) {
            if (!user.getRole().equals(RoleEnum.ADMIN)) {
                throw new RuntimeException("Cant call this api with other than an ADMIN user !");
            }
        }

        // 1. Keep fetching from DB based on updatedAt as you already do
        List<Order> allOrdersFromDb = orderRepository.findAllByUserIdAndUpdatedAtBetweenAndCountryKey(
                apiRole == RoleEnum.ADMIN ? null : user.getId(), startDate, endDate, country, productId);

        // 2. Define the logic: Does this order "belong" to this time period?
        List<Order> filteredOrders = allOrdersFromDb.stream()
                .filter(o -> {
                    boolean isActionState = o.getStatus() == OrderStatusEnum.CONFIRMED
                            || o.getStatus() == OrderStatusEnum.POSTPONED
                            || o.getShippingStatus() == OrderShippinStatusEnum.DELIVERED
                            || o.getShippingStatus() == OrderShippinStatusEnum.SHIPPED;

                    if (isActionState) {
                        // For these, we only care that they were UPDATED in this range
                        // Since the DB query already filtered by updatedAt, we just return true
                        return true;
                    } else {
                        // For others (Pending/Canceled), we only want them if they were CREATED in this range
                        return o.getDate().isAfter(startDate) && o.getDate().isBefore(endDate);
                    }
                })
                .toList();

        // 3. Build the DTO using the same counting logic as before
        return DashboardOrdersSummaryDTO.builder()
                .total(filteredOrders.size())
                .pending((int) filteredOrders.stream()
                        .filter(o -> o.getStatus() == OrderStatusEnum.PENDING
                                || o.getStatus() == OrderStatusEnum.NO_REPLY
                                || o.getStatus() == OrderStatusEnum.UNREACHABLE)
                        .count())
                .canceled((int) filteredOrders.stream()
                        .filter(o -> o.getStatus() == OrderStatusEnum.CANCELLED)
                        .count())
                .confirmed((int) filteredOrders.stream()
                        .filter(o -> o.getStatus() == OrderStatusEnum.CONFIRMED)
                        .count())
                .delivered((int) filteredOrders.stream()
                        .filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.DELIVERED)
                        .count())
                .shipping((int) filteredOrders.stream()
                        .filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.SHIPPED
                                || o.getShippingStatus() == OrderShippinStatusEnum.PREPARING)
                        .count())
                .returned((int) filteredOrders.stream()
                        .filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.RETURNED
                                || o.getShippingStatus() == OrderShippinStatusEnum.CANCELLED)
                        .count())
                .reprogrammed((int) filteredOrders.stream()
                        .filter(o -> o.getShippingStatus() == OrderShippinStatusEnum.REPROGRAMMED
                                || o.getShippingStatus() == OrderShippinStatusEnum.NO_REPLY
                                || o.getShippingStatus() == OrderShippinStatusEnum.POSTPONED
                                || o.getShippingStatus() == OrderShippinStatusEnum.UNREACHABLE)
                        .count())
                .postponed((int) filteredOrders.stream()
                        .filter(o -> o.getStatus() == OrderStatusEnum.POSTPONED)
                        .count())
                .build();
    }

}
