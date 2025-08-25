package com.codvortex.domain;

import com.codvortex.utils.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String transferImg;
    private String fileLink;
    private BigDecimal totalPrice;

    @Enumerated(value = EnumType.STRING)
    private InvoiceStatus status;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sellerInvoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> sellerOrders;

    @OneToMany(mappedBy = "productOwnerInvoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> productOwnerOrders;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean deleted = false;

    public void delete() {
        this.deleted = true;
    }
}
