package com.codvortex.service.sourcing;

import com.codvortex.commands.SourcingCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.*;
import com.codvortex.dto.SourcingDTO;
import com.codvortex.dtoMappers.SourcingMapper;
import com.codvortex.repository.*;
import com.codvortex.utils.SourcingPaymentStatusEnum;
import com.codvortex.utils.SourcingStatusEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional
public class SourcingService {

    private final SourcingRepository sourcingRepository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final SourcingMapper sourcingMapper;
    private final CountryRepository countryRepository;
    private final ProductRepository productRepository;
    private final SourcingProductsRepository sourcingProductsRepository;

    public Page<SourcingDTO> gatAll(String keyword, Pageable pageable, String country, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Page<Sourcing> sourcings = sourcingRepository.findByKeyword(user.getId(), keyword, country, pageable);

        return sourcings.map(s -> {
            SourcingDTO sourcingDTO = sourcingMapper.toDTO(s);
            sourcingDTO.setProductsCount(s.getSourcingProducts().size());
            AtomicInteger totalProductsQuantity = new AtomicInteger(0);

            s.getSourcingProducts().forEach(p -> {
                totalProductsQuantity.addAndGet(p.getQuantity());
            });

            sourcingDTO.setTotalProductsQuantity(totalProductsQuantity.get());

            return sourcingDTO;
        });
    }

    public void create(SourcingCommand sourcingCommand, String countryKey, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));

        Country country = countryRepository.findByKeyIgnoreCase(countryKey.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryKey));

        Sourcing sourcing = sourcingRepository.save(
                Sourcing.builder()
                        .title(sourcingCommand.getTitle())
                        .message(sourcingCommand.getMessage())
                        .user(user)
                        .country(country)
                        .status(SourcingStatusEnum.NEW)
                        .paymentStatus(SourcingPaymentStatusEnum.NOT_PAYED)
                        .isShippingFeesPayed(false)
                        .build()
        );

        sourcingCommand.getProducts().forEach(sp -> {
            Product product = productRepository.save(
                    Product.builder()
                            .name(sp.getName())
                            .country(country)
                            .user(user)
                            .isAvailableStock(false)
                            .quantity(0)
                            .build()
            );

            sourcingProductsRepository.save(
                    SourcingProducts.builder()
                            .product(product)
                            .createdAt(LocalDateTime.now())
                            .sourcing(sourcing)
                            .link(sp.getLink())
                            .quantity(sp.getQuantity())
                            .build()
            );
        });
    }
}
