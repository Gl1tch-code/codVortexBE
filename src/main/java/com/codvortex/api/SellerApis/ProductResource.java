package com.codvortex.api.SellerApis;

import com.codvortex.commands.ProductSwitchDropCommand;
import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.User;
import com.codvortex.dto.ProductDTO;
import com.codvortex.dto.ProductDropDTO;
import com.codvortex.dto.ProductPageDTO;
import com.codvortex.repository.CountryRepository;
import com.codvortex.repository.ProductRepository;
import com.codvortex.repository.UserRepository;
import com.codvortex.service.SellerServices.product.ProductService;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.SourcingStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/products")
public class ProductResource {
    private final ProductRepository productRepository;
    private final JwtTokenService jwtTokenService;
    private final CountryRepository countryRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductResource(ProductRepository productRepository, JwtTokenService jwtTokenService, CountryRepository countryRepository, ProductService productService, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.jwtTokenService = jwtTokenService;
        this.countryRepository = countryRepository;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<ProductPageDTO>> getPageable(@RequestParam String q, Pageable pageable, String country, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(productService.getAll(q, pageable, country, authHeader));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getProduct(@RequestParam("country") String country,
                                                       @RequestHeader("Authorization") String authHeader) {
        Long countryId = countryRepository.findByKeyIgnoreCase(country.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + country)).getId();

        return ResponseEntity.ok(
                productRepository.findByUserEmail(jwtTokenService.extractEmail(authHeader), countryId)
                        .stream()
                        .filter(p -> {
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

                            return ((totalSourced.get() - totalDelivered.get() - p.getQuantity()) > 0) && p.getSourcingProducts().get(0).getSourcing().getStatus() == SourcingStatusEnum.SOURCING_COMPLETE;
                        })
                        .map(p -> {
                            ProductDTO productDTO = new ProductDTO();
                            productDTO.setId(p.getId());
                            productDTO.setName(p.getName());
                            productDTO.setImg(p.getImg());
                            return productDTO;
                        }).toList()
        );
    }

    @GetMapping("/availableStock")
    public ResponseEntity<Page<ProductDropDTO>> getAvailableStockProducts(@RequestParam String q, Pageable pageable, String country) {
        return ResponseEntity.ok(productService.getAllAvailableStock(q, pageable, country));
    }

    @GetMapping("/availableStock/all")
    public ResponseEntity<List<ProductDTO>> getAllAvailableStockProducts(@RequestParam("country") String country, @RequestHeader("Authorization") String authHeader) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(authHeader)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Long countryId = countryRepository.findByKeyIgnoreCase(country.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + country)).getId();

        return ResponseEntity.ok(
                productRepository.findByCountryAndIsAvailableStock(countryId, user.getId())
                        .stream()
                        .filter(p -> p.getQuantity() > 0)
                        .map(p -> {
                            ProductDTO productDTO = new ProductDTO();
                            productDTO.setId(p.getId());
                            productDTO.setName(p.getName());
                            productDTO.setImg(p.getImg());
                            return productDTO;
                        }).toList()
        );
    }

    @PutMapping("/switchAvailableStock/{id}")
    public ResponseEntity<Void> switchAvailableStock(@PathVariable Long id, @RequestBody ProductSwitchDropCommand productSwitchDropCommand, @RequestHeader("Authorization") String authHeader) {
        productService.switchAvailableStock(id, productSwitchDropCommand, authHeader);
        return ResponseEntity.ok().build();
    }

}
