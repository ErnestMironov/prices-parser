# Prices Parser

A Spring Boot application for parsing product information (title, price, description) from web pages and storing the results in a database.

## Features

- Asynchronous web scraping using Jsoup
- RESTful API for managing parsing tasks and retrieving results
- H2 in-memory database with web console access
- Multithreading with configurable thread pools
- Product filtering, sorting, and pagination

## Requirements

- Java 21
- Maven 3.6+

## Installation and Running

1. Clone the repository:
```bash
git clone <repository-url>
cd prices-parser
```

2. Build the application:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

Or run the compiled JAR:
```bash
java -jar target/prices-parser-1.0.0.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### Parse Products

Start parsing products from provided URLs:

**POST** `/api/parse`

Request body:
```json
{
  "urls": [
    "https://example.com/product1",
    "https://example.com/product2"
  ]
}
```

Response: `202 Accepted` - parsing started asynchronously

### Get Parsing Results

Retrieve parsed products with pagination and sorting:

**GET** `/api/results`

Query parameters:
- `page` - page number (default: 0)
- `size` - items per page (default: 10)
- `sortBy` - field to sort by (default: "id")
- `sortDir` - sort direction: "asc" or "desc" (default: "asc")

Example:
```bash
curl "http://localhost:8080/api/results?page=0&size=20&sortBy=price&sortDir=desc"
```

Response:
```json
{
  "content": [
    {
      "id": 1,
      "url": "https://example.com/product",
      "title": "Product Name",
      "price": 99.99,
      "description": "Product description",
      "parsedAt": "2025-11-23T10:30:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### Get Products with Filters

**GET** `/api/products`

Query parameters:
- `page` - page number
- `size` - items per page
- `sortBy` - field to sort by
- `sortDir` - sort direction
- `minPrice` - minimum price filter
- `maxPrice` - maximum price filter
- `titleFilter` - search by title

Example:
```bash
curl "http://localhost:8080/api/products?minPrice=50&maxPrice=200&titleFilter=phone"
```

### Get Product Count

**GET** `/api/products/count`

Returns total number of products in database.

### Get Expensive Products

**GET** `/api/products/expensive?threshold=100`

Returns products with price above threshold.

### Get Products Sorted by Price

**GET** `/api/products/sorted-by-price`

Returns all products sorted by price in descending order.

## Database Access

The application uses H2 in-memory database. You can access the database console in your browser:

**URL:** `http://localhost:8080/h2-console`

**Connection settings:**
- JDBC URL: `jdbc:h2:mem:pricesdb`
- Username: `sa`
- Password: *(leave empty)*

Click "Connect" to access the database and run SQL queries directly in the browser.

## Configuration

Application settings can be modified in `src/main/resources/application.properties`:

- Server port: `server.port=8080`
- Thread pool size: `parser.thread-pool.core-size=5`
- Database settings: H2 configuration
- Logging levels

## Project Structure

```
src/main/java/com/pricesparser/
├── client/          # HTTP clients for external requests
├── config/          # Spring configuration classes
├── controller/      # REST API controllers
├── dto/             # Data transfer objects
├── model/           # JPA entities
├── parser/          # Web scraping logic
├── queue/           # URL queue management
├── repository/      # JPA repositories
├── service/         # Business logic services
├── task/            # Async task definitions
└── util/            # Utility classes
```

## Testing

Run tests:
```bash
mvn test
```

## Technologies

- Spring Boot 3.2.0
- Spring Data JPA
- Spring WebFlux
- H2 Database
- Jsoup (HTML parser)
- OpenFeign
- Maven
