package com.blibli.training.product.service;

import com.blibli.training.product.entity.Sequence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

@Service
@RequiredArgsConstructor
public class IdGeneratorService {

    private final MongoOperations mongoOperations;

    public String generateProductId() {
        Query query = new Query(Criteria.where("_id").is("product_sequence"));
        Update update = new Update().inc("value", 1);
        
        Sequence sequence = mongoOperations.findAndModify(
            query, 
            update, 
            options().returnNew(true).upsert(true), 
            Sequence.class
        );
        
        long sequenceValue = sequence != null ? sequence.getValue() : 100001;
        return String.format("MTA-%06d", sequenceValue);
    }
}

