package com.codvortex.dto;

import com.codvortex.utils.InvoiceStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceDTO {
    private String id;
    private String transferImg;
    private BigDecimal totalPrice;
    private Integer ordersCount;
    private LocalDateTime updatedAt;
    private InvoiceStatus status;
    private String fileLink;

}
