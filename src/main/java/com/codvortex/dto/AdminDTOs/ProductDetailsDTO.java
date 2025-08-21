package com.codvortex.dto.AdminDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProductDetailsDTO {
    private Long id;
    private String name;
    private String img;
    private Boolean isAvailableStock;
    private BigDecimal price;
    private Integer quantity;
    private String country;
    private Integer sourcingProductQuantity;
    private Integer stock;
}
