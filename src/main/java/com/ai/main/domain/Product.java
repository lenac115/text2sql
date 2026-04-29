package com.ai.main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_product_category_id", columnList = "category_id"),
        @Index(name = "idx_product_name", columnList = "name")
})
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    @ColumnDefault("0")
    private int price;

    @ColumnDefault("0")
    private int stock;

    @NotNull
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderItems> orderItemsList = new ArrayList<>();

    public void addOrderItems(OrderItems orderItems) {
        if (this.orderItemsList == null) {
            this.orderItemsList = new ArrayList<>();
        }
        this.orderItemsList.add(orderItems);
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다. (현재: " + this.stock + ", 요청: " + quantity + ")");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public void update(String name, String description, String imageUrl,
                       Integer price, Integer stock, Category category) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (price != null) this.price = price;
        if (stock != null) this.stock = stock;
        if (category != null) this.category = category;
    }
}