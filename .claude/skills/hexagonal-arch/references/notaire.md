# Notaire appendix — Ports & Adapters as done here

The generic skill explains the pattern. This file fixes the conventions of this repository (ADR-030) and shows
the shape of a slice. The skeletons are illustrative, not existing code; the reference implementation that
exists today is the payment slice.

## Reference slice that exists: payment (ADR-021)

| Layer | Where |
|---|---|
| Domain | `domain/payment`: `Money`, `PaymentDetails`, `PaymentStatus`, `BudgetCharges`, `ChargeLine` |
| Inbound ports | `application/port/in/payment`: `ProcessPaymentUseCase`, `EditPaymentUseCase`, … and their command records |
| Outbound ports | `application/port/out/payment`: `PaymentRepositoryPort`, `BudgetLookupPort`, `NewPayment`, `PaymentChanges` |
| Use cases | `application/usecase/payment`: `ProcessPaymentService`, `PaymentQueryService`, … |
| Inbound adapter | `adapter/in/web/payment`: `PaymentController`, `PaymentWebMapper` |
| Outbound adapter | `adapter/out/persistence/payment`: `PaymentPersistenceAdapter`, `BudgetLookupAdapter` |

What it still lacks to be the target: its adapters use the legacy JPA entities from `business` as the model, so
the persistence entity and the domain model are not yet separate classes (ADR-030 backend rule 4).

## Backend slice skeleton (strict form)

```java
// domain.concept — plain Java
public record Concept(Integer id, String name, java.math.BigDecimal value, int percentage) {
    public Concept {
        if (name == null || name.isBlank()) throw new InvalidConceptException("name is required");
    }
}

// application.port.in.concept
public interface CreateConceptUseCase {
    Concept create(CreateConceptCommand command);
    record CreateConceptCommand(String name, java.math.BigDecimal value, int percentage) {}
}

// application.port.out.concept — domain terms only
public interface ConceptRepositoryPort {
    Concept save(Concept concept);
    boolean existsByName(String name);
}

// application.usecase.concept — only @Service and @Transactional from Spring
@Service
@Transactional
class CreateConceptService implements CreateConceptUseCase {
    private final ConceptRepositoryPort concepts;
    CreateConceptService(ConceptRepositoryPort concepts) { this.concepts = concepts; }

    public Concept create(CreateConceptCommand c) {
        if (concepts.existsByName(c.name())) throw new DuplicateConceptException(c.name());
        return concepts.save(new Concept(null, c.name(), c.value(), c.percentage()));
    }
}

// adapter.out.persistence.concept — the only place that knows JPA
@Component
class ConceptPersistenceAdapter implements ConceptRepositoryPort {
    private final ConceptJpaRepository jpa;
    private final ConceptMapper mapper;
    // save: mapper.toDomain(jpa.save(mapper.toEntity(concept)))
}

// adapter.in.web.concept — validation, one port call, web DTO, status
@RestController
@RequestMapping("/api/v1/concepts")
class ConceptController {
    private final CreateConceptUseCase create;
    @PostMapping ResponseEntity<DtoConcept> create(@Valid @RequestBody DtoCreateConcept body) { /* map, call, 201 */ }
}
```

Tests: the use case is unit-tested with an in-memory `ConceptRepositoryPort` (no Spring, no database); the
mapper has its own unit tests; one MockMvc characterization test pins the REST behaviour before and after.

## Frontend slice skeleton

```ts
// core/concept/domain/concept.ts — pure
export interface Concept { id: number; name: string; value: number; percentage: number }

// core/concept/application/ports.ts
export interface ConceptGateway {
  list(): Promise<Concept[]>;
  create(input: Omit<Concept, "id">): Promise<Concept>;
}

// core/concept/application/create-concept.ts — no React, no fetch
export const createConcept = (gateway: ConceptGateway) => (input: Omit<Concept, "id">) => {
  if (!input.name.trim()) throw new Error("name is required");
  return gateway.create(input);
};

// adapters/http/concept/concept-http-gateway.ts — the only importer of lib/api-client
export const conceptHttpGateway: ConceptGateway = {
  list: async () => (await apiGet<ConceptDto[]>("/concepts")).map(toDomain),
  create: async (input) => toDomain(await apiPost<ConceptDto>("/concepts", toDto(input))),
};

// composition/container.ts — one place that wires; exposed through a React provider
// hooks/use-create-concept.ts — useMutation over useCases().createConcept
```

Tests: use cases with a fake gateway (plain Vitest); the http gateway with a mocked `api-client`; pages and
hooks with the composition provider given fake use cases, so no module mocking.

## Anti-patterns seen in this repository

| Anti-pattern | Example | Fix |
|---|---|---|
| Controller builds data access by hand from a static holder | `JpaControllerProvider` (#574) | inject a use case; the static holder is gone |
| Deleting a child without unlinking it from cascading parents | budget template delete answered 204, row stayed (#574) | characterization test on delete; unlink in the use case or map the aggregate |
| Controller injects a repository | 13 controllers (#1693) | add an inbound port and a use case |
| Port returns a JPA entity | 41 of 60 port files import `business.*` | return a domain type; map in the adapter |
| `@Transactional` on a controller | 31 web classes | move to the use case |
| Domain exception with an HTTP status | `NotaireException(statusCode, …)` | plain exception; the global handler maps it |
| Business rule in a mapper or a hook | — | rules belong in `domain` or a use case |
