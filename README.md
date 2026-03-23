# Personal-website-using-Springboot

This repository contains the source code for my personal website. It also serves as a playground to experiment with Spring Boot, REST APIs, and general web development concepts.

In the first sprint, I focused on building the initial version of the website, including personal information, ideas for future projects, and links to projects I have already developed. This phase allowed me to reinforce my understanding of Spring Boot fundamentals.

Since I had previous experience with Maven, Tomcat, and related technologies from college, the learning curve was smooth, allowing me to focus more on design decisions and overall architecture rather than tooling.

Throughout the development process, I have used AI-assisted tools (such as Codex) to validate best practices and keep the architecture clean and maintainable. Writing clear, well-structured code is something I care deeply about, as it is essential for becoming a solid and reliable software engineer.

The second functionality that this project has is a Watch Analysis website.

## Main Goals

This project currently has two main goals:

1. Serve as my personal website and presentation space.
2. Provide a watch analysis area with backend logic, data processing, and ranking features.

## Project Structure

The application is built with Spring Boot and follows a simple layered structure:

- `controller`: REST endpoints and request handling.
- `service`: business logic and ranking calculations.
- `scraper`: data collection and parsing logic.
- `model`: DTOs and domain objects used across the application.
- `data`: helper classes used for catalog ingestion and brand/model support.

This separation keeps responsibilities clear and makes the project easier to extend.

## Current Features

### Personal Website

The website includes:

- A personal presentation section.
- Space for portfolio-style project references.
- A base structure to continue adding pages and experiments.
- A Spring Boot setup that can be extended with new views and endpoints.

### Watch Analysis Area

The watch analysis part of the project includes:

- Snapshot generation for a specific watch.
- Summary metrics such as opening, closing, minimum, and maximum price.
- Detail extraction for watch-specific attributes like brand, model, caliber, material, movement, and water resistance.
- Bundle analysis for multiple watches in a single request.
- Ranking calculation over a previously collected bundle.
- Brand, model, and reference navigation endpoints.
- A scraper component that collects and normalizes the data needed by the application.

## REST API

The project exposes a REST API under:

`/api/v1/watch-analytics`

### Main Endpoints

- `GET /api/v1/watch-analytics/{brand}/{model}/{ref}`
  - Returns a full snapshot for a single watch.

- `POST /api/v1/watch-analytics/bundle`
  - Accepts a list of watches and returns a list of snapshots.

- `POST /api/v1/watch-analytics/bundle/ranking`
  - Accepts a list of snapshots and returns ranked financial results.

- `GET /api/v1/watch-analytics/brands`
  - Returns the available brands handled by the application.

- `GET /api/v1/watch-analytics/brands/{brand}/models`
  - Returns the models available for a brand.

- `GET /api/v1/watch-analytics/brands/{brand}/models/{model}/references`
  - Returns the references available for a given brand and model.

## Internal Components

Some of the main classes and responsibilities are:

- `WatchAnalyticsController`
  - Exposes the REST API and coordinates requests.

- `WatchAnalyticsScraper`
  - Builds the watch snapshot data, parses structured information, and extracts time-series values used internally by the application.

- `FinancialRankingService`
  - Computes ranking results from a collection of watch snapshots.

- `BrandIngester`
  - Manages the brand catalog used by the API.

- `WatchAnalyticsSnapshot`
  - Main model returned by the analysis endpoint, including details and summary sections.

## Technical Notes

This project uses:

- Spring Boot for application structure and dependency injection.
- Maven for dependency management and build lifecycle.
- REST controllers for backend communication.
- Jsoup and Playwright for document parsing and dynamic page handling.
- Java 17 as the language level.

## Why This Project Matters

This repository is not only a personal website but also an evolving engineering exercise. It allows me to practice:

- Backend architecture with Spring Boot.
- Clean controller/service separation.
- API design and DTO modeling.
- Data parsing and transformation.
- Building maintainable code with room for future growth.

## Future Improvements

Some areas I want to keep improving are:

- Better frontend presentation and styling.
- More robust error handling for API endpoints.
- Test coverage for services and scraper logic.
- Additional ranking and analytics metrics.
- Improved caching and performance for repeated queries.

## Running the Project

Typical commands:

```bash
mvn clean compile
mvn spring-boot:run
```
