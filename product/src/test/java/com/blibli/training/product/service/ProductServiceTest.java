package com.blibli.training.product.service;

import com.blibli.training.product.entity.Product;
import com.blibli.training.product.model.web.PagedProductResponse;
import com.blibli.training.product.model.web.SearchRequest;
import com.blibli.training.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("MTA-000001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        Product product2 = Product.builder()
                .id("MTA-000002")
                .name("Another Product")
                .description("Another Description")
                .price(new BigDecimal("200.00"))
                .stock(5)
                .build();

        testProducts = Arrays.asList(testProduct, product2);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        when(productRepository.findAll()).thenReturn(testProducts);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testProducts, result);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void createProduct_WhenIdIsNull_ShouldGenerateIdAndSave() {
        // Given
        Product newProduct = Product.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("150.00"))
                .stock(20)
                .build();

        String generatedId = "MTA-000003";
        when(idGeneratorService.generateProductId()).thenReturn(generatedId);
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        // When
        Product result = productService.createProduct(newProduct);

        // Then
        assertNotNull(result);
        verify(idGeneratorService, times(1)).generateProductId();
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void createProduct_WhenIdIsEmpty_ShouldGenerateIdAndSave() {
        // Given
        Product newProduct = Product.builder()
                .id("")
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("150.00"))
                .stock(20)
                .build();

        String generatedId = "MTA-000003";
        when(idGeneratorService.generateProductId()).thenReturn(generatedId);
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        // When
        Product result = productService.createProduct(newProduct);

        // Then
        assertNotNull(result);
        verify(idGeneratorService, times(1)).generateProductId();
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void createProduct_WhenIdIsProvided_ShouldNotGenerateId() {
        // Given
        Product newProduct = Product.builder()
                .id("CUSTOM-001")
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("150.00"))
                .stock(20)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        // When
        Product result = productService.createProduct(newProduct);

        // Then
        assertNotNull(result);
        verify(idGeneratorService, never()).generateProductId();
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        String productId = "MTA-000001";
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        String productId = "NON-EXISTENT";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> productService.getProductById(productId));
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void searchProducts_ShouldReturnPagedResponse() {
        // Given
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setMinPrice(new BigDecimal("50.00"));
        searchRequest.setMaxPrice(new BigDecimal("250.00"));

        when(productRepository.searchProducts(searchRequest)).thenReturn(testProducts);
        when(productRepository.countProducts(searchRequest)).thenReturn(2L);

        // When
        PagedProductResponse result = productService.searchProducts(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getProducts().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(productRepository, times(1)).searchProducts(searchRequest);
        verify(productRepository, times(1)).countProducts(searchRequest);
    }

    @Test
    void searchProducts_WithMultiplePages_ShouldCalculateCorrectTotalPages() {
        // Given
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        when(productRepository.searchProducts(searchRequest)).thenReturn(testProducts);
        when(productRepository.countProducts(searchRequest)).thenReturn(25L);

        // When
        PagedProductResponse result = productService.searchProducts(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(25L, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 25 items / 10 per page = 3 pages
    }

    @Test
    void searchProducts_WhenNoResults_ShouldReturnEmptyPagedResponse() {
        // Given
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        when(productRepository.searchProducts(searchRequest)).thenReturn(Arrays.asList());
        when(productRepository.countProducts(searchRequest)).thenReturn(0L);

        // When
        PagedProductResponse result = productService.searchProducts(searchRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.getProducts().isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void searchProducts_WithExactMultipleOfPageSize_ShouldCalculateCorrectPages() {
        // Given
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        when(productRepository.searchProducts(searchRequest)).thenReturn(testProducts);
        when(productRepository.countProducts(searchRequest)).thenReturn(20L);

        // When
        PagedProductResponse result = productService.searchProducts(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(20L, result.getTotalElements());
        assertEquals(2, result.getTotalPages()); // 20 items / 10 per page = 2 pages
    }
}

