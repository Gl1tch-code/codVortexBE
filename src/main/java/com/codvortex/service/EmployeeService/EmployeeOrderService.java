package com.codvortex.service.EmployeeService;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Country;
import com.codvortex.domain.Order;
import com.codvortex.dto.EmployeeDTOs.OrderEmployeeDTO;
import com.codvortex.dtoMappers.OrderMapper;
import com.codvortex.repository.CountryRepository;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeOrderService {
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CountryRepository countryRepository;

    public Page<OrderEmployeeDTO> getAllOrdersAsEmployee(String keyword, OrderStatusEnum status, OrderShippinStatusEnum shippingStatus, Boolean paymentStatus, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable, Long country, Long userId, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Page<Order> orders = orderRepository.findAsEmployeeByKeyword(
                userId,
                keyword,
                status,
                shippingStatus,
                country,
                paymentStatus,
                startDate,
                endDate,
                pageable);

        return orders.map(orderMapper::toEmployeeDTO);
    }

    public void updateAddress(Long id, String address, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        orderRepository.findById(id).ifPresent(order -> {
            order.setAddress(address);
        });
    }

    public void updateQuantity(Long id, Integer quant, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        orderRepository.findById(id).ifPresent(order -> {
            order.setQuantity(quant);
        });
    }

    public void updateStatus(Long id, String val, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus(OrderStatusEnum.valueOf(val));
        });
    }

    public void updateShippingStatus(Long id, String val, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        orderRepository.findById(id).ifPresent(order -> {
            order.setShippingStatus(OrderShippinStatusEnum.valueOf(val));
        });
    }

    public void updateMessage(Long id, String val, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        orderRepository.findById(id).ifPresent(order -> {
            order.setMessage(val);
        });
    }

    public void updateCountry(Long id, Long val, String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Country country = countryRepository.findById(val).orElseThrow(
                () -> new RuntimeException("Country Not Found")
        );

        orderRepository.findById(id).ifPresent(order -> {
            order.setCountry(country);
        });
    }

}
