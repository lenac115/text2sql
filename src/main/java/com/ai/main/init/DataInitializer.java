package com.ai.main.init;

import com.ai.main.domain.*;
import com.ai.main.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        long categoryCount = categoryRepository.count();
        log.info("[DataInitializer] run() 진입. category count = {}", categoryCount);
        if (categoryCount > 0) {
            log.info("[DataInitializer] 기존 데이터 존재 → 초기화 skip");
            return;
        }

        long t0 = System.currentTimeMillis();
        try {
            initUsers();
            initProducts();
            initOrders();
            log.info("[DataInitializer] 전체 초기화 완료. {}ms", System.currentTimeMillis() - t0);
        } catch (RuntimeException e) {
            log.error("[DataInitializer] 초기화 실패. {}ms 경과 후 예외 — 트랜잭션 롤백됨",
                    System.currentTimeMillis() - t0, e);
            throw e;
        }
    }

    private void initProducts() {
        long t0 = System.currentTimeMillis();
        log.info("[DataInitializer] initProducts() 시작");
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
        log.info("[DataInitializer] category {} 건 저장", categoryBatch.size());

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
        log.info("[DataInitializer] product {} 건 저장 완료 ({}ms)",
                productsBatch.size(), System.currentTimeMillis() - t0);
    }

    private void initUsers() {
        long t0 = System.currentTimeMillis();
        log.info("[DataInitializer] initUsers() 시작");
        Random random = new Random(42);

        // BCrypt는 해시당 100~300ms. 50K번 돌리면 수 시간 걸리므로 시드용 공용 해시 1회만 계산.
        // 일반 유저 로그인 비밀번호는 모두 "password".
        String sharedHash = passwordEncoder.encode("password");
        log.info("[DataInitializer] 공용 비밀번호 해시 1회 계산 완료 ({}ms)", System.currentTimeMillis() - t0);

        List<Users> usersBatch = new ArrayList<>();

        for (int i = 0; i < 50000; i++) {
            String name = "name" + i;
            Users user = Users.builder()
                    .createdAt(randomDateInPastYear(random))
                    .email("email" + i + "@gmail.com")
                    .password(sharedHash)
                    .name(name)
                    .role(Users.Role.USER)
                    .defaultAddress(dummyAddress(name))
                    .build();

            usersBatch.add(user);
        }
        Users admin = Users.builder()
                .createdAt(randomDateInPastYear(random))
                .email("lenac115@naver.com")
                .password(passwordEncoder.encode("test1234"))
                .name("admin")
                .role(Users.Role.ADMIN)
                .defaultAddress(dummyAddress("admin"))
                .build();
        usersBatch.add(admin);
        usersRepository.saveAll(usersBatch);
        log.info("[DataInitializer] users {} 건 저장 완료 ({}ms)",
                usersBatch.size(), System.currentTimeMillis() - t0);
    }

    private Address dummyAddress(String recipient) {
        return Address.builder()
                .recipient(recipient)
                .phone("010-0000-0000")
                .zipCode("00000")
                .addressLine1("서울시 더미구 더미동 1-1")
                .addressLine2("101호")
                .build();
    }


    private void initOrders() {
        long t0 = System.currentTimeMillis();
        log.info("[DataInitializer] initOrders() 시작");
        Random random = new Random(42);

        List<Users> allUsers = usersRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        log.info("[DataInitializer] orders 생성용 — users={} products={} 로드", allUsers.size(), allProducts.size());

        List<Orders> orderBatch = new ArrayList<>();
        List<OrderItems> itemBatch = new ArrayList<>();
        int savedOrders = 0;
        int savedItems = 0;

        for (int i = 0; i < 50000; i++) {
            Users randomUser = allUsers.get(random.nextInt(allUsers.size()));

            Orders order = Orders.builder()
                    .users(randomUser)
                    .orderStatus(randomStatus(random))
                    .orderedAt(randomDateInPastYear(random))
                    .totalAmount(0)
                    .shippingAddress(dummyAddress(randomUser.getName()))
                    .build();

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

            orderBatch.add(order);

            if (orderBatch.size() == 1000) {
                ordersRepository.saveAll(orderBatch);
                orderItemsRepository.saveAll(itemBatch);
                savedOrders += orderBatch.size();
                savedItems += itemBatch.size();
                orderBatch.clear();
                itemBatch.clear();
                log.info("[DataInitializer] orders 진행 — 누적 orders={} items={} ({}ms)",
                        savedOrders, savedItems, System.currentTimeMillis() - t0);
            }
        }

        if (!orderBatch.isEmpty()) {
            ordersRepository.saveAll(orderBatch);
            orderItemsRepository.saveAll(itemBatch);
            savedOrders += orderBatch.size();
            savedItems += itemBatch.size();
        }
        log.info("[DataInitializer] orders {} 건 / order_items {} 건 저장 완료 ({}ms)",
                savedOrders, savedItems, System.currentTimeMillis() - t0);
    }

    private Orders.@NotNull OrderStatus randomStatus(Random random) {
        int r = random.nextInt(100);
        if (r < 40) return Orders.OrderStatus.DELIVERED;
        if (r < 65) return Orders.OrderStatus.PAID;
        if (r < 80) return Orders.OrderStatus.SHIPPING;
        if (r < 90) return Orders.OrderStatus.PAYMENT_PENDING;
        if (r < 96) return Orders.OrderStatus.CANCELLED;
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
