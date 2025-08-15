package com.codvortex.dto;

import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OrderDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private String address;
    private BigDecimal price;
    private LocalDateTime date;
    private OrderStatusEnum status;
    private LocalDateTime updatedAt;
    private OrderShippinStatusEnum shippingStatus;
    private Boolean isSellerPayed;
    private String message;
}
