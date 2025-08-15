package com.codvortex.dto;

import com.codvortex.utils.OrderStatusEnum;
import com.codvortex.utils.SourcingStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductPageDTO {
    private Long id;
    private String name;
    private String img;
    private Boolean isAvailableStock;
    private SourcingStatusEnum status;
    private Integer stock;
}
