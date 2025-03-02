# Complaints Service

## Overview
The Complaints Service is a RESTful API designed to manage complaints ("reklamacje"). The service supports:

- Adding new complaints
- Updating complaint content
- Retrieving all stored complaints

Each complaint includes:

- **Product Identifier (`productId`)**
- **Complaint Content (`content`)**
- **Creation Date (`createdDate`)**
- **Complainant (`complainant`)**
- **Country (determined from the client’s IP)**
- **Complaint Counter (incremented if a duplicate complaint is submitted)**

If a complaint with the same `productId` and the same `complainant` already exists, the service increments the counter without updating other fields.

## Features

### Create Complaint
Adds a new complaint or increments the counter if a duplicate (same `productId` and `complainant`) already exists.

### Update Complaint
Updates the content of an existing complaint identified by its ID.

### Retrieve Complaints
Retrieves a list of all stored complaints.

### IP-based Country Lookup
Determines the client’s country based on their public IP, using a free external API (e.g., `ip-api.com`).

### Caching
Implements caching (with Redis) for frequently accessed data (e.g., complaint lookups, country lookups) to reduce database and external API calls.

### Global Error Handling
Provides a consistent error response model via a `@RestControllerAdvice`.

### Swagger Documentation
Automatically generates API documentation via OpenAPI/Swagger UI.

## Technologies Used

- **Java 17**: Exploits modern language features (e.g., records) for cleaner code.
- **Spring Boot 3**: Simplifies development with auto-configuration, embedded Tomcat, and robust ecosystem support.
- **Maven**: Dependency management and project build.
- **Spring Data JPA**: Integration with PostgreSQL (ORM capabilities).
- **PostgreSQL**: Relational database for persistent storage.
- **Spring Data Redis**: Caching layer to improve performance.
- **MapStruct**: Automated DTO-to-Entity (and vice versa) mapping.
- **Lombok**: Reduces boilerplate through annotations such as `@Getter`, `@Setter`, `@RequiredArgsConstructor`.
- **Jakarta Bean Validation**: Validates incoming request DTOs (`@Valid`, `@NotBlank`, etc.).
- **Swagger / OpenAPI (SpringDoc)**: Generates interactive API documentation.
- **Nginx**: Acts as a reverse proxy to the Spring Boot application, handling incoming requests, setting headers (e.g., `X-Forwarded-For`), and forwarding them to the application.
- **Docker & Docker Compose**: Containerizes the application and dependencies (PostgreSQL, Redis, Nginx) for consistent deployments across different environments.
- **TestContainers & WireMock**: Integration testing with real PostgreSQL instances (via TestContainers) and external API simulation (via WireMock).

## Architecture and Design

### RESTful API
Follows standard REST conventions with appropriate HTTP methods (`POST`, `PUT`, `GET`).

### Clean Code & Separation of Concerns
Divided into controllers, services, repositories, and mappers for clarity and maintainability.

### Nginx Reverse Proxy
- Terminates HTTP requests on port `80` (or `443` if SSL is configured) and forwards them to the Spring Boot application running on port `8080`.
- Manages security, logging (`X-Forwarded-For`), and can be extended for load balancing or caching.

### Global Error Handling
A `@RestControllerAdvice` handles exceptions globally, returning a unified error JSON payload.

### Caching Strategy
Uses Redis to cache complaint data and IP-to-country lookups, reducing both database load and external API calls.

### External API Integration
- Queries a free IP geolocation API (like `ip-api.com`) to determine the client’s country.
- Uses caching and fallback mechanisms to ensure resilience if the external API is unavailable.

## Nginx: Why We Use It and How It Works

Reverse Proxy:
Nginx stands in front of the Spring Boot service (which typically runs on port 8080). It handles incoming traffic on port 80 (and optionally 443 for HTTPS). This approach simplifies networking configurations and can improve security (e.g., managing SSL termination, request limiting, etc.).

Load Balancing & Scalability:
In more advanced setups, Nginx can distribute traffic across multiple containers or services.

Security and Observability:

You can apply various security measures (IP filtering, rate limiting, etc.) directly in Nginx.
Nginx sets headers such as X-Forwarded-For and X-Real-IP, enabling the Spring Boot application to correctly identify the real client IP, even in a containerized environment.

