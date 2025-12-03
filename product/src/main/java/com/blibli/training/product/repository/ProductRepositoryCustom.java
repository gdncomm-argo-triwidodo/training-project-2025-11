package com.blibli.training.product.repository;

import com.blibli.training.product.entity.Product;
import com.blibli.training.product.model.web.SearchRequest;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> searchProducts(SearchRequest searchRequest);
    long countProducts(SearchRequest searchRequest);
}

