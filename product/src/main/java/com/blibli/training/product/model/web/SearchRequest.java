package com.blibli.training.product.model.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.blibli.training.framework.dto.BasePagingRequest;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest extends BasePagingRequest {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

}
