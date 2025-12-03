package com.blibli.training.cart.controller;

import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.service.CartService;
import com.blibli.training.framework.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public BaseResponse<Cart> getCart(@RequestHeader("X-User-Id") Long userId) {
        return BaseResponse.success(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    public BaseResponse<Cart> addItem(@RequestHeader("X-User-Id") Long userId, @RequestBody CartItem item) {
        return BaseResponse.success(cartService.addItemToCart(userId, item));
    }

    @DeleteMapping("/items/{itemId}")
    public BaseResponse<Cart> removeItem(@RequestHeader("X-User-Id") Long userId, @PathVariable Long itemId) {
        return BaseResponse.success(cartService.removeItemFromCart(userId, itemId));
    }

    @DeleteMapping("/clear")
    public BaseResponse<Cart> clearCart(@RequestHeader("X-User-Id") Long userId) {
        return BaseResponse.success(cartService.clearCart(userId));
    }

    @DeleteMapping
    public BaseResponse<Void> deleteCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.deleteCart(userId);
        return BaseResponse.success(null);
    }
}
