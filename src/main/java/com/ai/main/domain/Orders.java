package com.ai.main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

    @Id
    @Column(name = "orders_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Builder.Default
    private int totalAmount = 0;

    @Builder.Default
    private int discountAmount = 0;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private LocalDateTime orderedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_deal_id")
    private TimeDeal timeDeal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipient",    column = @Column(name = "ship_recipient")),
            @AttributeOverride(name = "phone",        column = @Column(name = "ship_phone")),
            @AttributeOverride(name = "zipCode",      column = @Column(name = "ship_zip_code")),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "ship_address_line1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "ship_address_line2"))
    })
    private Address shippingAddress;

    @OneToMany(mappedBy = "orders", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItems> orderItemsList = new ArrayList<>();

    public enum OrderStatus {
        PAYMENT_PENDING, PAID, SHIPPING, DELIVERED, CANCELLED, REFUNDED
    }

    public void changeStatus(OrderStatus newStatus) {
        boolean valid = switch (this.orderStatus) {
            case PAYMENT_PENDING -> newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PAID            -> newStatus == OrderStatus.SHIPPING || newStatus == OrderStatus.CANCELLED;
            case SHIPPING        -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED       -> newStatus == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "주문 상태를 " + this.orderStatus + " → " + newStatus + "로 변경할 수 없습니다.");
        }
        this.orderStatus = newStatus;
    }

    public void addOrderItem(OrderItems item) {
        if (this.orderItemsList == null) {
            this.orderItemsList = new ArrayList<>();
        }
        for (OrderItems orderItem : this.orderItemsList) {
            if (orderItem.getProduct().getId().equals(item.getProduct().getId())) {
                orderItem.updateItemsForOrderCreated(item);
                this.totalAmount += item.getSubtotal();
                return;
            }
        }
        this.orderItemsList.add(item);
        this.totalAmount += item.getSubtotal();
    }

    public void applyDiscount(int discountAmount, UserCoupon userCoupon) {
        this.discountAmount = discountAmount;
        this.userCoupon = userCoupon;
    }

    public int getFinalAmount() {
        return Math.max(0, totalAmount - discountAmount);
    }
}