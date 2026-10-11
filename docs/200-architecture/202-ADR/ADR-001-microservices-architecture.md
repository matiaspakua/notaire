# ADR-001: Migration from the Swing monolith to a layered service architecture

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez, Architecture team  
**Supersedes:** N/A  
**Superseded by:** N/A  

## Context

Notaire was originally a monolithic application written in Java 1.6 with a Swing interface, direct access to a MySQL database, and business logic coupled to the presentation layer. That architecture had these problems:

- **Tight coupling**: business logic was split between the GUI and the database
- **Hard to maintain**: a requirement change needed edits in several layers
- **Limited scale**: individual components could not be scaled
- **Hard to test**: coupling to Swing and JDBC made automated tests difficult
- **Outdated technology**: Java 1.6, MySQL, and no modern standards

**Constraints:**

- Keep the existing behavior (54 use cases)
- Migrate gradually, without a full stop of the system
- A team experienced in Java and new to services
- Limited budget and time

## Decision

Move to a three-layer architecture with a clear split:

1. **Backend REST API** (Spring Boot 4.1.0, Java 21, PostgreSQL 16)
   - Holds all business logic
   - Provides a standard REST API
   - Manages data persistence
   - Implements audit and security

2. **Web frontend (Next.js)** (target technology — see ADR-005)
   - A modern web user interface
   - Replaces the Swing client gradually
   - Uses the centralized design system (see ADR-011)

3. **Swing client** (maintenance / transition)
   - Refactored to call the REST API
   - Being deprecated in favor of the web version
   - No business logic and no direct database access

4. **Shared module** (DTOs and common code)
   - Shared definitions between the backend and the Swing/Java frontend
   - Makes contract versioning easier

**Chosen technologies:**

- **Backend**: Spring Boot 4.1.0, Java 21
- **Database**: PostgreSQL 16, Hibernate
- **Build**: Maven 3.x
- **Deploy**: Docker + Docker Compose
- **API**: REST with OpenAPI 3.0 (Swagger)

## Options Considered

### Option A: Keep the Swing monolith and improve it

| Dimension | Assessment |
|-----------|-----------|
| Complexity | Low |
| Cost | Low |
| Scalability | Very low |
| Maintainability | Very low |
| Testability | Very low |

**Pros:**

- Minimal changes to existing code
- No team retraining
- Low initial cost

**Cons:**

- Does not fix the coupling
- Components still cannot scale
- Automated testing stays limited
- The old technology dependency continues
- Adding new features stays hard

### Option B: Full rewrite (big bang)

| Dimension | Assessment |
|-----------|-----------|
| Complexity | High |
| Cost | Very high |
| Scalability | High |
| Maintainability | High |
| Risk | Very high |

**Pros:**

- Entirely new, clean code
- A modern architecture from the start
- Potentially better performance
- A chance to refactor everything

**Cons:**

- High risk of failures during the transition
- A very long development (6+ months)
- The old system is unavailable during the migration
- Accumulated knowledge is lost
- Very high cost

### Option C: Gradual migration to 3 layers (selected)

| Dimension | Assessment |
|-----------|-----------|
| Complexity | Medium |
| Cost | Medium |
| Scalability | High |
| Maintainability | High |
| Risk | Low |

**Pros:**

- A controlled migration that can be checked at each step
- The system stays usable at every stage
- Lower risk of critical failures
- The business can keep operating
- Incremental testing of each module
- Gradual learning of the new technologies

**Cons:**

- Needs more coordination
- Technologies coexist for a while
- Several versions have to be maintained
- The initial setup work is duplicated

## Trade-off Analysis

**Scalability versus complexity**

Option C needs more coordination at the start than keeping the monolith, and it lets each component scale on its own. The backend can be replicated without touching the GUI, and the database can be tuned without changing application logic.

**Risk versus speed**

The big bang (option B) would be faster in theory, and the operational risk is unacceptable. The gradual migration lowers that risk: each change can be tried in production with a rollback.

**Transition cost versus maintenance cost**

The initial migration cost is offset by:

- Fewer production bugs
- Shorter time to market for new features
- Better performance
- The ability to scale under load

## Consequences

### Positive

- **Separation of responsibilities**: the GUI and the business logic are decoupled
- **Testability**: the backend can be tested on its own, without a GUI
- **Horizontal scale**: the backend can scale with demand
- **Technology update**: Java 21, Spring Boot, PostgreSQL
- **Reusable API**: other clients (mobile, web) can call the API
- **Stronger security**: centralized control and audit in the backend

### Challenges

- **Several modules to maintain**: more packages to keep
- **API versioning**: the evolution of REST contracts has to be planned
- **Network latency**: HTTP calls have latency that direct calls do not
- **Operational complexity**: more services to deploy and monitor
- **Temporary duplication**: legacy and new code coexist during the transition

### Follow-up actions

- [ ] Set SLAs for the REST API
- [ ] Add circuit breakers for resilience
- [ ] A deprecation plan for legacy code
- [ ] An API versioning strategy (semantic versioning)
- [ ] REST contract documentation (OpenAPI)

## Implementation Plan

1. **Phase 1**: Set up the Spring Boot backend with the shared module
2. **Phase 2**: Move business entities and repositories
3. **Phase 3**: Implement business services
4. **Phase 4**: Create REST endpoints
5. **Phase 5**: Refactor the GUI to call the API
6. **Phase 6**: Deprecate legacy code
7. **Phase 7**: Monitoring and tuning

## Related Decisions

- ADR-002: Maven package and module structure
- ADR-003: REST API versioning strategy
- ADR-004: Security and authentication on the REST API
- ADR-005: Testing strategy for the distributed architecture
