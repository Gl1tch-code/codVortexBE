package com.codvortex.dto;

import com.codvortex.utils.OrderStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProductDropDTO {
    private Long id;
    private String name;
    private String img;
    private Integer stock;
    private BigDecimal price;
}
