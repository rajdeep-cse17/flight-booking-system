# Flight Booking Microservice Architecture

A comprehensive microservice-based flight booking application built with Spring Boot, featuring user authentication, flight search, booking management, and payment processing.

## 🚀 **Quick Start (Automated)**

For the fastest setup, use our automated scripts:

```bash
# Run the complete E2E test (builds, starts services, runs tests)
./run-e2e-test.sh

# Clean up everything when done
./cleanup.sh
```

**What the automated script does:**
- ✅ Builds all microservices
- ✅ Starts DynamoDB Local and Redis
- ✅ Creates database tables and sample data
- ✅ Starts all microservices
- ✅ Runs sanity checks
- ✅ Executes end-to-end test
- ✅ Provides detailed results

## 🏗️ **Architecture Overview**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway  │    │   Load Balancer │    │   Eureka Server │
│   (Port 8080)  │◄──►│   (Port 8761)   │◄──►│   (Port 8761)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │
         ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  User Service  │    │ Search Service  │    │ Booking Service │
│  (Port 8081)   │    │  (Port 8082)    │    │  (Port 8083)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User DB      │    │   Flight DB     │    │  Inventory DB   │
│  (DynamoDB)    │    │  (DynamoDB)     │    │   (DynamoDB)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Redis Cache   │    │   Booking DB    │
                       │   (Port 6379)   │    │   (DynamoDB)    │
                       └─────────────────┘    └─────────────────┘
```

## Microservices

### 1. User Service (Port 8081)
**Purpose**: User management and authentication
**Database**: User table (DynamoDB)
**Key Features**:
- User authentication
- User profile management
- Total booking value tracking

**API Endpoints**:
- `POST /api/users/authenticate` - Authenticate user
- `GET /api/users/{userId}` - Get user details
- `PUT /api/users/{userId}/total-booking-value` - Update booking value
- `POST /api/users` - Create new user

### 2. Search Service (Port 8082)
**Purpose**: Flight search and discovery
**Database**: Flight table (DynamoDB)
**Cache**: Redis
**Key Features**:
- Flight search with preferences (cheapest/fastest)
- Caching for search results
- Graph-based route finding (up to 5 stops)

**API Endpoints**:
- `GET /api/search/flights` - Search flights with preferences

**Search Preferences**:
- `cheapest` - Returns top 10 cheapest routes
- `fastest` - Returns top 10 routes with fewest flights
- `none` - Returns all available routes

### 3. Booking Service (Port 8083)
**Purpose**: Flight booking and inventory management
**Database**: Inventory and Booking tables (DynamoDB)
**Key Features**:
- Inventory checking and locking
- Booking creation and management
- Payment integration
- User booking value updates

**API Endpoints**:
- `POST /api/booking/flight/book` - Book a flight

**Booking Flow**:
1. Check inventory availability
2. Lock inventory (decrease available seats)
3. Create booking with PROCESSING status
4. Send to payment service
5. Wait for payment response (3-minute timeout)
6. Update booking status (SUCCESS/FAILED)
7. Update user's total booking value
8. Release inventory lock

### 4. Payment Service (Port 8084)
**Purpose**: Payment processing
**Key Features**:
- Payment validation
- Transaction management
- Hardcoded success response (for exercise)

**API Endpoints**:
- `POST /api/payment/pay` - Process payment

### 5. API Gateway (Port 8080)
**Purpose**: Centralized routing and load balancing
**Key Features**:
- Route distribution to microservices
- Circuit breaker implementation
- CORS configuration
- Service discovery integration

## Database Schema

### User Table
```json
{
  "userId": "string (Primary Key)",
  "memberSince": "datetime",
  "name": "string",
  "totalBookingValue": "number"
}
```

### Flight Table
```json
{
  "flightId": "string (Primary Key)",
  "daysOfWeek": ["string"],
  "source": "string",
  "destination": "string",
  "cost": "number"
}
```

### Inventory Table
```json
{
  "flightId": "string (Primary Key)",
  "date": "date (Primary Key)",
  "numberOfSeatsLeft": "number"
}
```

### Booking Table
```json
{
  "bookingId": "string (Primary Key)",
  "userId": "string",
  "flightIds": ["string"],
  "date": "date",
  "source": "string",
  "destination": "string",
  "status": "string (PROCESSING/SUCCESS/FAILED)",
  "cost": "number"
}
```

## Technology Stack

- **Framework**: Spring Boot 2.7.0
- **Cloud**: Spring Cloud 2021.0.3
- **Database**: DynamoDB (AWS)
- **Cache**: Redis
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway
- **Load Balancer**: Spring Cloud LoadBalancer
- **Circuit Breaker**: Resilience4j

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (for Redis)
- AWS CLI configured
- DynamoDB tables created

### Running the Services

1. **Start Redis Cache**:
```bash
docker run -d -p 6379:6379 redis:alpine
```

2. **Start Eureka Server**:
```bash
cd eureka-server
mvn spring-boot:run
```

3. **Start User Service**:
```bash
cd user-service
mvn spring-boot:run
```

4. **Start Search Service**:
```bash
cd search-service
mvn spring-boot:run
```

5. **Start Booking Service**:
```bash
cd booking-service
mvn spring-boot:run
```

6. **Start Payment Service**:
```bash
cd payment-service
mvn spring-boot:run
```

7. **Start API Gateway**:
```bash
cd api-gateway
mvn spring-boot:run
```

### Service URLs
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Search Service**: http://localhost:8082
- **Booking Service**: http://localhost:8083
- **Payment Service**: http://localhost:8084
- **Eureka Server**: http://localhost:8761

## API Examples

### Search Flights
```bash
GET http://localhost:8080/api/search/flights?userId=user123&source=DEL&destination=BOM&preference=cheapest
```

### Book Flight
```bash
POST http://localhost:8080/api/booking/flight/book
Content-Type: application/json

