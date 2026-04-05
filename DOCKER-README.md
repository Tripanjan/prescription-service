# Docker Setup for Prescription Service

This document explains how to build and run the Prescription Service using Docker.

## Prerequisites

- Docker installed (version 20.10+)
- Docker Compose installed (version 2.0+)

## Quick Start

### Using Docker Compose (Recommended)

Start all services (prescription-service + MySQL):

```bash
docker-compose up -d
```

Stop all services:

```bash
docker-compose down
```

Stop and remove volumes (clean database):

```bash
docker-compose down -v
```

View logs:

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f prescription-service
```

### Using Dockerfile Only

Build the image:

```bash
docker build -t prescription-service:latest .
```

Run the container (requires external MySQL):

```bash
docker run -d \
  -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/prescription_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  --name prescription-service \
  prescription-service:latest
```

### Using Jib (Maven Plugin)

Build and push to local Docker daemon:

```bash
mvn clean compile jib:dockerBuild
```

Build and push to registry:

```bash
mvn clean compile jib:build
```

## Service Endpoints

Once running, the service is available at:

- API Base: `http://localhost:8082/api/prescriptions`
- Health Check: `http://localhost:8082/actuator/health`

## Configuration

### Environment Variables

The following environment variables can be configured:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/prescription_db` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `root` | Database password |
| `SERVER_PORT` | `8082` | Application port |
| `MEDICINE_SERVICE_URL` | `http://localhost:8081/api/medicines` | Medicine service URL |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | JVM options |

### Docker Compose Services

- **prescription-db**: MySQL 8.0 database (exposed on port 3307)
- **prescription-service**: Spring Boot application (exposed on port 8082)

## Networking

Services communicate via the `medtrip-network` bridge network:

- `prescription-service` → `prescription-db:3306`
- `prescription-service` → `medicine-service:8081` (if available)

## Data Persistence

MySQL data is persisted in the Docker volume `prescription-db-data`.

## Troubleshooting

### Service won't start

Check logs:
```bash
docker-compose logs prescription-service
```

### Database connection issues

Ensure MySQL is healthy:
```bash
docker-compose ps
docker-compose logs prescription-db
```

### Reset everything

```bash
docker-compose down -v
docker-compose up -d --build
```

## Production Considerations

1. **Security**: Change default passwords in docker-compose.yml
2. **Resources**: Adjust JVM memory settings via JAVA_OPTS
3. **Networking**: Use proper service discovery (Consul, Eureka)
4. **Monitoring**: Add Prometheus/Grafana for metrics
5. **Logging**: Configure centralized logging (ELK stack)
