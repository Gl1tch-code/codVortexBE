package com.codvortex.domain;

import com.codvortex.utils.SourcingPaymentStatusEnum;
import com.codvortex.utils.SourcingStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "sourcings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sourcing extends BaseEntity{
    private String title;
    private String message;
    private String invoice;
    private String paymentTransferInvoice;
    private BigDecimal totalPrice;
    private BigDecimal shippingFees;
    private Boolean isShippingFeesPayed;

    @Enumerated(value = EnumType.STRING)
    private SourcingStatusEnum status;

    @Enumerated(value = EnumType.STRING)
    private SourcingPaymentStatusEnum paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id")
    private Country country;

    @OneToMany(mappedBy = "sourcing",cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SourcingProducts> sourcingProducts;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

}
