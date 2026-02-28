# Financial Engineering: Trading & Risk Management System

A minimal simulation trading and risk management system designed to demonstrate core concepts like order matching, real-time risk calculations, and event-driven architecture.

This v1 implements an in-memory approach to the architecture, using interfaces designed to be extensible to distributed systems (Kafka, ClickHouse, Hazelcast) in future iterations.

## Architecture

1. **Trading System**: A core matching engine utilizing price-time priority limit order books. Processes market and limit orders and emits trade execution events.
2. **Risk System**: Listens to trade events and market data updates to calculate real-time PnL (realized/unrealized) and simple Greeks/sensitivities (Delta, DV01) across different asset classes (Equities, FX).
3. **Simulation Data Engine**: Automatically simulates order flows and random market price walks to keep the system active.
4. **Backend API**: A lightweight Javalin API that serves trading metrics and accepts manual order entry.
5. **Frontend Dashboard**: A React UI to visualize the live order book, recent trades, and live risk metrics.

## Tech Stack

- **Backend**: Java 17+, Maven, Guice (Dependency Injection), Javalin (Lightweight Web API), JUnit 5
- **Frontend**: React, Vite
- **Storage & Messaging (v1)**: `ConcurrentHashMap`, `CopyOnWriteArrayList`, and `BlockingQueue` (In-memory implementations of extensible interfaces)

## Running the Application

### 1. Start the Backend

The backend will start the API server on `http://localhost:8080` and immediately begin simulating market data and trades.

```bash
# Build the backend and run tests
mvn clean package

# Run the system
mvn exec:java -Dexec.mainClass="me.diepdao.Main"
```
*(Alternatively, you can run the generated fat jar if configured, or run via your IDE by executing `me.diepdao.Main`)*

### 2. Start the Frontend Dashboard

Open a new terminal window and start the Vite development server.

```bash
cd frontend
npm install
npm run dev &
```

Open your browser to the URL provided by Vite (typically `http://localhost:5173`).

## Usage

- **Instruments Dropdown**: Switch between different simulated instruments (e.g., AAPL, TSLA, EUR/USD).
- **Order Book**: See the top 5 levels of the bid/ask spread dynamically updating as simulated orders arrive.
- **Recent Trades**: See the blotter of executed trades.
- **New Order Form**: Manually submit BUY or SELL limit/market orders and watch them interact with the simulated book.
- **Risk Dashboard**: See live MTM (Mark-to-Market), Unrealized PnL, Delta, and DV01 updating in real-time as prices fluctuate and positions change.

## Extensibility

The system heavily utilizes interfaces (`TradeRepository`, `RiskRepository`, `EventPublisher`, `EventSubscriber`). To upgrade the system to a production-ready distributed setup:
1. Implement `KafkaEventBus` to replace `InMemoryEventBus`.
2. Implement `ClickHouseTradeRepository` to replace `InMemoryTradeRepository`.
3. Update `TradingSystemModule` (Guice) to bind the new implementations.

## Docker Deployment

To run the entire system using Docker:

```bash
docker-compose up --build
```

The frontend will be available at `http://localhost:80` and the backend API at `http://localhost:8080/api/`.

## CI/CD and Cloud Deployment

This repository includes a GitHub Actions workflow (`.github/workflows/deploy.yml`) that automatically builds the Java and React applications and publishes Docker images to GitHub Container Registry (GHCR) upon merging to `main`.

A `render.yaml` file is also included for easy Infrastructure-as-Code deployment to **Render** (a popular PaaS).
