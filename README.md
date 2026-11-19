# Toll Plaza Locator API

A Spring Boot REST API that identifies toll plazas located along a road route between two Indian PIN codes. The application uses geographical coordinates, OpenRouteService for road-route calculation, and PostgreSQL to store toll plaza information.

## Features

- Accepts source and destination Indian PIN codes.
- Returns toll plazas ordered by [their distance from the source.

## Live Deployment

**Base URL:** [](https://tollplazas.onrender.com)

**API Endpoint:**

```http
POST ](https://tollplazas.onrender.com/api/v1/toll-plaza)
```

**Example Request:**

```json
{
  "sourcePincode": "110001",
  "destinationPincode": "560001"
}
```

The API is deployed on Render and uses PostgreSQL for persistent data storage.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Backend framework |
| Spring Data JPA | Database operations |
| PostgreSQL | Relational database |
| OpenRouteService | Road route calculation |
| Maven | Dependency management and build |
| Docker | Containerization and deployment |
| Render | Cloud hosting |

## Project Structure

```text
TollPlazaLocator/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tollplazas/backend/
│   │   │       ├── controller/
│   │   │       ├── services/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       └── config/
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── data/
│   │           └── TollPlaza.csv
│   │
│   └── test/
|   |     ├── controller
│   │     |── sevices
│   │     
│
├── Dockerfile
├── pom.xml
├── mvnw
└── README.md
```

*Note: The package and folder names above describe the intended organization. Adjust them if your actual project structure differs.*

## Prerequisites

Before running the application locally, install:

1. Java 21 or later.
2. PostgreSQL.
3. Maven (optional if the project includes the Maven Wrapper).
4. Postman.
5. An OpenRouteService API key.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Anjali22-07/TollPlazas
```

Navigate into the project directory:

```bash
cd TollPlazaLocator
```

### 2. Configure PostgreSQL

Create a PostgreSQL database:

```sql
CREATE DATABASE "TollPlazaLocater";
```

Make sure the database name matches the configuration in your application.

### 3. Configure environment variables

The application requires database credentials and an OpenRouteService API key.

**Windows PowerShell:**

```powershell
$env:DB_USERNAME="your_postgres_username"
$env:DB_PASSWORD="your_postgres_password"
$env:ORS_API_KEY="your_openrouteservice_api_key"
```

**Linux/macOS:**

```bash
export DB_USERNAME="your_postgres_username"
export DB_PASSWORD="your_postgres_password"
export ORS_API_KEY="your_openrouteservice_api_key"
```

Do not commit API keys, database passwords, or other secrets to GitHub.

### 4. Configure the database connection

The application uses the following database configuration in `application.yaml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/TollPlazaLocater
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

ors:
  api:
    key: ${ORS_API_KEY}
```

### 5. Run the application

Using Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Using Maven Wrapper on Linux/macOS:

```bash
./mvnw spring-boot:run
```

Alternatively:

```bash
mvn spring-boot:run
```

The application should start at:

```text
http://localhost:8080
```

## Toll Plaza Data Import

The application uses a CSV file containing toll plaza geographical information.

**CSV location:**

```text
src/main/resources/data/TollPlaza.csv
```

**Expected CSV columns:**

```csv
longitude,latitude,toll_name,geo_state
```

On application startup, the CSV importer reads the file and saves the records to PostgreSQL if the toll plaza table is empty.

To verify the imported data, execute:

```sql
SELECT * FROM toll_plaza;
```

## API Documentation

### Find Toll Plazas Between Two PIN Codes

**Endpoint**

```http
POST /api/v1/toll-plazas
```

**Local URL**

```text
http://localhost:8080/api/v1/toll-plazas
```

**Request Headers**

```http
Content-Type: application/json
```

**Request Body**

```json
{
  "sourcePincode": "110001",
  "destinationPincode": "560001"
}
```

**Example Successful Response**

```json
{
  "route": {
    "sourcePincode": "110001",
    "destinationPincode": "560001",
    "distanceInKm": 2057.32
  },
  "tollPlazas": [
    {
      "name": "Example Toll Plaza",
      "latitude": 28.7041,
      "longitude": 77.1025,
      "distanceFromSource": 200.45
    }
  ]
}
```

*The response values above are illustrative. Actual results depend on the selected PIN codes, route, and toll plaza dataset.*

### Error Handling

The API is designed to handle invalid requests and unsuccessful route searches.

Examples of error scenarios:

- Invalid or unsupported PIN codes.
- Source and destination PIN codes are identical.
- No toll plazas found near the calculated route.
- External routing service errors.
- Database or application errors.

The actual HTTP status codes and error response bodies are determined by the application's exception handling implementation.

## How the Route Matching Works

The application follows these steps:

1. Receives source and destination PIN codes.
2. Resolves the PIN codes into geographical coordinates.
3. Sends the coordinates to OpenRouteService.
4. Receives the road route as a sequence of geographical coordinates.
5. Checks the distance between each toll plaza and the route segments.
6. Includes toll plazas that fall within the configured proximity threshold.
7. Calculates their approximate distance along the route from the source.
8. Sorts the results by distance from the source.
9. Returns the route details and matching toll plazas.

The proximity threshold is configurable through the application's `MAX_DISTANCE_FROM_ROUTE_KM` setting.

## Testing with Postman

1. Start the Spring Boot application.
2. Open Postman.
3. Select the `POST` method.
4. Enter:

```text
http://localhost:8080/api/v1/toll-plazas
```

5. Select **Body → raw → JSON**.
6. Enter:

```json
{
  "sourcePincode": "110001",
  "destinationPincode": "560001"
}
```

7. Click **Send**.
8. Verify the route distance and returned toll plaza details.

## Running Tests

Execute the following command to run the project's JUnit tests:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

To build the application:

```bash
./mvnw clean package
```

## Deployment on Render

The application can be deployed as a Docker-based Web Service on Render.

### 1. Prepare the repository

Make sure the following files are committed and pushed to GitHub:

- Spring Boot source code.
- `pom.xml`.
- `Dockerfile`.
- `src/main/resources/data/TollPlaza.csv`.
- Maven Wrapper files, if included.

Never push credentials or API keys.

### 2. Create a Render PostgreSQL database

1. Sign in to [Render](https://render.com/).
2. Open the Dashboard.
3. Select **New → PostgreSQL**.
4. Create a database.
5. Choose a region.
6. Copy the database's internal connection details for your Web Service.

Keep the database and Web Service in the same Render region when possible.

### 3. Add a Dockerfile

Create a file named `Dockerfile` in the repository root:

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
```

This Dockerfile builds the Spring Boot application and then runs the generated JAR using a smaller Java runtime image.

If your project does not contain the `.mvn` directory or Maven Wrapper, adjust the Dockerfile accordingly.

### 4. Create a Render Web Service

1. Open the Render Dashboard.
2. Select **New → Web Service**.
3. Connect your GitHub repository.
4. Select the repository containing the Dockerfile.
5. Choose **Docker** as the runtime.
6. Select your preferred region and compute plan.
7. Create the service.

Render will build the Docker image and deploy the application.

### 5. Configure environment variables on Render

Open your Web Service → **Environment** and add:

| Key | Value |
|---|---|
| `ORS_API_KEY` | Your OpenRouteService API key |
| `SPRING_DATASOURCE_URL` | JDBC URL of your Neon PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |

For the database URL, use the Render database's internal hostname, port, and database name in JDBC format:

```text
jdbc:postgresql://HOST:PORT/DATABASE_NAME
```

Use the internal database hostname rather than `localhost`.

Spring Boot's `SPRING_DATASOURCE_*` environment variables override the corresponding datasource properties in `application.yaml`.

Save the variables and redeploy the service.

### 6. Verify the deployment

After the deployment succeeds, Render will provide a public URL similar to:

```text
https://your-service-name.onrender.com
```

Test the API using Postman:

```http
POST https://your-service-name.onrender.com/api/v1/toll-plazas
```

Use the same JSON request body shown in the API documentation.

Check the Render deployment logs if the application fails to start or cannot connect to PostgreSQL.

## Important Notes

- Keep API keys and database credentials in environment variables.
- The route calculation depends on the availability and usage limits of OpenRouteService.
- Toll plaza results depend on the accuracy and completeness of the CSV dataset.
- The application requires a reachable PostgreSQL database.
- The first deployment should be tested with a known source and destination PIN code before sharing the public API URL.

## Author

**Anjali Singh**

Backend Developer | Java | Spring Boot | REST APIs | PostgreSQL