{
  "userId": "user123",
  "flightIds": ["F001", "F002"],
  "date": "2024-01-15",
  "numberOfPassengers": 2
}
```

### Process Payment
```bash
POST http://localhost:8080/api/payment/pay
Content-Type: application/json

{
  "cardNumber": "1234567890123456",
  "expiryDate": "12/25",
  "cvv": "123",
  "cardHolderName": "John Doe",
  "amount": 299.99
}
```

## Key Features

✅ **Microservice Architecture**: Decoupled services with clear boundaries  
✅ **Service Discovery**: Eureka-based service registration and discovery  
✅ **Load Balancing**: Automatic load distribution across service instances  
✅ **Circuit Breaker**: Fault tolerance and fallback mechanisms  
✅ **Caching**: Redis-based caching for search results  
✅ **Database Isolation**: Each service owns its data  
✅ **API Gateway**: Centralized routing and security  
✅ **Scalability**: Horizontal scaling capability  
✅ **Fault Tolerance**: Graceful degradation and error handling  

## Monitoring and Health Checks

- **Eureka Dashboard**: http://localhost:8761
- **Service Health**: Each service exposes `/actuator/health` endpoint
- **Metrics**: Spring Boot Actuator metrics available

## Security Considerations

- API Gateway can implement authentication middleware
- Service-to-service communication can use mutual TLS
- Database access can be restricted with IAM policies
- CORS configured for cross-origin requests

## Deployment

### Docker Deployment
```bash
# Build all services
mvn clean package -DskipTests

# Run with Docker Compose
docker-compose up -d
```

### AWS Deployment
- Deploy to ECS/EKS
- Use Application Load Balancer
- Configure Auto Scaling Groups
- Set up CloudWatch monitoring

## Future Enhancements

- **Authentication**: JWT-based authentication
- **Rate Limiting**: API rate limiting and throttling
- **Logging**: Centralized logging with ELK stack
- **Monitoring**: Prometheus and Grafana integration
- **Tracing**: Distributed tracing with Jaeger
- **Security**: OAuth2 and OIDC integration
- **Testing**: Contract testing with Pact
- **CI/CD**: Automated deployment pipelines 