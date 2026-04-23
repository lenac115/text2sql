package com.ai.main.init;

import com.ai.main.domain.*;
import com.ai.main.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) return; // 이미 데이터 있으면 스킵

        initUsers();
        initProducts();
        initOrders();
    }

    private void initProducts() {
        Random random = new Random(42);

        List<Category> categoryBatch = new ArrayList<>();
        List<Product> productsBatch = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Category category = Category.builder()
                    .name("Category" + i)
                    .build();
            categoryBatch.add(category);
        }

        categoryRepository.saveAll(categoryBatch);

        List<Category> categories = categoryRepository.findAll();

        for (int i = 0; i < 50000; i++) {

            Category category = categories.get(random.nextInt(categories.size()));
            Product product = Product.builder()
                    .price(random.nextInt(random.nextInt(99000) + 1000))
                    .stock(random.nextInt(100000))
                    .name("name" + i)
                    .category(category)
                    .createdAt(randomDateInPastYear(random))
                    .build();
            category.addProduct(product);

            productsBatch.add(product);
        }

        productRepository.saveAll(productsBatch);
    }

    private void initUsers() {
        Random random = new Random(42);

        List<Users> usersBatch = new ArrayList<>();

        for (int i = 0; i < 50000; i++) {

            Users user = Users.builder()
                    .createdAt(randomDateInPastYear(random))
                    .email("email" + i + "@gmail.com")
                    .password("password" + i)
                    .name("name" + i)
                    .build();

            usersBatch.add(user);
        }
        usersRepository.saveAll(usersBatch);
    }


    private void initOrders() {
        Random random = new Random(42);

        List<Users> allUsers = usersRepository.findAll();
        List<Product> allProducts = productRepository.findAll();

        List<Orders> orderBatch = new ArrayList<>();
        List<OrderItems> itemBatch = new ArrayList<>();

        for (int i = 0; i < 50000; i++) {
            Users randomUser = allUsers.get(random.nextInt(allUsers.size()));

            // 1. Orders 먼저 생성 (totalAmount는 0으로 시작)
            Orders order = Orders.builder()
                    .users(randomUser)
                    .orderStatus(randomStatus(random))
                    .orderedAt(randomDateInPastYear(random))
                    .totalAmount(0)
                    .build();

            // 2. OrderItems 생성하면서 subtotal 합산
            int itemCount = random.nextInt(5) + 1;
            int totalAmount = 0;

            for (int j = 0; j < itemCount; j++) {
                Product product = allProducts.get(random.nextInt(allProducts.size()));
                int quantity = random.nextInt(3) + 1;
                int unitPrice = product.getPrice();
                int subtotal = unitPrice * quantity;

                OrderItems item = OrderItems.builder()
                        .orders(order)
                        .product(product)
                        .quantity(quantity)
                        .unitPrice(unitPrice)
                        .subtotal(subtotal)
                        .build();

                order.addOrderItem(item);
                itemBatch.add(item);
            }

            // 3. totalAmount 재계산 (도메인 메서드 사용)
            // → 근데 여기서 문제: orderItemsList가 비어있으니 calculateTotalAmount가 0으로 나옴

            orderBatch.add(order);

            if (orderBatch.size() == 1000) {
                ordersRepository.saveAll(orderBatch);
                orderItemsRepository.saveAll(itemBatch);
                orderBatch.clear();
                itemBatch.clear();
            }
        }

        if (!orderBatch.isEmpty()) {
            ordersRepository.saveAll(orderBatch);
            orderItemsRepository.saveAll(itemBatch);
        }
    }

    private Orders.@NotNull OrderStatus randomStatus(Random random) {
        int r = random.nextInt(100);
        if (r < 90) return Orders.OrderStatus.COMPLETED;
        if (r < 95) return Orders.OrderStatus.CANCELLED;
        if (r < 98) return Orders.OrderStatus.PENDING;
        return Orders.OrderStatus.REFUNDED;
    }

    private LocalDateTime randomDateInPastYear(Random random) {
        long minDay = LocalDate.now().minusYears(1).toEpochDay();
        long maxDay = LocalDate.now().toEpochDay();
        long randomDay = minDay + random.nextInt((int)(maxDay - minDay));
        return LocalDate.ofEpochDay(randomDay).atStartOfDay()
                .plusHours(random.nextInt(24))
                .plusMinutes(random.nextInt(60));
    }
}
