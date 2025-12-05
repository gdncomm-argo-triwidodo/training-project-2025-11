package com.blibli.training.cart.service;

import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
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
    void getCartByUserId_WhenCartExists_ShouldReturnExistingCart() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // When
        Cart result = cartService.getCartByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testCart.getId(), result.getId());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCartByUserId_WhenCartDoesNotExist_ShouldCreateNewCart() {
        // Given
        Cart newCart = Cart.builder().userId(userId).build();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        // When
        Cart result = cartService.getCartByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ShouldAddItemAndSaveCart() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.addItemToCart(userId, testItem);

        // Then
        assertNotNull(result);
        assertTrue(testCart.getItems().contains(testItem));
        assertEquals(testCart, testItem.getCart());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void addItemToCart_WhenCartDoesNotExist_ShouldCreateCartAndAddItem() {
        // Given
        Cart newCart = Cart.builder().userId(userId).items(new ArrayList<>()).build();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        // When
        Cart result = cartService.addItemToCart(userId, testItem);

        // Then
        assertNotNull(result);
        assertEquals(newCart, testItem.getCart());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(2)).save(any(Cart.class)); // Once for creating cart, once for adding item
    }

    @Test
    void removeItemFromCart_ShouldRemoveItemAndSaveCart() {
        // Given
        testCart.getItems().add(testItem);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.removeItemFromCart(userId, testItem.getId());

        // Then
        assertNotNull(result);
        assertFalse(testCart.getItems().contains(testItem));
        assertEquals(0, testCart.getItems().size());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void removeItemFromCart_WhenItemDoesNotExist_ShouldNotRemoveAnything() {
        // Given
        CartItem anotherItem = CartItem.builder()
                .id(2L)
                .productCode("PROD-002")
                .build();
        testCart.getItems().add(anotherItem);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.removeItemFromCart(userId, 999L);

        // Then
        assertNotNull(result);
        assertEquals(1, testCart.getItems().size());
        assertTrue(testCart.getItems().contains(anotherItem));
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void clearCart_ShouldRemoveAllItemsAndSaveCart() {
        // Given
        testCart.getItems().add(testItem);
        testCart.getItems().add(CartItem.builder().id(2L).productCode("PROD-002").build());
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.clearCart(userId);

        // Then
        assertNotNull(result);
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void clearCart_WhenCartIsEmpty_ShouldStillSaveCart() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.clearCart(userId);

        // Then
        assertNotNull(result);
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void deleteCart_WhenCartExists_ShouldDeleteCart() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        doNothing().when(cartRepository).delete(testCart);

        // When
        cartService.deleteCart(userId);

        // Then
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).delete(testCart);
    }

    @Test
    void deleteCart_WhenCartDoesNotExist_ShouldNotThrowException() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertDoesNotThrow(() -> cartService.deleteCart(userId));
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).delete(any(Cart.class));
    }
}

