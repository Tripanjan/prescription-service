# Feign Client Migration Guide

## What Changed?

Your prescription service has been migrated from **RestTemplate** to **Spring Cloud OpenFeign** for inter-service communication with the Medicine Service.

---

## Files Changed

### 1. **pom.xml**
- Added `spring-cloud-starter-openfeign` dependency
- Added `spring-cloud-dependencies` BOM for version management

### 2. **PrescriptionServiceApplication.java**
- Added `@EnableFeignClients` annotation to enable Feign client scanning

### 3. **MedicineServiceClient.java** (Completely Rewritten)
- Changed from a `@Component` class with `RestTemplate` to a `@FeignClient` interface
- Removed manual URL construction and JSON parsing
- Now uses declarative HTTP annotations

### 4. **AppConfig.java**
- Removed `RestTemplate` bean (no longer needed)

### 5. **application.yaml**
- Added Feign client configuration (timeouts, logging, compression)

### 6. **New Files Created:**
- `FeignConfig.java` - Custom Feign configuration
- `FeignErrorDecoder.java` - Custom error handling
- `MedicineServiceResponse.java` - Response wrapper DTO
- `StockCheckResponse.java` - Stock check response DTO

---

## Line-by-Line Explanation of Feign Client

### MedicineServiceClient.java

```java
@FeignClient(
    name = "medicine-service",
    url = "${medicine.service.url}"
)
public interface MedicineServiceClient {
```

