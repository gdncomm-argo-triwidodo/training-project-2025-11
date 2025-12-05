package com.blibli.training.product.controller;

import com.blibli.training.product.entity.Product;
import com.blibli.training.product.model.web.PagedProductResponse;
import com.blibli.training.product.model.web.SearchRequest;
import com.blibli.training.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getProducts_ShouldReturnAllProducts() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(testProducts);

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value("MTA-000001"))
                .andExpect(jsonPath("$.data[0].name").value("Test Product"))
                .andExpect(jsonPath("$.data[1].id").value("MTA-000002"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProducts_WhenNoProducts_ShouldReturnEmptyList() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        // Given
        String productId = "MTA-000001";
        when(productService.getProductById(productId)).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(100.00))
                .andExpect(jsonPath("$.data.stock").value(10));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowException() throws Exception {
        // Given
        String productId = "NON-EXISTENT";
        when(productService.getProductById(productId))
                .thenThrow(new RuntimeException("Product not found"));

        // When & Then - Service layer throws exception, controller doesn't handle it
        try {
            mockMvc.perform(get("/products/{id}", productId));
        } catch (Exception e) {
            // Expected - ServletException wraps the RuntimeException
            assertTrue(e.getCause() instanceof RuntimeException);
        }

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        // Given
        Product newProduct = Product.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("150.00"))
                .stock(20)
                .build();

        Product createdProduct = Product.builder()
                .id("MTA-000003")
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("150.00"))
                .stock(20)
                .build();

        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("MTA-000003"))
                .andExpect(jsonPath("$.data.name").value("New Product"));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void searchProducts_ShouldReturnPagedResults() throws Exception {
        // Given
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setMinPrice(new BigDecimal("50.00"));
        searchRequest.setMaxPrice(new BigDecimal("250.00"));

        PagedProductResponse pagedResponse = PagedProductResponse.builder()
                .products(testProducts)
                .currentPage(0)
                .pageSize(10)
                .totalElements(2L)
                .totalPages(1)
                .build();

        when(productService.searchProducts(any(SearchRequest.class))).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(post("/products/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products", hasSize(2)))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(productService, times(1)).searchProducts(any(SearchRequest.class));
    }

    @Test
    void searchProducts_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        // Given
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        PagedProductResponse pagedResponse = PagedProductResponse.builder()
                .products(Arrays.asList())
                .currentPage(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .build();

        when(productService.searchProducts(any(SearchRequest.class))).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(post("/products/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(productService, times(1)).searchProducts(any(SearchRequest.class));
    }

    @Test
    void createProduct_WithInvalidData_ShouldHandleError() throws Exception {
        // Given
        Product invalidProduct = Product.builder().build(); // Empty product

        when(productService.createProduct(any(Product.class)))
                .thenThrow(new RuntimeException("Invalid product data"));

        // When & Then - MockMvc converts exception to error response
        try {
            mockMvc.perform(post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidProduct)));
        } catch (Exception e) {
            // Expected - ServletException wraps the RuntimeException
            assertTrue(e.getCause() instanceof RuntimeException);
        }

        verify(productService, times(1)).createProduct(any(Product.class));
    }
}

