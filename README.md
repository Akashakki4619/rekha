# Rate Limited API - Spring Boot Application

A Spring Boot application that implements rate limiting using the **Token Bucket Algorithm** with Java 8 and Spring Framework 4.5.3.

## Features

- **Token Bucket Rate Limiting**: Implements rate limiting using the token bucket algorithm
- **Per-IP Rate Limiting**: Each client IP gets its own rate limit bucket
- **Multiple Endpoints**: Different rate limits for different API endpoints
- **Real-time Status**: Check remaining tokens for your IP address
- **Graceful Error Handling**: Proper HTTP 429 responses when rate limit is exceeded
- **Spring Boot 2.1.x**: Uses Spring Framework 4.5.3 as specified
- **Java 8 Compatible**: Built with Java 8 support

## Rate Limiting Configuration

- **Rate Limit**: 10 requests per minute per endpoint per IP address
- **Algorithm**: Token Bucket (using Bucket4j library)
- **Scope**: Per client IP address and per endpoint

## API Endpoints

### 1. GET /api/dummy
- **Description**: Basic dummy endpoint
- **Rate Limit**: 10 requests/minute
- **Response**: JSON with greeting message and metadata

### 2. POST /api/dummy
- **Description**: Accepts JSON data
- **Rate Limit**: 10 requests/minute
- **Body**: Any JSON object (optional)
- **Response**: Confirmation message with received data

### 3. GET /api/dummy/{id}
- **Description**: Get dummy data by ID
- **Rate Limit**: 10 requests/minute
- **Response**: JSON with ID-specific data

### 4. GET /api/status
- **Description**: Check rate limit status
- **Rate Limit**: None (not rate limited)
- **Response**: Current available tokens for all endpoints

## Prerequisites

- Java 8 or higher
- Maven 3.x

## How to Run

1. **Clone or download the project**

2. **Build the application**:
   ```bash
   mvn clean compile
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Alternative - Run JAR directly**:
   ```bash
   mvn clean package
   java -jar target/rate-limited-api-1.0.0.jar
   ```

The application will start on `http://localhost:8080`

## Testing the Rate Limiting

### 1. Test Normal Request
```bash
curl http://localhost:8080/api/dummy
```

### 2. Test Rate Limiting
Run this command multiple times quickly (more than 10 times within a minute):
```bash
for i in {1..15}; do curl http://localhost:8080/api/dummy; echo ""; done
```

After the 10th request, you should receive HTTP 429 responses.

### 3. Check Rate Limit Status
```bash
curl http://localhost:8080/api/status
```

### 4. Test POST Endpoint
```bash
curl -X POST http://localhost:8080/api/dummy \
  -H "Content-Type: application/json" \
  -d '{"test": "data", "timestamp": "2024-01-01"}'
```

### 5. Test ID-based Endpoint
```bash
curl http://localhost:8080/api/dummy/123
```

## Example Responses

### Successful Request
```json
{
  "message": "Hello from dummy API!",
  "timestamp": "2024-01-15T10:30:45.123",
  "method": "GET",
  "endpoint": "/api/dummy",
  "clientIp": "127.0.0.1"
}
```

### Rate Limit Exceeded
```json
{
  "timestamp": "2024-01-15T10:31:00.456",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later.",
  "path": "/api/dummy"
}
```

### Rate Limit Status
```json
{
  "clientIp": "127.0.0.1",
  "availableTokens": {
    "dummy-get": 7,
    "dummy-post": 10,
    "dummy-get-by-id": 10
  },
  "rateLimitInfo": "10 requests per minute per endpoint",
  "timestamp": "2024-01-15T10:30:45.789"
}
```

## Architecture

### Components

1. **RateLimitingService**: Manages token buckets using Bucket4j
2. **@RateLimit Annotation**: Custom annotation for marking rate-limited methods
3. **RateLimitingAspect**: AOP aspect that intercepts and applies rate limiting
4. **GlobalExceptionHandler**: Handles rate limit exceptions and returns proper HTTP responses
5. **DummyApiController**: REST controller with sample endpoints

### Token Bucket Algorithm

- Each client IP gets a separate bucket per endpoint
- Bucket capacity: 10 tokens
- Refill rate: 10 tokens per minute
- Each API call consumes 1 token
- When bucket is empty, requests are rejected with HTTP 429

## Technologies Used

- **Spring Boot 2.1.18** (includes Spring Framework 4.5.3)
- **Java 8**
- **Bucket4j 4.10.0** (Token bucket implementation)
- **Spring AOP** (Aspect-oriented programming)
- **Maven** (Build tool)

## Health Check

Check application health:
```bash
curl http://localhost:8080/actuator/health
```

## Customization

### Modify Rate Limits

Edit `RateLimitingService.java` to change the rate limiting configuration:

```java
// Current: 10 requests per minute
Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));

// Example: 5 requests per 30 seconds
Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(30)));
```

### Add New Rate-Limited Endpoints

1. Add `@RateLimit(key = "your-key")` annotation to any controller method
2. The rate limiting will be automatically applied

## Troubleshooting

1. **Port already in use**: Change the port in `application.yml`
2. **Rate limits not working**: Check that AOP is enabled and aspects are being applied
3. **Maven build issues**: Ensure Java 8 is properly configured

## License

This project is for educational/demonstration purposes.