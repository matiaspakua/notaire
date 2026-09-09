---
name: hexagonal-architecture
description: Design, implement, and refactor Ports & Adapters systems with clear domain boundaries, dependency inversion, and testable use-case orchestration across TypeScript, Java, Kotlin, and Go services. Use when introducing or refactoring toward Ports and Adapters, or when domain logic has become entangled with I/O.
metadata:
  origin: ECC
---

# Hexagonal Architecture

Hexagonal architecture (Ports and Adapters) keeps business logic independent from frameworks, transport, and persistence details. The core app depends on abstract ports, and adapters implement those ports at the edges. [web:1][web:7][web:14]

## When to Use

- Building new features where long-term maintainability and testability matter. [web:1]
- Refactoring layered or framework-heavy code where domain logic is mixed with I/O concerns. [web:4][web:8]
- Supporting multiple interfaces for the same use case (HTTP, CLI, queue workers, cron jobs). [web:10]
- Replacing infrastructure (database, external APIs, message bus) without rewriting business rules. [web:3][web:6]
- Teams adopting DDD principles without requiring full ubiquitous language or bounded contexts. [web:5][web:8]

Use this skill when the request involves boundaries, domain-centric design, refactoring tightly coupled services, or decoupling application logic from specific libraries.

## Core Concepts

- **Domain model**: Business rules and entities/value objects. No framework imports. [web:1][web:14]
- **Use cases (application layer)**: Orchestrate domain behavior and workflow steps. [web:1][web:7]
- **Inbound ports (driving/primary ports)**: Contracts describing what the application can do (commands/queries/use-case interfaces). [web:3][web:7][web:10]
- **Outbound ports (driven/secondary ports)**: Contracts for dependencies the application needs (repositories, gateways, event publishers, clock, UUID, etc.). [web:3][web:7]
- **Adapters**: Infrastructure and delivery implementations of ports (HTTP controllers, DB repositories, queue consumers, SDK wrappers). [web:1][web:14]
- **Composition root**: Single wiring location where concrete adapters are bound to use cases. [web:1][web:6][web:11]

Outbound port interfaces usually live in the application layer (or in domain only when the abstraction is truly domain-level), while infrastructure adapters implement them. [web:2][web:5]

Dependency direction is always inward:

- Adapters -> application/domain
- Application -> port interfaces (inbound/outbound contracts)
- Domain -> domain-only abstractions (no framework or infrastructure dependencies)
- Domain -> nothing external [web:14]

## How It Works

### Step 1: Model a use case boundary

Define a single use case with a clear input and output DTO. Keep transport details (Express `req`, GraphQL `context`, job payload wrappers) outside this boundary. [web:1][web:8]

### Step 2: Define outbound ports first

Identify every side effect as a port:

- persistence (`UserRepositoryPort`)
- external calls (`BillingGatewayPort`)
- cross-cutting (`LoggerPort`, `ClockPort`) [web:1][web:6]

Ports should model capabilities, not technologies. Avoid over-porting by not creating a port for every external dependency, especially those that will never have a second implementation. [web:9]

### Step 3: Implement the use case with pure orchestration

Use case class/function receives ports via constructor/arguments. It validates application-level invariants, coordinates domain rules, and returns plain data structures. [web:1][web:7]

### Step 4: Build adapters at the edge

- Inbound adapter converts protocol input to use-case input.
- Outbound adapter maps app contracts to concrete APIs/ORM/query builders.
- Mapping stays in adapters, not inside use cases. [web:1][web:3][web:15]

### Step 5: Wire everything in a composition root

Instantiate adapters, then inject them into use cases. Keep this wiring centralized to avoid hidden service-locator behavior. [web:1][web:6][web:11]

### Step 6: Test per boundary

- Unit test use cases with fake ports.
- Integration test adapters with real infra dependencies.
- E2E test user-facing flows through inbound adapters. [web:1][web:6][web:15]

## Architecture Diagram

```mermaid
flowchart LR
  Client["Client (HTTP/CLI/Worker)"] --> InboundAdapter["Inbound Adapter"]
  InboundAdapter -->|"calls"| UseCase["UseCase (Application Layer)"]
  UseCase -->|"uses"| OutboundPort["OutboundPort (Interface)"]
  OutboundAdapter["Outbound Adapter"] -->|"implements"| OutboundPort
  OutboundAdapter --> ExternalSystem["DB/API/Queue"]
  UseCase --> DomainModel["DomainModel"]
```

## Suggested Module Layout

Use feature-first organization with explicit boundaries:

