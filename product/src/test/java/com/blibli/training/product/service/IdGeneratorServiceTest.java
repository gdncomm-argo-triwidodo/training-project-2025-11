package com.blibli.training.product.service;

import com.blibli.training.product.entity.Sequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private IdGeneratorService idGeneratorService;

    @Test
    void generateProductId_ShouldReturnFormattedId() {
        // Given
        Sequence sequence = new Sequence();
        sequence.setId("product_sequence");
        sequence.setValue(100001L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        )).thenReturn(sequence);

        // When
        String result = idGeneratorService.generateProductId();

        // Then
        assertNotNull(result);
        assertEquals("MTA-100001", result);
        verify(mongoOperations, times(1)).findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        );
    }

    @Test
    void generateProductId_WithDifferentSequenceValue_ShouldReturnCorrectFormat() {
        // Given
        Sequence sequence = new Sequence();
        sequence.setId("product_sequence");
        sequence.setValue(123456L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        )).thenReturn(sequence);

        // When
        String result = idGeneratorService.generateProductId();

        // Then
        assertNotNull(result);
        assertEquals("MTA-123456", result);
    }

    @Test
    void generateProductId_WithSmallSequenceValue_ShouldPadWithZeros() {
        // Given
        Sequence sequence = new Sequence();
        sequence.setId("product_sequence");
        sequence.setValue(42L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        )).thenReturn(sequence);

        // When
        String result = idGeneratorService.generateProductId();

        // Then
        assertNotNull(result);
        assertEquals("MTA-000042", result);
    }

    @Test
    void generateProductId_WhenSequenceIsNull_ShouldReturnDefaultId() {
        // Given
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        )).thenReturn(null);

        // When
        String result = idGeneratorService.generateProductId();

        // Then
        assertNotNull(result);
        assertEquals("MTA-100001", result);
    }

    @Test
    void generateProductId_ShouldIncrementSequenceValue() {
        // Given
        Sequence sequence1 = new Sequence();
        sequence1.setId("product_sequence");
        sequence1.setValue(100001L);

        Sequence sequence2 = new Sequence();
        sequence2.setId("product_sequence");
        sequence2.setValue(100002L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        )).thenReturn(sequence1, sequence2);

        // When
        String result1 = idGeneratorService.generateProductId();
        String result2 = idGeneratorService.generateProductId();

        // Then
        assertEquals("MTA-100001", result1);
        assertEquals("MTA-100002", result2);
        verify(mongoOperations, times(2)).findAndModify(
                any(Query.class),
                any(Update.class),
                any(),
                eq(Sequence.class)
        );
    }
}

