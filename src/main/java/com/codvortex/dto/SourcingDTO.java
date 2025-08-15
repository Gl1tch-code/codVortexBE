package com.codvortex.dto;

import com.codvortex.utils.SourcingPaymentStatusEnum;
import com.codvortex.utils.SourcingStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SourcingDTO {
    private Long id;
    private String title;
    private String invoice;
    private BigDecimal totalPrice;
    private Integer totalProductsQuantity;
    private Integer productsCount;

    private SourcingStatusEnum status;
    private String paymentTransferInvoice;
    private SourcingPaymentStatusEnum paymentStatus;
}
