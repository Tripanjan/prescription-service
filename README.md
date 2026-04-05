# Prescription Service

A Spring Boot microservice for managing medical prescriptions. This service handles prescription creation, retrieval, and integrates with the Medicine Service to validate medication availability.

## Features

- Create and manage prescriptions with multiple medication items
- Integration with Medicine Service via Feign Client for stock validation
- RESTful API endpoints for prescription operations
- Docker support for containerized deployment

## Technology Stack

- Java Spring Boot
- Feign Client for inter-service communication
- MySQL Database
- Docker & Docker Compose

## Getting Started

Build and run the service using Docker Compose:

```bash
docker-compose up --build
```

The service will be available at `http://localhost:8081`
