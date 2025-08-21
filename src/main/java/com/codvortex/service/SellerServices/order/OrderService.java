package com.codvortex.service.SellerServices.order;

import com.codvortex.commands.OrderCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Country;
import com.codvortex.domain.Order;
import com.codvortex.domain.Product;
import com.codvortex.domain.User;
import com.codvortex.dto.OrderDTO;
import com.codvortex.dtoMappers.OrderMapper;
import com.codvortex.repository.CountryRepository;
import com.codvortex.repository.OrderRepository;
import com.codvortex.repository.ProductRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CountryRepository countryRepository;

    public Page<OrderDTO> getAllOrders(String keyword, OrderStatusEnum status, OrderShippinStatusEnum shippingStatus, Pageable pageable, String country, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Page<Order> orders = orderRepository.findByKeyword(user.getId(), keyword, status, shippingStatus, country, pageable);
        return orders.map(orderMapper::toDTO);
    }

    public Order saveOrder(OrderCommand command, String countryKey, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));

        Country country = countryRepository.findByKeyIgnoreCase(countryKey.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryKey));

        Product product = productRepository.findById(command.getProduct()).orElseThrow(() -> new RuntimeException("Product id not found"));

        Order order = new Order();
        order.setUser(user);
        order.setAddress(command.getAddress());
        order.setName(command.getCustomerName());
        order.setDate(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setShippingStatus(OrderShippinStatusEnum.PENDING);
        order.setPhoneNumber(command.getPhoneNumber());
        order.setPrice(BigDecimal.valueOf(command.getPrice()));
        order.setQuantity(command.getQuantite());
        order.setIsSellerPayed(false);
        order.setIsProductOwnerPayed(false);
        order.setCountry(country);
        order.setProduct(product);

        if (command.getIsAvailableStock().equals(true)) {
            if (product.getUser().getId().equals(user.getId())) {
                order.setIsAvailableStock(false);
            } else {
                order.setIsAvailableStock(command.getIsAvailableStock());
            }
        } else {
            order.setIsAvailableStock(command.getIsAvailableStock());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public void importFromCsv(MultipartFile file, String token, String countryKey) throws Exception {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));

        Country country = countryRepository.findByKeyIgnoreCase(countryKey.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryKey));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // skip header
                    continue;
                }

                // Simple CSV splitting on comma, not perfect but works if no commas inside fields
                String[] fields = line.split(",");

                if (fields.length < 6) {
                    // Invalid row, skip or throw error
                    continue;
                }

                String name = fields[0].trim();
                String phoneNumber = fields[1].trim();
                String priceRaw = fields[2].replaceAll("[^\\d.]", "");
                String address = fields[3].trim();
                String productIdsRaw = fields[4].trim();
                String quantity = fields[5].trim();

                if (name.isEmpty() || phoneNumber.isEmpty() || priceRaw.isEmpty()) continue;

                BigDecimal price = new BigDecimal(priceRaw);

                Order order = new Order();
                order.setName(name);
                order.setPhoneNumber(phoneNumber);
                order.setPrice(price);
                order.setAddress(address);
                order.setDate(LocalDateTime.now());
                order.setStatus(OrderStatusEnum.PENDING);
                order.setShippingStatus(OrderShippinStatusEnum.PENDING);
                order.setUser(user);
                order.setCountry(country);
                order.setIsSellerPayed(false);
                order.setIsProductOwnerPayed(false);
                System.out.println("ss -----------------------------------------------------" + productIdsRaw);
                System.out.println("ss -----------------------------------------------------" + Long.parseLong(productIdsRaw));
                order.setProduct(productRepository.findById(Long.parseLong(productIdsRaw)).orElseThrow(() -> new RuntimeException("Product id not found")));
                order.setQuantity(Integer.parseInt(quantity));

                Product product = productRepository.findById(Long.parseLong(productIdsRaw))
                        .orElseThrow(() -> new RuntimeException("Product id not found"));

                order.setIsAvailableStock(!user.getId().equals(product.getUser().getId()));

                orderRepository.save(order);
            }
        }
    }


}
