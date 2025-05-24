package com.codvortex.domain;

import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order extends BaseEntity{
    private BigDecimal price;
    private LocalDateTime date = LocalDateTime.now();
    private String address;

    @Enumerated(value = EnumType.STRING)
    private OrderStatusEnum status;

    @Enumerated(value = EnumType.STRING)
    private OrderShippinStatusEnum shippingStatus;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

}
