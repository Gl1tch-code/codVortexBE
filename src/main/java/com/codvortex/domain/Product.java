package com.codvortex.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseEntity {
    private String name;
    private String img;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isAvailableStock;
    private BigDecimal price;
    private Integer quantity;

    @OneToMany(mappedBy = "product")
    private List<Order> orders;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id")
    private Country country;

    @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SourcingProducts> sourcingProducts;

}
