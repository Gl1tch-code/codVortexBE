package com.codvortex.service.AdminServices.product;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.Product;
import com.codvortex.domain.SourcingProducts;
import com.codvortex.repository.CountryRepository;
import com.codvortex.repository.ProductRepository;
import com.codvortex.repository.SourcingProductsRepository;
import com.codvortex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SourcingProductsRepository sourcingProductsRepository;
    private final CountryRepository countryRepository;

    public Product getProduct(Long id, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void updateName(Long id, String value, String token) {
        Product product = getProduct(id, token);
        product.setName(value);
        productRepository.save(product);
    }

    public void updatePrice(Long id, String value, String token) {
        Product product = getProduct(id, token);
        product.setPrice(BigDecimal.valueOf(Double.parseDouble(value)));
        productRepository.save(product);
    }

    public void updateQuantity(Long id, String value, String token) {
        Product product = getProduct(id, token);
        product.setQuantity(Integer.parseInt(value));
        productRepository.save(product);
    }

    public void updateIsAvailableStock(Long id, String value, String token) {
        Product product = getProduct(id, token);
        product.setIsAvailableStock(Boolean.valueOf(value));
        productRepository.save(product);
    }

    public void updateCountry(Long id, String value, String token) {
        Product product = getProduct(id, token);
        product.setCountry(countryRepository.findById(Long.parseLong(value)).orElseThrow(
                () -> new RuntimeException("Country not found")
        ));
        productRepository.save(product);
    }

    public void updateSourcingQuantity(Long id, Long sourcingId, String value, String token) {
        userRepository.findByUsernameAndAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        SourcingProducts sourcingProduct = sourcingProductsRepository.findBySourcingIdAndProductId(sourcingId, id)
                .orElseThrow(() -> new RuntimeException("Sourcing product not found"));
        sourcingProduct.setQuantity(Integer.parseInt(value));
        sourcingProductsRepository.save(sourcingProduct);
    }

}
