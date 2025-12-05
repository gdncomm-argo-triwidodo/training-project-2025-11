package com.blibli.training.framework.dto;

import com.blibli.training.framework.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTest {

    @Test
    void success_WithData_ShouldReturnSuccessResponse() {
        // Given
        String data = "Test Data";

        // When
        BaseResponse<String> response = BaseResponse.success(data);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void success_WithNullData_ShouldReturnSuccessResponseWithNullData() {
        // When
        BaseResponse<String> response = BaseResponse.success(null);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNull(response.getData());
    }

    @Test
    void success_WithComplexObject_ShouldReturnSuccessResponse() {
        // Given
        TestObject testObject = new TestObject("test", 123);

        // When
        BaseResponse<TestObject> response = BaseResponse.success(testObject);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(testObject, response.getData());
    }

    @Test
    void error_WithCodeAndException_ShouldReturnErrorResponse() {
        // Given
        int errorCode = 401;
        Exception exception = new AuthenticationException("Unauthorized");

        // When
        BaseResponse<String> response = BaseResponse.error(errorCode, exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(errorCode, response.getCode());
        assertEquals("Unauthorized", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void error_WithDifferentCodes_ShouldReturnCorrectCode() {
        // When
        BaseResponse<String> response400 = BaseResponse.error(400, new Exception("Bad Request"));
        BaseResponse<String> response404 = BaseResponse.error(404, new Exception("Not Found"));
        BaseResponse<String> response500 = BaseResponse.error(500, new Exception("Internal Error"));

        // Then
        assertEquals(400, response400.getCode());
        assertEquals(404, response404.getCode());
        assertEquals(500, response500.getCode());
    }

    @Test
    void builder_ShouldCreateCustomResponse() {
        // When
        BaseResponse<String> response = BaseResponse.<String>builder()
                .success(true)
                .code(201)
                .message("Created")
                .data("New Resource")
                .build();

        // Then
        assertTrue(response.isSuccess());
        assertEquals(201, response.getCode());
        assertEquals("Created", response.getMessage());
        assertEquals("New Resource", response.getData());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyResponse() {
        // When
        BaseResponse<String> response = new BaseResponse<>();

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(0, response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void allArgsConstructor_ShouldCreateFullResponse() {
        // When
        BaseResponse<String> response = new BaseResponse<>(true, 200, "OK", "Data");

        // Then
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("OK", response.getMessage());
        assertEquals("Data", response.getData());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        BaseResponse<String> response = new BaseResponse<>();

        // When
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("Test Message");
        response.setData("Test Data");

        // Then
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals("Test Message", response.getMessage());
        assertEquals("Test Data", response.getData());
    }

    @Test
    void success_WithDifferentDataTypes_ShouldWork() {
        // When
        BaseResponse<Integer> intResponse = BaseResponse.success(42);
        BaseResponse<Boolean> boolResponse = BaseResponse.success(true);
        BaseResponse<Double> doubleResponse = BaseResponse.success(3.14);

        // Then
        assertEquals(42, intResponse.getData());
        assertTrue(boolResponse.getData());
        assertEquals(3.14, doubleResponse.getData());
    }

    @Test
    void error_WithExceptionMessage_ShouldUseExceptionMessage() {
        // Given
        Exception exception = new RuntimeException("Custom error message");

        // When
        BaseResponse<String> response = BaseResponse.error(500, exception);

        // Then
        assertEquals("Custom error message", response.getMessage());
    }

    // Helper class for testing
    private static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}