## Running the Application

### Prerequisites
- Docker and Docker Compose installed on your machine.

### Steps

#### Build and Start Containers
```bash
docker-compose up --build
```
This will spin up:

- `complaints-service` (Spring Boot, port 8080 internally)
- `postgres` (database)
- `redis` (cache)
- `nginx` (reverse proxy, port 80 externally)

#### Verify Service Health
Check Docker Compose logs. Once everything is healthy, your application will be accessible via:
```bash
http://localhost/swagger-ui/index.html
```

## API Documentation
Viewable via Swagger UI:

### **POST** `/api/complaints`
Create a new complaint or increment an existing complaint’s counter.

### **PUT** `/api/complaints/{id}`
Update the content of an existing complaint.

### **GET** `/api/complaints`
Retrieve all complaints.

## Testing

### Local Testing (Curl / CLI)
```bash
curl -i -X POST \
     -H "Content-Type: application/json" \
     -d '{"productId":"123","content":"My complaint","complainant":"John Doe"}' \
     http://localhost/api/complaints
```

### Integration / Unit Tests
- **JUnit 5 + Mockito**: For unit testing service logic.
- **Spring Boot Test**: For end-to-end integration tests.
- **TestContainers**: Runs a real PostgreSQL instance.
- **WireMock**: Mocks external IP APIs.

## Exposing Locally Hosted App with Ngrok

In many cases, you may be behind a router or your ISP might not provide a truly public IP, making it difficult or impossible to expose your local application to the internet. You might also face issues with NAT loopback (hairpin NAT) or complex router configuration that prevents external users or services from reaching your local network.

Ngrok solves this by creating a secure tunnel from a public URL to your local machine, allowing you to quickly test external integrations, see real public IP addresses in logs, or share your in-development API with teammates.

### Steps with Ngrok:
#### Start Docker
```bash
docker-compose up -d
```
Ensure Nginx is listening on port `80` on your local machine.

#### Install and Run Ngrok
- Download from [ngrok.com](https://ngrok.com/).
- Run:
```bash
ngrok http 80
```
You’ll see something like:
```bash
https://abcd-1234.ngrok-free.app -> http://localhost:80
```

#### Access the Public URL
Anyone (including you) can now go to:
```bash
https://abcd-1234.ngrok-free.app/swagger-ui/index.html
```
and invoke your endpoints.

The requests are forwarded securely through ngrok to your local Nginx, which then passes them to the Spring Boot application.

Result

- No router reconfiguration needed.
- No public IP necessary from your ISP.
- Spring Boot will see real public IPs in the X-Forwarded-For header. This is helpful for geolocation testing (e.g., verifying a complaint comes from a certain country or seeing the IP from a different VPN).

```bash
curl -i -X POST \
     -H "Content-Type: application/json" \
     -d '{"productId":"123","content":"My ngrok test","complainant":"Alice"}' \
     https://abcd-1234.ngrok-free.app/api/complaints
```

## API Documentation
Viewable via Swagger UI:
- POST /api/complaints
Create a new complaint or increment an existing complaint’s counter.
- PUT /api/complaints/{id}
Update the content of an existing complaint.
- GET /api/complaints
Retrieve all complaints.
Global Error Responses
A unified JSON structure returned for validation errors, exceptions, etc.

## Conclusion
This project demonstrates a modern, production-grade RESTful API with Java 17 and Spring Boot 3, showcasing:

- Clean architecture and layered design (controllers, services, repositories, mappers)
- HTTP reverse proxy via Nginx to manage incoming requests, real IP headers, and potential load balancing
- Robust error handling (`@RestControllerAdvice`)
- Redis caching for performance and reduced external dependencies
- Containerization via Docker & Docker Compose for consistent local/production environments
- Comprehensive testing (unit, integration, external mocking)
- Ngrok usage for quickly exposing a local service, sidestepping router/NAT issues, and capturing real external IPs for geolocation or analytics

By following these steps and examples, you can develop, test, and expand the Complaints Service with minimal friction, regardless of local network or ISP constraints. Feel free to extend or customize the approach for specific security, scaling, or infrastructure needs.