```text
src/
  features/
    orders/
      domain/
        Order.ts
        OrderPolicy.ts
      application/
        ports/
          inbound/
            CreateOrder.ts
          outbound/
            OrderRepositoryPort.ts
            PaymentGatewayPort.ts
        use-cases/
          CreateOrderUseCase.ts
      adapters/
        inbound/
          http/
            createOrderRoute.ts
        outbound/
          postgres/
            PostgresOrderRepository.ts
          stripe/
            StripePaymentGateway.ts
      composition/
        ordersContainer.ts
```

## TypeScript Example

### Port definitions

```typescript
export interface OrderRepositoryPort {
  save(order: Order): Promise<void>;
  findById(orderId: string): Promise<Order | null>;
}

export interface PaymentGatewayPort {
  authorize(input: { orderId: string; amountCents: number }): Promise<{ authorizationId: string }>;
}
```

### Use case

```typescript
type CreateOrderInput = {
  orderId: string;
  amountCents: number;
};

type CreateOrderOutput = {
  orderId: string;
  authorizationId: string;
};

export class CreateOrderUseCase {
  constructor(
    private readonly orderRepository: OrderRepositoryPort,
    private readonly paymentGateway: PaymentGatewayPort
  ) {}

  async execute(input: CreateOrderInput): Promise<CreateOrderOutput> {
    const order = Order.create({ id: input.orderId, amountCents: input.amountCents });

    const auth = await this.paymentGateway.authorize({
      orderId: order.id,
      amountCents: order.amountCents,
    });

    // markAuthorized returns a new Order instance; it does not mutate in place.
    const authorizedOrder = order.markAuthorized(auth.authorizationId);
    await this.orderRepository.save(authorizedOrder);

    return {
      orderId: order.id,
      authorizationId: auth.authorizationId,
    };
  }
}
```

### Outbound adapter

```typescript
export class PostgresOrderRepository implements OrderRepositoryPort {
  constructor(private readonly db: SqlClient) {}

  async save(order: Order): Promise<void> {
    await this.db.query(
      "insert into orders (id, amount_cents, status, authorization_id) values ($1, $2, $3, $4)",
      [order.id, order.amountCents, order.status, order.authorizationId]
    );
  }

  async findById(orderId: string): Promise<Order | null> {
    const row = await this.db.oneOrNone("select * from orders where id = $1", [orderId]);
    return row ? Order.rehydrate(row) : null;
  }
}
```

### Composition root

```typescript
export const buildCreateOrderUseCase = (deps: { db: SqlClient; stripe: StripeClient }) => {
  const orderRepository = new PostgresOrderRepository(deps.db);
  const paymentGateway = new StripePaymentGateway(deps.stripe);

  return new CreateOrderUseCase(orderRepository, paymentGateway);
};
```

## Multi-Language Mapping

Use the same boundary rules across ecosystems; only syntax and wiring style change. [web:6][web:8][web:11]

- **TypeScript/JavaScript**
  - Ports: `application/ports/*` as interfaces/types.
  - Use cases: classes/functions with constructor/argument injection.
  - Adapters: `adapters/inbound/*`, `adapters/outbound/*`.
  - Composition: explicit factory/container module (no hidden globals). [web:5]

- **Java**
  - Packages: `domain`, `application.port.in`, `application.port.out`, `application.usecase`, `adapter.in`, `adapter.out`.
  - Ports: interfaces in `application.port.*`.
  - Use cases: plain classes (Spring `@Service` is optional, not required).
  - Composition: Spring config or manual wiring class; keep wiring out of domain/use-case classes. [web:8][web:11][web:13]

- **Kotlin**
  - Modules/packages mirror the Java split (`domain`, `application.port`, `application.usecase`, `adapter`).
  - Ports: Kotlin interfaces.
  - Use cases: classes with constructor injection (Koin/Dagger/Spring/manual).
  - Composition: module definitions or dedicated composition functions; avoid service locator patterns. [web:11]

- **Go**
  - Packages: `internal/<feature>/domain`, `application`, `ports`, `adapters/inbound`, `adapters/outbound`.
  - Ports: small interfaces owned by the consuming application package.
  - Use cases: structs with interface fields plus explicit `New...` constructors.
  - Composition: wire in `cmd/<app>/main.go` (or dedicated wiring package), keep constructors explicit. [web:5]

## Anti-Patterns to Avoid

