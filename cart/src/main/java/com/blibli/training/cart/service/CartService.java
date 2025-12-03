package com.blibli.training.cart.service;

import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
    }

    public Cart addItemToCart(Long userId, CartItem item) {
        Cart cart = getCartByUserId(userId);
        
        item.setCart(cart);
        cart.getItems().add(item);

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(Long userId, Long itemId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return cartRepository.save(cart);
    }

    public Cart clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    public void deleteCart(Long userId) {
        cartRepository.findByUserId(userId)
                .ifPresent(cartRepository::delete);
    }
}

