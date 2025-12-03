package com.blibli.training.product.model.web;

import com.blibli.training.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedProductResponse {
    private List<Product> products;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}

