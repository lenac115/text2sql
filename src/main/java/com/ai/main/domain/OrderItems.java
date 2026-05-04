package com.ai.main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_order_items_orders_id", columnList = "orders_id"),
        @Index(name = "idx_order_items_product_id", columnList = "product_id")
})
public class OrderItems {

    @Id @Column(name = "orderItems_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ColumnDefault("0")
    private int quantity;

    @NotNull
    @ColumnDefault("0")
    private int unitPrice;

    @NotNull
    @ColumnDefault("0")
    private int subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id")
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    public void updateItemsForOrderCreated(OrderItems orderItems) {
        this.quantity += orderItems.getQuantity();
        this.subtotal += orderItems.getSubtotal();
    }
}
