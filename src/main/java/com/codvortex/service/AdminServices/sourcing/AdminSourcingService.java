package com.codvortex.service.AdminServices.sourcing;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Sourcing;
import com.codvortex.dto.SourcingDTO;
import com.codvortex.dtoMappers.SourcingMapper;
import com.codvortex.repository.CountryRepository;
import com.codvortex.repository.SourcingRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.SourcingPaymentStatusEnum;
import com.codvortex.utils.SourcingStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AdminSourcingService {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final SourcingRepository sourcingRepository;
    private final CountryRepository countryRepository;
    private final SourcingMapper sourcingMapper;

    public Page<SourcingDTO> getAll(Long userId, String keyword, String countryKey, SourcingStatusEnum status, String token, Pageable pageable) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        Page<Sourcing> sourcings = sourcingRepository.findByAdminKeyword(userId, keyword, countryKey, status, pageable);

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

    public Sourcing getSourcing(Long id, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        return sourcingRepository.findById(id).orElseThrow(() -> new RuntimeException("Sourcing not found"));
    }

    public void changeStatus(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setStatus(SourcingStatusEnum.valueOf(value));
        sourcingRepository.save(sourcing);
    }

    public void changePaymentStatus(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setPaymentStatus(SourcingPaymentStatusEnum.valueOf(value));
        sourcingRepository.save(sourcing);
    }

    public void changeTotalPrice(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setTotalPrice(BigDecimal.valueOf(Double.parseDouble(value)));
        sourcingRepository.save(sourcing);
    }

    public void changeShippingFees(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setShippingFees(BigDecimal.valueOf(Double.parseDouble(value)));
        sourcingRepository.save(sourcing);
    }

    public void changeInvoice(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setInvoice(value);
        sourcingRepository.save(sourcing);
    }

    public void changePaymentInvoice(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setPaymentTransferInvoice(value);
        sourcingRepository.save(sourcing);
    }

    public void changeCountry(Long id, String value, String token) {
        Sourcing sourcing = getSourcing(id, token);
        sourcing.setCountry(countryRepository.findById(Long.parseLong(value)).orElseThrow(
                () -> new RuntimeException("Country not found")
        ));
        sourcingRepository.save(sourcing);
    }

}
