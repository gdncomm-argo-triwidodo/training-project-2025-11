package com.blibli.training.product.repository;

import com.blibli.training.framework.constant.SortDirection;
import com.blibli.training.product.entity.Product;
import com.blibli.training.product.model.web.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Product> searchProducts(SearchRequest searchRequest) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Text search on name and description
        if (searchRequest.getSearch() != null && !searchRequest.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(searchRequest.getSearch(), "i"),
                Criteria.where("description").regex(searchRequest.getSearch(), "i")
            );
            criteriaList.add(searchCriteria);
        }

        // Price range filter
        if (searchRequest.getMinPrice() != null) {
            criteriaList.add(Criteria.where("price").gte(searchRequest.getMinPrice()));
        }
        if (searchRequest.getMaxPrice() != null) {
            criteriaList.add(Criteria.where("price").lte(searchRequest.getMaxPrice()));
        }

        // Combine all criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Sorting
        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty()) {
            Sort.Direction direction = searchRequest.getSortDirection() == SortDirection.DESC 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            query.with(Sort.by(direction, searchRequest.getSortBy()));
        }

        // Pagination
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        query.with(pageable);

        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public long countProducts(SearchRequest searchRequest) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Text search on name and description
        if (searchRequest.getSearch() != null && !searchRequest.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("name").regex(searchRequest.getSearch(), "i"),
                Criteria.where("description").regex(searchRequest.getSearch(), "i")
            );
            criteriaList.add(searchCriteria);
        }

        // Price range filter
        if (searchRequest.getMinPrice() != null) {
            criteriaList.add(Criteria.where("price").gte(searchRequest.getMinPrice()));
        }
        if (searchRequest.getMaxPrice() != null) {
            criteriaList.add(Criteria.where("price").lte(searchRequest.getMaxPrice()));
        }

        // Combine all criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.count(query, Product.class);
    }
}

