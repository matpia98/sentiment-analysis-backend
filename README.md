# Sentiment Analysis Spring Boot Application

This Spring Boot application provides sentiment analysis functionality using Anthropic's Claude 3.5 Haiku API. It allows users to submit text content (reviews, comments, opinions) and automatically analyzes the emotional tone (positive, negative, neutral).

## Features

- REST API for sentiment analysis
- Integration with Anthropic Claude 3.5 Haiku API
- Persistence of sentiment analysis results
- Detailed analysis with confidence scores
- Support for different data sources (reviews, comments, etc.)

## Technologies

- Java 21
- Spring Boot 3.4.3
- Spring Data JPA
- PostgreSQL Database (with Docker support)
- Anthropic Claude API

## Getting Started

### Prerequisites

- Java 21 JDK or later
- Maven 3.6 or later
- Docker and Docker Compose (for PostgreSQL and pgAdmin)
- Anthropic API Key

### Database Setup with Docker

1. Start the PostgreSQL database and pgAdmin using Docker Compose:
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- pgAdmin web interface on port 5050

2. Access pgAdmin at http://localhost:5050
   - Email: admin@admin.com
   - Password: admin

3. Add a new server in pgAdmin:
   - Name: Sentiment DB
   - Host: postgres
   - Port: 5432
   - Username: postgres
   - Password: postgres
   - Database: sentiment_db

### Configuration

1. Open `src/main/resources/application.properties`
2. Replace the placeholder API key with your actual Anthropic API key:
   ```
   anthropic.api.key=your-anthropic-api-key-here
   ```

### Running the Application

```bash
mvn spring:boot run
```

Or run the application from your IDE by executing the main class:
```
mat.pia.sentiment.SentimentBackendApplication
```

The application will start on port 8080.

## API Endpoints

### Analyze Text Sentiment

**Endpoint:** `POST /api/sentiment/analyze`

**Request Body:**
```json
{
  "text": "I absolutely love this product! It's amazing and exceeded all my expectations.",
  "source": "product-review"
}
```

**Response:**
```json
{
  "text": "I absolutely love this product! It's amazing and exceeded all my expectations.",
  "sentiment": "POSITIVE",
  "confidence": 0.95,
  "analysis": "The text contains strong positive sentiment with phrases like 'absolutely love' and 'amazing', indicating high satisfaction.",
  "timestamp": "2025-03-15T14:30:45.123"
}
```

### Get Sentiment Analysis History

**Endpoint:** `GET /api/sentiment/history`

**Response:** List of all sentiment analysis entities.

### Get Sentiment Analysis by ID

**Endpoint:** `GET /api/sentiment/history/{id}`

**Response:** Specific sentiment analysis entity.

### Get Sentiment Analyses by Type

**Endpoint:** `GET /api/sentiment/history/type/{type}`

Where `{type}` is one of: `POSITIVE`, `NEGATIVE`, `NEUTRAL`

**Response:** List of sentiment analyses with the specified sentiment type.

## Database Management

### PostgreSQL

The application uses PostgreSQL as its database. Connection details:
- JDBC URL: `jdbc:postgresql://localhost:5432/sentiment_db`
- Username: postgres
- Password: postgres

You can manage the database using pgAdmin at http://localhost:5050

### H2 Database (for tests)

H2 is still available for tests. For local development, the application now uses PostgreSQL.

## Switching Between AI Providers

The application supports multiple AI providers for sentiment analysis. To switch providers, modify the `sentiment.api.provider` property in `application.properties`:

- For Anthropic Claude: `sentiment.api.provider=anthropic`
- For Hugging Face: `sentiment.api.provider=huggingface`
- For OpenAI: `sentiment.api.provider=openai`

Note: For providers other than Anthropic, you'll need to set up the corresponding API keys.
