# EmallSpace - Social Distributed Operation Platform

A high-concurrency social marketing platform similar to Xiaohongshu, featuring social blogging and precision marketing capabilities.

## Core Features

### 1. Social Blog
- **Post Management**: 
  - Storage: Cassandra (Optimized for write-heavy, short text).
  - ID Generation: Snowflake Algorithm.
  - Content Safety: Trie Tree sensitive word filtering.
- **Interactions**:
  - **Likes**: High-concurrency handling using **BufferTrigger** pattern (Redis + Local Buffer -> Batch Write to MySQL).
  - **Comments**: Infinite nesting support using **Path Enumeration** (MySQL).

### 2. Marketing Space
- **Flash Sale (Points Redemption)**:
  - Prevention of overselling using **Redis Atomic Decrement** + **MySQL Optimistic Locking**.
- **User Portrait**:
  - High-performance user selection using **RoaringBitmap**.
  - Supports complex boolean logic (AND, OR, NOT) on user tags.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Databases**: 
  - MySQL (User, Comments, Stock, Tags)
  - Cassandra (Posts)
  - Redis (Caching, Counters, Distributed Locks)
- **Message Queue**: Kafka (Async processing, Feed updates)
- **Utils**: RoaringBitmap, Caffeine, Lombok

## Prerequisites
Ensure the following services are running locally or configured in `application.properties`:
- MySQL (Port 3306)
- Cassandra (Port 9042)
- Redis (Port 6379)
- Kafka (Port 9092)

## Getting Started
1. Clone the repository.
2. Configure `application.properties`.
3. Run `mvn clean install`.
4. Start `EmallSpaceApplication`.
