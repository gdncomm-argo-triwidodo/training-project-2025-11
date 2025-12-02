package com.blibli.training.product.service;

import com.blibli.training.product.entity.Product;
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

    public List<Product> searchProducts(SearchRequest searchRequest) {
        return productRepository.searchProducts(searchRequest);
    }
}

