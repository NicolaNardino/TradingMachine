# TradingMachine

A mini trading system simulation in Java. It generates random market data and orders, routes the orders over FIX to a matching engine, persists the executions and rejections, and shows everything live in a desktop monitor.

The project focuses on the plumbing of a front-to-back trading flow (messaging, FIX sessions, matching, persistence, monitoring) rather than on realistic market microstructure. Its cloud-native successor is [serverless-trading-system](https://github.com/NicolaNardino/serverless-trading-system).

## Architecture

![TradingMachine architecture](https://github.com/NicolaNardino/TradingMachine/wiki/Images/TradingMachineArchitecture.jpg)

| Component | Role |
|---|---|
| **Market data feed** | Generates random bid/ask prices and sizes for a configured set of symbols and publishes them to an ActiveMQ topic at a fixed interval. |
| **Orders feed** | Generates random market, limit and stop orders and publishes them to an ActiveMQ queue at a fixed interval. |
| **Order router (FIX initiator)** | Acts as the OMS: consumes the orders queue, sends each order to the matching engine over a FIX 5.0 session, and publishes the execution reports (fills and rejections) to a topic. |
| **Trading server (FIX acceptor)** | Hosts the matching engine. It subscribes to market data and executes incoming orders against it, after a credit check backed by a MySQL stored procedure. |
| **Orders back-end store** | Subscribes to the executed-orders topic and persists every fill and rejection to both MongoDB and MySQL. |
| **Statistics service** | Periodically aggregates the stored orders and logs order execution statistics. |
| **Trade Monitor UI** | Swing desktop app subscribing to the orders and market data topics. It shows live fills, rejections and market data, the history stored in MongoDB, and order statistics. |

### Matching rules

- **Market orders** always find a price, since one is always available from market data. They fill unless they are Fill-or-Kill (FOK) and the order quantity exceeds the available bid/ask size.
- **Limit and stop orders** fill only when their limit/stop price and quantity are compatible with current market data. Limit orders can also be FOK.
- Before execution, the matching engine checks that the counterparty has enough credit.

## Trade Monitor UI

Executed and rejected orders, with statistics:

![Trade Monitor UI](https://github.com/NicolaNardino/TradingMachine/wiki/Images/TradeMonitorUI.jpg)

Market data:

![Market data UI](https://github.com/NicolaNardino/TradingMachine/wiki/Images/MarketDataUI.jpg)

## Tech stack

- Java 8, Maven (multi-module)
- [QuickFIX/J](https://www.quickfixj.org/) 1.6 (FIX 5.0) for the order router and trading server sessions
- Apache ActiveMQ 5 (JMS topics and queues)
- MongoDB and MySQL (stored procedures, connection pooling via Commons DBCP2)
- Swing for the monitor UI
- Log4j 2 / SLF4J, JUnit 4

## Project structure

```
TradingMachineParentPom.xml   Parent POM aggregating the modules below
TradingMachineUtility/        Shared model (orders, market data), DB and credit-check helpers, unit tests
TradingMachineServices/       Market data and orders feeds, orders back-end store, statistics service
TradingMachineServer/         FIX acceptor and matching engine
TradingMachineOrderRouter/    FIX initiator (OMS)
TradingMachineMonitorUI/      Swing Trade Monitor
```

## Getting started

### Prerequisites

- JDK 8 and Maven 3
- ActiveMQ broker on `tcp://localhost:61616`
- MongoDB on `localhost:27017`
- MySQL on `localhost:3306`

### Database setup

Create a `TradingMachine` MySQL database and a `TradingUser` user (password `TradingUser`, as in the default properties), then run
[`TradingMachineServices/src/main/resources/MySqlScripts.sql`](TradingMachineServices/src/main/resources/MySqlScripts.sql) to create the tables and stored procedures. MongoDB needs no setup: the collections are created on first write.

### Build

```bash
mvn -f TradingMachineParentPom.xml clean install
```

### Run

Start the components in this order, each from its module's main class (from the IDE or with `java -cp`):

1. `ServicesRunner` (TradingMachineServices): market data feed, orders feed, orders back-end store and statistics service.
2. `TradingMachineServer` (TradingMachineServer): FIX acceptor and matching engine.
3. `TradingMachineOrderRouter` (TradingMachineOrderRouter): OMS and FIX initiator.
4. `TradeMonitorUI` (TradingMachineMonitorUI): the monitor.

### Configuration

Each module reads a `.properties` file from its `src/main/resources`:

| File | Key settings |
|---|---|
| `tradingMachineServices.properties` | ActiveMQ topics/queues, MongoDB and MySQL connections, publishing periods (seconds), `allowedSymbols` |
| `tradingMachine.properties` | ActiveMQ URL, market data topic, order processing threads, MySQL credit-check pool |
| `tradingMachineFixEngine.properties` / `tradingMachineOrderRouterFixEngine.properties` | QuickFIX/J session settings for the acceptor and initiator |
| `tradeMonitor.properties` | Topics and MongoDB connection used by the UI |

## Roadmap

- Matching algorithm for stop-limit orders.
- JNDI-based topic and queue lookup instead of the ActiveMQ-specific API.

## Status

This is a learning project, last actively developed in the Java 8 era, and is no longer maintained beyond dependency bumps. The default credentials in the properties files are for local use only.
