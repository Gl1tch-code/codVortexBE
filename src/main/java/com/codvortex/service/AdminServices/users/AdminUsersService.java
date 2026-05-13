package com.codvortex.service.AdminServices.users;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.File;
import com.codvortex.domain.Order;
import com.codvortex.domain.Sourcing;
import com.codvortex.domain.User;
import com.codvortex.dto.AdminDTOs.ProductDetailsDTO;
import com.codvortex.dto.AdminDTOs.SourcingDetailsDTO;
import com.codvortex.dto.AdminDTOs.UserDTO;
import com.codvortex.dto.AdminDTOs.UserDetailsDTO;
import com.codvortex.dto.SourcingDTO;
import com.codvortex.dtoMappers.ProductMapper;
import com.codvortex.dtoMappers.SourcingMapper;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.ProductRepository;
import com.codvortex.repository.SourcingRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.Constants;
import com.codvortex.utils.OrderShippinStatusEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AdminUsersService {
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final SourcingRepository sourcingRepository;
    private final SourcingMapper sourcingMapper;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final OrderRepository orderRepository;

    public Page<UserDTO> getUsers(String keyword, Pageable pageable, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        Page<User> users = userRepository.findByKeyword(keyword, pageable);

        return users.map(au -> UserDTO.builder()
                .balance(getUserBalance(au.getId()))
                .id(au.getId())
                .email(au.getEmail())
                .fullName(au.getFullName())
                .isActive(au.isActive())
                .phoneNumber(au.getPhoneNumber())
                .isAccountManagerAssigned(au.getIsAccountManagerAssigned())
                .build());
    }

    @Transactional
    public UserDetailsDTO getUserDetails(Long id, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not fount"));

        return UserDetailsDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.isActive())
                .phoneNumber(user.getPhoneNumber())
                .isAccountManagerAssigned(user.getIsAccountManagerAssigned())
                .sourcing(user.getSourcings().stream().map(s -> {
                    SourcingDTO sourcingDTO = sourcingMapper.toDTO(s);
                    sourcingDTO.setProductsCount(s.getSourcingProducts().size());
                    AtomicInteger totalProductsQuantity = new AtomicInteger(0);

                    s.getSourcingProducts().forEach(p -> {
                        totalProductsQuantity.addAndGet(p.getQuantity());
                    });

                    sourcingDTO.setTotalProductsQuantity(totalProductsQuantity.get());

                    return sourcingDTO;
                }).toList())
                .filesIds(user.getFiles().stream().map(File::getId).toList())
                .build();
    }

    public SourcingDetailsDTO getUserSourcingDetails(Long sourcingId, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        Sourcing sourcing = sourcingRepository.findById(sourcingId).orElseThrow(() -> new RuntimeException("Not found"));

        SourcingDetailsDTO sourcingDetailsDTO = sourcingMapper.toDetailsDTO(sourcing);
        sourcingDetailsDTO.setProducts(productRepository.findAllBySourcingId(sourcing.getId()).stream()
                .map(p -> {
                    ProductDetailsDTO productDetailsDTO = productMapper.toDetailsDTO(p);
                    productDetailsDTO.setSourcingProductQuantity(p.getSourcingProducts().get(0).getQuantity());

                    AtomicInteger totalSourced = new AtomicInteger();
                    AtomicInteger totalDelivered = new AtomicInteger();

                    p.getSourcingProducts().forEach(sp -> {
                        totalSourced.addAndGet(sp.getQuantity());
                    });
                    p.getOrders().forEach(po -> {
                        if (po.getShippingStatus().equals(OrderShippinStatusEnum.DELIVERED)) {
                            totalDelivered.addAndGet(po.getQuantity());
                        }
                    });

                    productDetailsDTO.setStock(totalSourced.get() - totalDelivered.get() - p.getQuantity());

                    return productDetailsDTO;
                })
                .toList());

        return sourcingDetailsDTO;
    }

    public void toggleUserActivation(Long id, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not fount"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void toggleIsAccountManagerAssigned(Long id, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not fount"));

        user.setIsAccountManagerAssigned(!user.getIsAccountManagerAssigned());
        userRepository.save(user);
    }


    public BigDecimal getUserBalance(Long id) {
        User user = userRepository.findById(id)
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


}
