package com.blibli.training.cart.controller;

import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private Cart testCart;
    private CartItem testItem;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        
        testCart = Cart.builder()
                .id(1L)
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        testItem = CartItem.builder()
                .id(1L)
                .productCode("PROD-001")
                .productName("Test Product")
                .price(new BigDecimal("100.00"))
                .quantity(2)
                .build();
    }

    @Test
    void getCart_ShouldReturnCart() throws Exception {
        // Given
        testCart.getItems().add(testItem);
        when(cartService.getCartByUserId(userId)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(get("/cart")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productCode").value("PROD-001"));

        verify(cartService, times(1)).getCartByUserId(userId);
    }

    @Test
    void getCart_WithEmptyCart_ShouldReturnEmptyItems() throws Exception {
        // Given
        when(cartService.getCartByUserId(userId)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(get("/cart")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        verify(cartService, times(1)).getCartByUserId(userId);
    }

    @Test
    void addItem_ShouldAddItemToCart() throws Exception {
        // Given
        testCart.getItems().add(testItem);
        when(cartService.addItemToCart(eq(userId), any(CartItem.class))).thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/cart/items")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productCode").value("PROD-001"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));

        verify(cartService, times(1)).addItemToCart(eq(userId), any(CartItem.class));
    }

    @Test
    void removeItem_ShouldRemoveItemFromCart() throws Exception {
        // Given
        Long itemId = 1L;
        when(cartService.removeItemFromCart(userId, itemId)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(delete("/cart/items/{itemId}", itemId)
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(cartService, times(1)).removeItemFromCart(userId, itemId);
    }

    @Test
    void clearCart_ShouldClearAllItems() throws Exception {
        // Given
        when(cartService.clearCart(userId)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(delete("/cart/clear")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        verify(cartService, times(1)).clearCart(userId);
    }

    @Test
    void deleteCart_ShouldDeleteCart() throws Exception {
        // Given
        doNothing().when(cartService).deleteCart(userId);

        // When & Then
        mockMvc.perform(delete("/cart")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService, times(1)).deleteCart(userId);
    }

    @Test
    void getCart_WithoutUserIdHeader_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is5xxServerError());

        verify(cartService, never()).getCartByUserId(any());
    }

    @Test
    void addItem_WithMultipleItems_ShouldReturnAllItems() throws Exception {
        // Given
        CartItem item2 = CartItem.builder()
                .id(2L)
                .productCode("PROD-002")
                .productName("Another Product")
                .price(new BigDecimal("200.00"))
                .quantity(1)
                .build();
        
        testCart.getItems().addAll(Arrays.asList(testItem, item2));
        when(cartService.addItemToCart(eq(userId), any(CartItem.class))).thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/cart/items")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(2)));

        verify(cartService, times(1)).addItemToCart(eq(userId), any(CartItem.class));
    }
}

