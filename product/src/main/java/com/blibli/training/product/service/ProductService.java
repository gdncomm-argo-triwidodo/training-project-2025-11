package com.blibli.training.product.service;

import com.blibli.training.product.entity.Product;
import com.blibli.training.product.model.web.PagedProductResponse;
import com.blibli.training.product.model.web.SearchRequest;
import com.blibli.training.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final IdGeneratorService idGeneratorService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
        // Generate custom ID if not provided
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(idGeneratorService.generateProductId());
        }
        return productRepository.save(product);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public PagedProductResponse searchProducts(SearchRequest searchRequest) {
        List<Product> products = productRepository.searchProducts(searchRequest);
        long totalElements = productRepository.countProducts(searchRequest);
        int totalPages = (int) Math.ceil((double) totalElements / searchRequest.getSize());

        return PagedProductResponse.builder()
                .products(products)
                .currentPage(searchRequest.getPage())
                .pageSize(searchRequest.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}

