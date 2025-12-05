# Mini E-commerce System

A microservices-based e-commerce system built with Spring Boot.

## Architecture
- **API Gateway**: Entry point, handles routing and authentication.
- **Member Service**: Handles user registration and login.
- **Product Service**: Manages products.
- **Cart Service**: Manages user carts.
- **Generic Framework**: Shared library for common code.

## Setup
1. Run `mvn clean install` in `generic-framework`.
2. Run `mvn spring-boot:run` in each service directory.
