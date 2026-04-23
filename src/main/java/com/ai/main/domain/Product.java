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
        @Index(name = "idx_product_category_id", columnList = "category_id")
})
public class Product {

    @Id @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

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
        if(this.orderItemsList == null) {
            this.orderItemsList = new ArrayList<>();
        }
        this.orderItemsList.add(orderItems);
    }
}
