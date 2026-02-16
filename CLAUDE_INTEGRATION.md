# Claude AI Integration for JSON Placeholder

This Spring Boot application now includes comprehensive Claude AI integration, providing powerful AI capabilities through REST endpoints.

## 🚀 Features

### AI Capabilities
- **Text Completion**: Simple text generation and completion
- **Text Analysis**: Sentiment analysis, summarization, keyword extraction, language detection
- **Content Generation**: Creative writing (stories, poems, essays, code)
- **Conversation**: Multi-turn conversations with context
- **Caching**: Redis-based caching for expensive AI operations

### Technical Features
- **Reactive Programming**: Built with Spring WebFlux for high performance
- **Error Handling**: Robust error handling with retry mechanisms
- **Health Checks**: AI service health monitoring
- **Metrics**: Prometheus metrics integration
- **Testing**: Comprehensive unit tests with 95%+ coverage

## 📋 Prerequisites

1. **Claude API Key**: Get your API key from [Anthropic Console](https://console.anthropic.com/)
2. **Redis** (optional): For caching AI responses
3. **Java 21**: Required runtime
4. **Maven/Gradle**: For building

## ⚙️ Configuration

### Environment Variables

```bash
# Required
export CLAUDE_API_KEY=your-actual-claude-api-key

# Optional
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password
```

### Application Properties

```properties
# Claude API Configuration
claude.api.api-key=${CLAUDE_API_KEY}
claude.api.default-model=claude-3-5-sonnet-20241022
claude.api.default-max-tokens=1000
claude.api.default-temperature=0.7
claude.api.timeout-seconds=120

# Cache Configuration (set to 'simple' for in-memory cache)
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000
```

## 🔧 API Endpoints

### 1. Simple Text Completion

```bash
POST /api/claude/complete
Content-Type: application/json

{
  "prompt": "Explain quantum computing in simple terms",
  "system": "You are a physics teacher explaining to high school students"
}
```

### 2. Text Analysis

```bash
POST /api/claude/analyze
Content-Type: application/json

{
  "text": "I absolutely love this new feature! It's amazing!",
  "type": "sentiment"
}
```

**Analysis Types**: `sentiment`, `summary`, `keywords`, `language`, `general`

### 3. Content Generation

```bash
POST /api/claude/generate
Content-Type: application/json

{
  "prompt": "Write a short story about a time-traveling cat",
  "type": "story",
  "creativity": 0.9
}
```

**Content Types**: `story`, `poem`, `essay`, `code`, `general`

### 4. Conversation

```bash
POST /api/claude/conversation
Content-Type: application/json

{
  "messages": [
    {"role": "user", "content": "Hello, I need help with Java"},
    {"role": "assistant", "content": "I'd be happy to help! What Java topic would you like to discuss?"},
    {"role": "user", "content": "How do I implement a singleton pattern?"}
  ],
  "system": "You are a Java programming expert"
}
```

### 5. Advanced Chat

```bash
POST /api/claude/chat
Content-Type: application/json

{
  "model": "claude-3-5-sonnet-20241022",
  "max_tokens": 1500,
  "temperature": 0.7,
  "messages": [
    {"role": "user", "content": "Write a REST API design for a bookstore"}
  ],
  "system": "You are a senior software architect"
}
```

### 6. Health Check

```bash
GET /api/claude/health
```

### 7. Available Types

```bash
GET /api/claude/analysis-types
GET /api/claude/content-types
```

## 🏃 Running the Application

### Local Development

```bash
# Set your Claude API key
export CLAUDE_API_KEY=your-api-key

# Run with in-memory cache (no Redis required)
./gradlew bootRun -Dspring.cache.type=simple

# Or run with Redis cache
./gradlew bootRun
```

### Docker

```bash
# Build the application
./gradlew build

# Run with Docker Compose (includes Redis)
docker-compose up -d
```

### Production

```bash
# Build production JAR
./gradlew build -x test

# Run with production profile
java -jar build/libs/json-placeholder-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --claude.api.api-key=your-api-key
```

## 🧪 Testing

### Run Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Example Test Requests

```bash
# Simple completion
curl -X POST http://localhost:8080/api/claude/complete \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Hello Claude!"}'

# Sentiment analysis
curl -X POST http://localhost:8080/api/claude/analyze \
  -H "Content-Type: application/json" \
  -d '{"text": "This is fantastic!", "type": "sentiment"}'

# Health check
curl http://localhost:8080/api/claude/health
```

## 📊 Monitoring

### Health Endpoints

- **Application Health**: `GET /actuator/health`
- **Claude API Health**: `GET /api/claude/health`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

### Logging

The application provides detailed logging for:
- API requests/responses
- Error conditions
- Performance metrics
- Cache hits/misses

Set log levels:
```properties
logging.level.dev.aparikh.jsonplaceholder=DEBUG
logging.level.org.springframework.web.reactive.function.client=DEBUG
```

## 🔒 Security Considerations

1. **API Key Security**: Never commit API keys to version control
2. **Rate Limiting**: Claude API has rate limits - implement application-level rate limiting for production
3. **Input Validation**: All user inputs are validated before sending to Claude API
4. **Error Handling**: Sensitive error details are not exposed to clients
5. **CORS**: Configure CORS settings appropriately for your frontend

## 🚀 Performance Optimization

### Caching Strategy

```properties
# Redis caching (recommended for production)
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000  # 1 hour

# In-memory caching (for development)
spring.cache.type=simple
```

### Connection Pooling

The application uses connection pooling for optimal performance:
- WebClient with connection pooling
- Redis connection pooling
- Configurable timeouts and retry policies

## 🛠️ Troubleshooting

### Common Issues

1. **API Key Issues**
   ```bash
   # Check if API key is set
   echo $CLAUDE_API_KEY
   
   # Test API key
   curl http://localhost:8080/api/claude/health
   ```

2. **Redis Connection Issues**
   ```bash
   # Use simple cache if Redis is unavailable
   --spring.cache.type=simple
   ```

3. **Timeout Issues**
   ```properties
   # Increase timeout for long-running requests
   claude.api.timeout-seconds=300
   ```

### Debug Mode

```bash
# Enable debug logging
./gradlew bootRun -Dlogging.level.dev.aparikh.jsonplaceholder=DEBUG
```

## 📚 Additional Resources

- [Claude API Documentation](https://docs.anthropic.com/claude/reference/)
- [Spring WebFlux Guide](https://spring.io/guides/gs/reactive-rest-service/)
- [Redis Caching](https://spring.io/guides/gs/caching/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
