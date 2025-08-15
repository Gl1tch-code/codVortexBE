package com.codvortex.service.product;

import com.codvortex.commands.ProductSwitchDropCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Product;
import com.codvortex.domain.User;
import com.codvortex.dto.ProductDropDTO;
import com.codvortex.dto.ProductPageDTO;
import com.codvortex.dtoMappers.ProductMapper;
import com.codvortex.repository.ProductRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.utils.OrderShippinStatusEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductPageDTO> getAll(String keyword, Pageable pageable, String country, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Page<Product> products = productRepository.findByKeyword(user.getId(), keyword, country, false, pageable);
        return products.map(p -> {
            AtomicInteger totalSourced = new AtomicInteger();
            AtomicInteger totalDelivered = new AtomicInteger();
            ProductPageDTO productPageDTO = productMapper.toDTO(p);

            p.getSourcingProducts().forEach(sp -> {
                totalSourced.addAndGet(sp.getQuantity());
            });
            p.getOrders().forEach(po -> {
                if (po.getShippingStatus().equals(OrderShippinStatusEnum.DELIVERED)) {
                    totalDelivered.addAndGet(po.getQuantity());
                }
            });

            productPageDTO.setStock(totalSourced.get() - totalDelivered.get() - p.getQuantity());

            if (!p.getSourcingProducts().isEmpty()) {
                productPageDTO.setStatus(p.getSourcingProducts().get(0).getSourcing().getStatus());
            }

            return productPageDTO;
        });
    }

    public Page<ProductDropDTO> getAllAvailableStock(String keyword, Pageable pageable, String country) {
        Page<Product> products = productRepository.findByKeyword(null, keyword, country, true, pageable);


        return products.map(p -> {
                    ProductDropDTO productDropDTO = productMapper.toDropDTO(p);
                    productDropDTO.setStock(p.getQuantity());
                    return productDropDTO;
                });
    }

    public void switchAvailableStock(Long id, ProductSwitchDropCommand productSwitchDropCommand, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product Not Found"));

        if (product.getUser().getId().equals(user.getId())) {
            if (product.getIsAvailableStock()) {
                product.setPrice(BigDecimal.ZERO);
                product.setQuantity(0);
            } else {
                AtomicInteger totalSourced = new AtomicInteger();
                AtomicInteger totalDelivered = new AtomicInteger();

                product.getSourcingProducts().forEach(sp -> {
                    totalSourced.addAndGet(sp.getQuantity());
                });
                product.getOrders().forEach(po -> {
                    if (po.getShippingStatus().equals(OrderShippinStatusEnum.DELIVERED)) {
                        totalDelivered.addAndGet(po.getQuantity());
                    }
                });

                if (totalSourced.get() - totalDelivered.get() - product.getQuantity() < productSwitchDropCommand.getQuantity()) {
                   throw new RuntimeException("Not enough products to switch");
                }

                product.setQuantity(productSwitchDropCommand.getQuantity());
                product.setPrice(productSwitchDropCommand.getPrice());
            }
            product.setIsAvailableStock(!product.getIsAvailableStock());
        }

    }
}
