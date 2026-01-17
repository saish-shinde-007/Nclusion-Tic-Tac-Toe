# Tic-Tac-Toe Backend API

A Spring Boot REST API for managing Tic-Tac-Toe games.

## Requirements

- **Java 17** (required)
- Maven 3.6+

## Quick Start

```bash
# Set Java 17 (if not default)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Run the application
mvn spring-boot:run

# Run tests
mvn test
```

Server starts at **http://localhost:8080**

## API Endpoints

### Players (`/api/players`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/players` | Create player |
| GET | `/api/players` | List all players |
| GET | `/api/players/{id}` | Get player |
| PUT | `/api/players/{id}` | Update player |
| DELETE | `/api/players/{id}` | Delete player |
| GET | `/api/players/{id}/stats` | Get player stats |
| GET | `/api/players/leaderboard` | Get leaderboard |
| GET | `/api/players/count` | Get player count |

### Games (`/games`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/games` | Create game |
| GET | `/games` | List all games |
| GET | `/games/{id}` | Get game |
| GET | `/games/{id}/status` | Get game status |
| GET | `/games/waiting` | Get waiting games |
| GET | `/games/stats` | Get game stats |
| POST | `/games/{id}/join` | Join game |
| POST | `/games/{id}/moves` | Make a move |
| DELETE | `/games/{id}` | Delete game |

## Quick API Examples

```bash
# Create a game
curl -s -X POST http://localhost:8080/games -H 'Content-Type: application/json' \
  -d '{"name":"Sample"}' | jq .

# Join the game
GAME_ID=<paste-from-create>
curl -s -X POST http://localhost:8080/games/$GAME_ID/join -H 'Content-Type: application/json' \
  -d '{"playerId":"<player-id>"}' | jq .

# Make a move
curl -s -X POST http://localhost:8080/games/$GAME_ID/moves -H 'Content-Type: application/json' \
  -d '{"playerId":"<player-id>","row":0,"col":0}' | jq .

# Get game status
curl -s http://localhost:8080/games/$GAME_ID/status | jq .
```

## Features

- **Rate Limiting**: 100 requests/minute per IP with `X-RateLimit-*` headers
- **Request Logging**: All requests logged with request ID and timing
- **Email Validation**: Strict regex pattern validation
- **Win Detection**: Automatic win/draw detection
- **Player Stats**: Tracks games played, wins, losses, draws

---

## AI Usage Disclosure

I used an AI coding assistant for targeted technical research and test generation during this assignment:

1. **Email Validation Regex** — Consulted AI to verify the RFC-compliant regex pattern `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` for stricter validation than Jakarta's default `@Email` (which accepts nearly anything with an `@`). This ensures proper TLD enforcement and rejects malformed addresses like `user@domain` without extension.

2. **Lombok + Java 17 Compatibility** — Used AI to troubleshoot a `java.lang.ExceptionInInitializerError` caused by Lombok's annotation processor failing with Java 25. Identified that Lombok 1.18.34 resolves the issue when paired with Java 17, and configured `JAVA_HOME` accordingly.

3. **Controller Tests** — Due to time constraints, I used AI assistance to generate the unit tests for `GameControllerTest.java` and `PlayerControllerTest.java`. The tests cover HTTP status codes, business logic validation, error handling, and edge cases. I reviewed and verified all generated tests to ensure correctness and alignment with the actual implementation behavior.

All implementation decisions—including the sliding window rate limiter (using standard `X-RateLimit-*` headers, referenced from MDN/Google), game state management, win detection logic, and API design—were made independently. The model tests (`GameTest.java`) and service tests were also written independently. I am fully prepared to discuss, debug, or extend any part of this codebase in a live session.
