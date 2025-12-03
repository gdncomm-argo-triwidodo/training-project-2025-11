package com.blibli.training.product.controller;

import com.blibli.training.framework.dto.BaseResponse;
import com.blibli.training.product.entity.Product;
import com.blibli.training.product.model.web.PagedProductResponse;
import com.blibli.training.product.model.web.SearchRequest;
import com.blibli.training.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public BaseResponse<List<Product>> getProducts() {
        return BaseResponse.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public BaseResponse<Product> getProductById(@PathVariable String id) {
        return BaseResponse.success(productService.getProductById(id));
    }
    
    @PostMapping
    public BaseResponse<Product> createProduct(@RequestBody Product product) {
        return BaseResponse.success(productService.createProduct(product));
    }

    @PostMapping("/search")
    public BaseResponse<PagedProductResponse> searchProducts(@RequestBody SearchRequest searchRequest) {
        return BaseResponse.success(productService.searchProducts(searchRequest));
    }
    
}
