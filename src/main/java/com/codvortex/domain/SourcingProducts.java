package com.codvortex.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sourcing_products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourcingProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String link;
    private Integer quantity;
    private BigDecimal unitePrice;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "sourcing_id", nullable = false)
    private Sourcing sourcing;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}