- **Leaky persistence models**: Domain entities importing ORM models, web framework types, or SDK clients. [web:4][web:15]
- **Transport leakage**: Use cases reading directly from `req`, `res`, or queue metadata. [web:1]
- **Direct infrastructure returns**: Returning database rows directly from use cases without domain/application mapping. [web:4]
- **Adapter-to-adapter coupling**: Letting adapters call each other directly instead of flowing through use-case ports. [web:14]
- **Hidden wiring**: Spreading dependency wiring across many files with hidden global singletons. [web:1][web:6]
- **Over-porting**: Creating a port for every external dependency, including things that will never have a second implementation. [web:9]
- **Leaky ports**: A port speaking the language of infrastructure instead of the domain. [web:9]

## Migration Playbook

1. Pick one vertical slice (single endpoint/job) with frequent change pain. [web:1]
2. Extract a use-case boundary with explicit input/output types. [web:1][web:8]
3. Introduce outbound ports around existing infrastructure calls. [web:2][web:6]
4. Move orchestration logic from controllers/services into the use case. [web:1]
5. Keep old adapters, but make them delegate to the new use case. [web:1]
6. Add tests around the new boundary (unit + adapter integration). [web:1][web:15]
7. Repeat slice-by-slice; avoid full rewrites. [web:1]

### Refactoring Existing Systems

- **Strangler approach**: keep current endpoints, route one use case at a time through new ports/adapters. [web:1]
- **No big-bang rewrites**: migrate per feature slice and preserve behavior with characterization tests. [web:1]
- **Facade first**: wrap legacy services behind outbound ports before replacing internals. [web:1]
- **Composition freeze**: centralize wiring early so new dependencies do not leak into domain/use-case layers. [web:1]
- **Slice selection rule**: prioritize high-churn, low-blast-radius flows first. [web:1]
- **Rollback path**: keep a reversible toggle or route switch per migrated slice until production behavior is verified. [web:1]

## Testing Guidance (Same Hexagonal Boundaries)

- **Domain tests**: test entities/value objects as pure business rules (no mocks, no framework setup). [web:1][web:8][web:15]
- **Use-case unit tests**: test orchestration with fakes/stubs for outbound ports; assert business outcomes and port interactions. [web:1][web:6][web:14]
- **Outbound adapter contract tests**: define shared contract suites at port level and run them against each adapter implementation. [web:2][web:6]
- **Inbound adapter tests**: verify protocol mapping (HTTP/CLI/queue payload to use-case input and output/error mapping back to protocol). [web:1]
- **Adapter integration tests**: run against real infrastructure (DB/API/queue) for serialization, schema/query behavior, retries, and timeouts. [web:1][web:15]
- **End-to-end tests**: cover critical user journeys through inbound adapter -> use case -> outbound adapter. [web:1]
- **Refactor safety**: add characterization tests before extraction; keep them until new boundary behavior is stable and equivalent. [web:1]

## Best Practices Checklist

- Domain and use-case layers import only internal types and ports. [web:1][web:14]
- Every external dependency is represented by an outbound port. [web:3][web:6]
- Validation occurs at boundaries (inbound adapter + use-case invariants). [web:1][web:8]
- Use immutable transformations (return new values/entities instead of mutating shared state). [web:1][web:8]
- Errors are translated across boundaries (infra errors -> application/domain errors). [web:15]
- Composition root is explicit and easy to audit. [web:1][web:6]
- Use cases are testable with simple in-memory fakes for ports. [web:1][web:6][web:15]
- Refactoring starts from one vertical slice with behavior-preserving tests. [web:1]
- Language/framework specifics stay in adapters, never in domain rules. [web:3][web:14]
- Start with two adapters per port (real implementation + in-memory fake) to validate port design is implementation-agnostic. [web:6]
- Keep adapters dumb: business logic is not their job. [web:15]

## Trade-offs and When Not to Use

Hexagonal architecture introduces initial complexity and code overhead. It may feel excessive for small projects or simple CRUD applications. [web:1][web:9]

**Avoid hexagonal architecture when:**

- Building prototypes or proof-of-concepts where speed matters more than maintainability. [web:9]
- Working on simple scripts or one-off utilities. [web:9]
- The team lacks experience and the project timeline cannot accommodate the learning curve. [web:1]
- The application has no external dependencies beyond a single database and no planned interface variations. [web:9]

**Expected challenges:**

- Initial learning curve as teams adapt to ports, adapters, and dependency inversion concepts. [web:1]
- Perceived code overhead when creating multiple layers for simple features. [web:1]
- Risk of over-engineering if ports are created for every minor dependency. [web:9]