**Explanation:**
- `@FeignClient`: Tells Spring to create a Feign client implementation at runtime
- `name`: Logical name for the client (used for metrics, logging, service discovery)
- `url`: Base URL from application.yaml (http://localhost:8081/api/medicines)
- `interface`: Feign generates the implementation automatically—no need to write code!

---

```java
@GetMapping("/{medicineId}")
MedicineServiceResponse<MedicineDTO> getMedicineById(@PathVariable("medicineId") Long medicineId);
```

**Explanation:**
- `@GetMapping`: HTTP GET request to `{baseUrl}/{medicineId}`
- `@PathVariable`: Injects `medicineId` into the URL path
- Return type: Feign automatically deserializes JSON response to `MedicineServiceResponse<MedicineDTO>`
- **What Feign does:**
  1. Constructs URL: `http://localhost:8081/api/medicines/1`
  2. Sends HTTP GET request
  3. Receives JSON response
  4. Deserializes JSON to `MedicineServiceResponse<MedicineDTO>`
  5. Returns the object

---

```java
@GetMapping("/{medicineId}/stock")
StockCheckResponse checkStock(
    @PathVariable("medicineId") Long medicineId,
    @RequestParam("quantity") Integer quantity
);
```

**Explanation:**
- `@RequestParam`: Adds query parameter to URL
- **Resulting URL:** `http://localhost:8081/api/medicines/1/stock?quantity=10`
- Feign automatically appends `?quantity=10` to the URL

---

```java
@PostMapping("/{medicineId}/reduce-stock")
void reduceStock(
    @PathVariable("medicineId") Long medicineId,
    @RequestParam("quantity") Integer quantity
);
```

**Explanation:**
- `@PostMapping`: HTTP POST request
- `void` return type: No response body expected
- Feign sends POST request to `http://localhost:8081/api/medicines/1/reduce-stock?quantity=10`

---

### PrescriptionService.java Changes

#### Before (RestTemplate):
```java
MedicineDTO medicine = medicineServiceClient.getMedicineById(itemDTO.getMedicineId());
```

#### After (Feign):
```java
MedicineDTO medicine = medicineServiceClient.getMedicineById(itemDTO.getMedicineId()).getData();
```

**Explanation:**
- Feign returns `MedicineServiceResponse<MedicineDTO>` (wrapper object)
- We call `.getData()` to extract the `MedicineDTO` from the wrapper

---

#### Before (RestTemplate):
```java
boolean hasStock = medicineServiceClient.checkStock(medicine.getId(), itemDTO.getQuantity());
```

#### After (Feign):
```java
boolean hasStock = medicineServiceClient.checkStock(medicine.getId(), itemDTO.getQuantity()).isAvailable();
```

**Explanation:**
- Feign returns `StockCheckResponse` object
- We call `.isAvailable()` to get the boolean value

---

### FeignConfig.java

```java
@Bean
Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
}
```

**Explanation:**
- Sets Feign logging level to `FULL` (logs everything: headers, body, metadata)
- Other levels: `NONE`, `BASIC`, `HEADERS`
- Useful for debugging and monitoring inter-service communication

---

```java
@Bean
public ErrorDecoder errorDecoder() {
    return new FeignErrorDecoder();
}
```

**Explanation:**
- Custom error decoder to handle HTTP errors from remote services
- Converts HTTP status codes to appropriate Java exceptions

---

### FeignErrorDecoder.java

```java
switch (response.status()) {
    case 404:
        return new ResourceNotFoundException("Resource not found in Medicine Service");
    case 400:
        return new IllegalArgumentException("Invalid request to Medicine Service");
    case 503:
        return new RuntimeException("Medicine Service is currently unavailable");
    default:
        return defaultDecoder.decode(methodKey, response);
}
```

**Explanation:**
- Intercepts HTTP errors from Medicine Service
- Maps status codes to meaningful Java exceptions
- `404 → ResourceNotFoundException`
- `400 → IllegalArgumentException`
- `503 → RuntimeException` (service unavailable)

---

### application.yaml

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000  # Max time to establish connection (5 seconds)
        readTimeout: 5000     # Max time to wait for response (5 seconds)
        loggerLevel: BASIC    # Log method, URL, status, execution time
```

**Explanation:**
- `connectTimeout`: How long to wait for connection establishment before failing
- `readTimeout`: How long to wait for response before failing
- `loggerLevel`: What to log for debugging

---

```yaml
      medicine-service:
        connectTimeout: 3000
        readTimeout: 3000
        loggerLevel: FULL
```

**Explanation:**
- Override config specifically for `medicine-service` client
- Shorter timeouts for faster failure detection
- `FULL` logging for detailed debugging

---

```yaml
  compression:
    request:
      enabled: true
      min-request-size: 2048  # Compress requests > 2KB
    response:
      enabled: true
```

**Explanation:**
- Enables GZIP compression for large requests/responses
- Reduces network bandwidth usage
- Only compresses requests larger than 2048 bytes

---

## Comparison: RestTemplate vs Feign Client

| Feature | RestTemplate (Before) | Feign Client (After) |
|---------|----------------------|---------------------|
| **Code Type** | Imperative (manual code) | Declarative (annotations) |
| **Lines of Code** | ~100 lines | ~20 lines |
| **URL Construction** | Manual string concatenation | Automatic via annotations |
| **JSON Parsing** | Manual `Map<String, Object>` parsing | Automatic deserialization |
| **Error Handling** | Try-catch in every method | Centralized error decoder |
| **Configuration** | Manual `SimpleClientHttpRequestFactory` | YAML-based configuration |
| **Logging** | Manual logging statements | Built-in logging with levels |
| **Timeouts** | Configured in Java code | Configured in YAML |
| **Retries** | Manual implementation | Built-in with config |
| **Circuit Breaker** | Requires manual integration | Easy integration with Resilience4j |
| **Load Balancing** | Manual implementation | Built-in with Ribbon/Spring Cloud LoadBalancer |
| **Service Discovery** | Manual URL management | Automatic with Eureka/Consul |
| **Testing** | Requires extensive mocking | Easy to mock interface |
| **Maintainability** | High (verbose code) | Low (interface changes only) |

---

## Key Benefits of Feign

### 1. **Less Boilerplate Code**
- No manual URL construction
- No JSON parsing with `Map<String, Object>`
- No try-catch blocks in every method

### 2. **Type Safety**
- Strong typing with DTOs (no raw Maps)
- Compile-time checking of method signatures

### 3. **Declarative Syntax**
- Looks like a REST controller (familiar)
- Easy to understand and maintain

### 4. **Built-in Features**
- Automatic retry on failure
- Load balancing across multiple instances
- Circuit breaker integration
- Request/response compression

### 5. **Centralized Configuration**
- Timeouts, logging, compression in YAML
- Per-client configuration override
- Easy to change without code changes

### 6. **Better Error Handling**
- Custom error decoder for all clients
- Consistent exception mapping
- Easier debugging with detailed logs

### 7. **Integration with Spring Cloud**
- Works seamlessly with Eureka (service discovery)
- Works with Ribbon/LoadBalancer
- Works with Hystrix/Resilience4j (circuit breaker)

---

## How Feign Works Internally

1. **Application Startup:**
   - Spring scans for `@FeignClient` interfaces
   - Creates a proxy implementation at runtime
   - Registers the proxy as a Spring bean

2. **Method Call:**
   - You call `medicineServiceClient.getMedicineById(1)`
   - Feign intercepts the call
   - Constructs HTTP request from annotations
   - Sends request to `http://localhost:8081/api/medicines/1`
   - Receives JSON response
   - Deserializes to `MedicineServiceResponse<MedicineDTO>`
   - Returns the object

3. **Error Handling:**
   - If HTTP error occurs (4xx, 5xx)
   - Feign calls `errorDecoder.decode()`
   - Custom decoder maps status to exception
   - Exception is thrown to caller

---

## When to Use Feign vs RestTemplate vs WebClient

| Client | Use Case | Pros | Cons |
|--------|----------|------|------|
| **Feign** | Microservices communication | Declarative, less code, better integration | Requires Spring Cloud |
| **RestTemplate** | Simple HTTP calls, legacy code | Simple, no extra dependencies | Verbose, manual error handling |
| **WebClient** | Reactive/async applications | Non-blocking, reactive streams | Steep learning curve, complex |

---

## Best Practices

1. **Use Feign for Microservices**: Perfect for service-to-service communication
2. **Configure Timeouts**: Always set realistic timeouts to prevent hanging
3. **Enable Logging**: Use `FULL` in dev, `BASIC` in production
4. **Implement Error Decoder**: Handle errors consistently across all clients
5. **Use DTOs**: Define response wrappers for type safety
6. **Externalize URLs**: Use `${medicine.service.url}` instead of hardcoding
7. **Enable Compression**: For large requests/responses
8. **Add Circuit Breaker**: Prevent cascading failures (add Resilience4j)

---

## Running with Feign

Nothing changes! Use the same Docker commands:

```bash
docker-compose up -d
```

Feign is a **drop-in replacement** for RestTemplate with zero impact on the API or Docker setup.

---

## Troubleshooting

### Feign Client Not Found
- Ensure `@EnableFeignClients` is on the main application class
- Check `@FeignClient` annotation on the interface

### Connection Timeout
- Increase `connectTimeout` in application.yaml
- Check if Medicine Service is running

### Read Timeout
- Increase `readTimeout` in application.yaml
- Check if Medicine Service is responding slowly

### 404 Not Found
- Check the `url` in `@FeignClient`
- Verify the endpoint path in `@GetMapping`

### JSON Deserialization Error
- Ensure DTOs match the response structure
- Check Feign logs with `loggerLevel: FULL`

---

## Next Steps

- **Add Circuit Breaker**: Integrate Resilience4j for fault tolerance
- **Add Service Discovery**: Use Eureka for dynamic service URLs
- **Add Load Balancing**: Use Spring Cloud LoadBalancer for multiple instances
- **Add Metrics**: Monitor Feign calls with Micrometer/Prometheus

---

**Summary:** You've successfully migrated from RestTemplate to Feign Client, reducing code by 80% and gaining better error handling, logging, and integration capabilities!
