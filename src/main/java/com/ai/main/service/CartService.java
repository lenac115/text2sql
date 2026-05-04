package com.ai.main.service;

import com.ai.main.domain.CartItem;
import com.ai.main.domain.Product;
import com.ai.main.domain.Users;
import com.ai.main.dto.cart.*;
import com.ai.main.repository.CartItemRepository;
import com.ai.main.repository.ProductRepository;
import com.ai.main.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        List<CartItemResponse> items = cartItemRepository.findByUserEmail(email).stream()
                .map(CartItemResponse::from)
                .toList();
        int totalAmount = items.stream().mapToInt(CartItemResponse::subtotal).sum();
        return new CartResponse(items, totalAmount);
    }

    @Transactional
    public CartItemResponse addItem(String email, CartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        Optional<CartItem> existing = cartItemRepository.findByUser_EmailAndProduct_Id(email, request.productId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.updateQuantity(item.getQuantity() + request.quantity());
            return CartItemResponse.from(item);
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        CartItem item = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(request.quantity())
                .addedAt(LocalDateTime.now())
                .build();

        cartItemRepository.save(item);
        return CartItemResponse.from(item);
    }

    @Transactional
    public CartItemResponse updateQuantity(String email, Long cartItemId, CartItemUpdateRequest request) {
        CartItem item = cartItemRepository.findByIdAndUser_Email(cartItemId, email)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목을 찾을 수 없습니다."));
        item.updateQuantity(request.quantity());
        return CartItemResponse.from(item);
    }

    @Transactional
    public void removeItem(String email, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUser_Email(cartItemId, email)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목을 찾을 수 없습니다."));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(String email) {
        cartItemRepository.deleteAllByUserEmail(email);
    }
}