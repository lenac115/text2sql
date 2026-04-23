package com.ai.main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_orders_ordered_at", columnList = "ordered_at"),
        @Index(name = "idx_orders_status_ordered_at", columnList = "order_status, ordered_at"),
        @Index(name = "idx_orders_user_id", columnList = "users_id")
})
public class Orders {

    @Id @Column(name = "orders_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private LocalDateTime orderedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;

    @OneToMany(mappedBy = "orders", fetch = FetchType.LAZY)
    private List<OrderItems> orderItemsList = new ArrayList<>();

    public enum OrderStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        REFUNDED
    }

    public void addOrderItem(OrderItems item) {
        if(this.orderItemsList == null) {
            this.orderItemsList = new ArrayList<>();
        }
        this.orderItemsList.add(item);
        this.totalAmount += item.getSubtotal();
    }
}
