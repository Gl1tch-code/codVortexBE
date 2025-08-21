package com.codvortex.service.SellerServices.invoice;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Invoice;
import com.codvortex.domain.Order;
import com.codvortex.domain.Sourcing;
import com.codvortex.domain.User;
import com.codvortex.dto.InvoiceDTO;
import com.codvortex.dtoMappers.InvoiceMapper;
import com.codvortex.repository.InvoiceRepository;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.SourcingRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.InvoiceStatus;
import com.codvortex.utils.OrderShippinStatusEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final InvoiceMapper invoiceMapper;
    private final OrderRepository orderRepository;
    private final SourcingRepository sourcingRepository;

    public Page<InvoiceDTO> getAllInvoices(String q, InvoiceStatus status, Pageable pageable, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Page<Invoice> invoices = invoiceRepository.findByKeyword(q, user.getId(), status, pageable);
        return invoices.map(inv -> {
            InvoiceDTO invDTO = invoiceMapper.toDTO(inv);
            invDTO.setOrdersCount(inv.getSellerOrders().size());
            return invDTO;
        });
    }

    public void createInvoice(String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));

        List<Order> sellerOrders = new ArrayList<>();
        List<Order> sellerOrdersFromDrop = new ArrayList<>();
        List<Order> sellerSoldProductsInDrop = new ArrayList<>();

        BigDecimal balance = BigDecimal.ZERO;

        BigDecimal minus = new BigDecimal("4500");
        BigDecimal divisor = new BigDecimal("65");

        for (Order order : orderRepository.findAllByUserId(user.getId())) {
            if (order.getShippingStatus() == OrderShippinStatusEnum.DELIVERED) {

                if (order.getUser().getId().equals(user.getId())) {
                    if (order.getProduct().getUser().getId().equals(user.getId())) {
                        sellerOrders.add(order);
                        balance = balance.add(order.getPrice()
                                .subtract(minus)
                                .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        if (!order.getIsSellerPayed()) {
                            sellerOrdersFromDrop.add(order);
                            balance = balance.add(order.getPrice()
                                    .subtract(minus)
                                    .subtract(order.getProduct().getPrice())
                                    .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                        }
                    }
                } else if (order.getProduct().getUser().getId().equals(user.getId())) {
                    if (!order.getIsProductOwnerPayed()) {
                        sellerSoldProductsInDrop.add(order);
                        balance = balance.add(order.getProduct().getPrice()
                                .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                    }
                }
            }
        }

        List<Sourcing> shippingNotPayedSourcing = sourcingRepository.findAllByUserIdAndIsShippingFeesPayedFalse(user.getId());

        for (Sourcing userSourcing : shippingNotPayedSourcing) {
            balance = balance.subtract(userSourcing.getShippingFees().divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
        }

        if (balance.doubleValue() > 200) {
            shippingNotPayedSourcing.forEach(sourcing -> {
                sourcing.setIsShippingFeesPayed(true);
            });
            List<Order> allSellerOrders = new ArrayList<>(sellerOrders);
            allSellerOrders.addAll(sellerOrdersFromDrop);

            List<Order> allProductOwnerOrders = new ArrayList<>(sellerOrders);
            allProductOwnerOrders.addAll(sellerSoldProductsInDrop);

            Invoice invoice = invoiceRepository.save(
                    Invoice.builder()
                            .status(InvoiceStatus.WAITING)
                            .updatedAt(LocalDateTime.now())
                            .user(user)
                            .sellerOrders(allSellerOrders)
                            .totalPrice(balance)
                            .productOwnerOrders(allProductOwnerOrders)
                            .build()
            );

            sellerOrders.forEach(order -> {
                order.setSellerInvoice(invoice);
                order.setProductOwnerInvoice(invoice);
                order.setIsSellerPayed(true);
                order.setIsProductOwnerPayed(true);
            });
            sellerOrdersFromDrop.forEach(order -> {
                order.setSellerInvoice(invoice);
                order.setIsSellerPayed(true);
            });
            sellerSoldProductsInDrop.forEach(order -> {
                order.setProductOwnerInvoice(invoice);
                order.setIsProductOwnerPayed(true);
            });
        } else {
            throw new RuntimeException("You dont have enough money");
        }

    }
}
