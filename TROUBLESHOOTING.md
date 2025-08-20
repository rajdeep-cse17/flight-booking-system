# Troubleshooting Guide

## 🚨 Common Issues and Solutions

### 1. AWS Credentials Error
**Error**: `error getting credentials - err: exit status 1, out: error getting credentials - err: exec: no command, out:`

**Cause**: TestContainers is trying to use AWS CLI credentials but can't find the AWS CLI or proper credentials.

**Solutions**:

#### Option A: Use Local AWS Credentials (Recommended)
```java
// In BaseIntegrationTest.java
BasicAWSCredentials awsCredentials = new BasicAWSCredentials("local", "local");
AmazonDynamoDBClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
    .build();
```

#### Option B: Set Environment Variables
```bash
export AWS_ACCESS_KEY_ID=local
export AWS_SECRET_ACCESS_KEY=local
export AWS_DEFAULT_REGION=us-east-1
```

#### Option C: Create AWS Credentials File
```bash
mkdir -p ~/.aws
cat > ~/.aws/credentials << EOF
[default]
aws_access_key_id = local
aws_secret_access_key = local
EOF

cat > ~/.aws/config << EOF
[default]
region = us-east-1
EOF
```

### 2. Maven Not Found
**Error**: `bash: mvn: command not found`

**Solutions**:

#### Option A: Install Maven
```bash
# macOS with Homebrew
brew install maven

# Ubuntu/Debian
sudo apt-get install maven

# CentOS/RHEL
sudo yum install maven
```

#### Option B: Use Maven Wrapper
```bash
# If mvnw exists in the project
./mvnw test -Dtest=SimpleSearchServiceIntegrationTest
```

#### Option C: Use IDE
- Open the project in IntelliJ IDEA or Eclipse
- Run tests from the IDE's test runner

### 3. Docker Issues
**Error**: Docker containers fail to start

**Solutions**:

#### Check Docker Status
```bash
# Check if Docker is running
docker --version
docker-compose --version

# Start Docker Desktop if needed
open -a Docker
```

#### Check Port Conflicts
```bash
# Check what's using the ports
lsof -i :8080
lsof -i :8000
lsof -i :6379

# Kill processes if needed
kill -9 <PID>
```

#### Check Container Logs
```bash
# View all logs
docker-compose -f docker-compose.integration-test.yml logs

# View specific service logs
docker-compose -f docker-compose.integration-test.yml logs search-service
```

### 4. TestContainers Issues
**Error**: TestContainers can't start containers

**Solutions**:

#### Check Docker Permissions
```bash
# Ensure your user is in the docker group
groups $USER

# Add user to docker group if needed
sudo usermod -aG docker $USER
# Then log out and back in
```

#### Check Available Memory
```bash
# Check available memory
free -h

# Ensure at least 4GB is available for containers
```

#### Use LocalStack Instead
```java
@Container
static LocalStackContainer localStack = new LocalStackContainer(
    DockerImageName.parse("localstack/localstack:latest")
)
    .withServices(LocalStackContainer.Service.DYNAMODB);
```

### 5. Spring Boot Context Issues
**Error**: Spring context fails to load

**Solutions**:

#### Check Configuration
```yaml
# application-integration-test.yml
spring:
  profiles: integration-test
  cloud:
    aws:
      credentials:
        use-default-aws-credentials-chain: false
```

#### Check Dependencies
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

#### Use @TestPropertySource
```java
@TestPropertySource(properties = {
    "spring.profiles.active=integration-test",
    "aws.dynamodb.endpoint=http://localhost:8000"
})
```

## 🔧 Quick Fixes

### 1. Skip Complex Integration Tests
```bash
# Run only unit tests
mvn test -Dtest=*Test -DexcludedGroups=integration

# Run only simple tests
mvn test -Dtest=SimpleSearchServiceIntegrationTest
```

### 2. Use In-Memory Databases
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 3. Mock External Services
```java
@MockBean
private PaymentServiceClient paymentServiceClient;

@MockBean
private UserServiceClient userServiceClient;
```

## 📋 Pre-flight Checklist

Before running integration tests, ensure:

- [ ] Docker is running and accessible
- [ ] At least 4GB RAM is available
- [ ] Ports 8080-8084, 8000, 6379 are free
- [ ] Java 17+ is installed
- [ ] Maven is installed or mvnw is available
- [ ] AWS credentials are configured (if using real AWS)

## 🆘 Getting Help

### 1. Check Logs
```bash
# Application logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f

# Test logs
mvn test -X
```

### 2. Enable Debug Logging
```yaml
logging:
  level:
    root: DEBUG
    com.flightbooking: DEBUG
    org.springframework: DEBUG
```

### 3. Common Commands
```bash
# Clean and rebuild
mvn clean install

# Run specific test
mvn test -Dtest=ClassName#methodName

# Skip tests
mvn install -DskipTests

# Debug mode
mvn test -X
``` 