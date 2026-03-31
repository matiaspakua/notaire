---
title: Java Programming Best Practices
description: General rules for clean and maintainable Java code
alwaysApply: true
---

## Fundamental Principles

- Write readable, simple, and maintainable code
- Follow SOLID principles
- Prefer composition over inheritance
- Design for change: isolate business logic and minimize framework dependencies
- Avoid code duplication through modularization and iteration
- Program to interfaces, not implementations

## Naming Conventions

- Variables and methods: camelCase with auxiliary verbs (isLoading, hasError, getUserData)
- Classes and interfaces: PascalCase
- Constants: UPPER_SNAKE_CASE
- Packages: lowercase without underscores (com.company.module)
- Descriptive names that reveal intention
- Avoid ambiguous abbreviations
- Collections: plural names (users, orders, products)
- Booleans: start with is/has/can/should
- Avoid -Er/-Or/-Utils suffixes in classes (high cyclomatic complexity)

## Code Structure

- Indentation: 4 spaces (Java standard)
- Line limit: 120 characters
- Code blocks separated by blank lines
- Order members: constants, static fields, instance fields, constructors, public methods, private methods
- One concept per method
- Small methods: maximum 20-30 lines
- Maximum 3-4 parameters per method
- Consistent abstraction levels
- Group related methods together

## Java Conventions

- Always use braces for if/else/while/for blocks
- Opening brace on same line
- else on same line as closing brace
- Space after keywords (if (), while (), for ())
- Spaces around operators (a + b, x == y)
- One statement per line
- Use @Override annotation consistently
- Avoid wildcard imports (import java.util.*)
- Order imports: java, javax, third-party, own packages

## Object-Oriented Design

- Classes should have single responsibility
- Keep classes small and focused
- Minimize class coupling
- Maximize cohesion
- Encapsulate fields (private with getters/setters when needed)
- Prefer immutable objects when possible
- Use final for classes, methods, and variables when appropriate
- Avoid exposing internal state

## Error Handling

- Never ignore exceptions silently
- Catch specific exceptions, not generic Exception
- Use try-with-resources for AutoCloseable resources
- Propagate exceptions with additional context
- Validate inputs at component boundaries
- Appropriate error logging with levels (DEBUG, INFO, WARN, ERROR)
- Don't use exceptions for control flow
- Document thrown exceptions with @throws in Javadoc

## Type Safety

- Use generics to provide type safety
- Avoid raw types
- Prefer specific types over Object
- Use Optional<T> for nullable return values
- Minimize use of type casting
- Check for null before dereferencing
- Use Objects.requireNonNull() for parameter validation
- Consider @NonNull/@Nullable annotations

## Collections and Streams

- Use appropriate collection types (List, Set, Map)
- Prefer interfaces over implementations (List<String> vs ArrayList<String>)
- Use Stream API for functional operations
- Avoid modifying collections during iteration
- Use forEach only for side effects, not for transformations
- Consider parallel streams for large datasets
- Close streams that use IO resources
- Prefer Collections.emptyList() over new ArrayList<>() for empty returns

## Comments and Documentation

- Self-documenting code over comments
- Comment the "why", not the "what"
- Remove commented code, use version control
- Document public APIs with Javadoc
- Include @param, @return, @throws in method documentation
- Update comments together with code
- Use TODO/FIXME markers for temporary code
- Package-level documentation in package-info.java

## Testing

- Write unit tests for business logic
- Use JUnit 5 for testing
- Descriptive test method names (shouldReturnUserWhenValidIdProvided)
- AAA pattern: Arrange, Act, Assert
- One assertion per test when possible
- Use mocks for external dependencies (Mockito)
- Independent tests without execution order dependency
- Minimum coverage: 70-80% for critical code
- Test edge cases and error conditions

## Performance

- Avoid premature optimization
- Profile before optimizing
- Minimize object creation in loops
- Use StringBuilder for string concatenation in loops
- Lazy initialization for expensive objects
- Consider algorithm complexity (Big O notation)
- Reuse expensive objects (connections, threads)
- Cache expensive computations when appropriate
- Use primitive types instead of wrappers when possible

## Concurrency

- Minimize mutable shared state
- Use java.util.concurrent utilities over raw threads
- Prefer ExecutorService over Thread creation
- Use thread-safe collections (ConcurrentHashMap, CopyOnWriteArrayList)
- Synchronize access to shared mutable state
- Avoid nested synchronization blocks
- Document thread-safety guarantees
- Consider using volatile for simple flags
- Use atomic classes for lock-free operations

## Security

- Validate and sanitize all inputs
- Don't expose sensitive information in logs
- Use PreparedStatement for SQL queries (prevent SQL injection)
- Implement principle of least privilege
- Keep dependencies updated
- Don't hardcode credentials or secrets
- Validate permissions for each operation
- Use secure random number generation (SecureRandom)
- Close security-sensitive resources properly

## Version Control

- Atomic and descriptive commits
- Commit messages format: type(scope): description
- Types: feat, fix, docs, style, refactor, test, chore
- Branches: feature/, bugfix/, hotfix/
- Mandatory code review before merge
- Don't commit commented code or temporary files
- Keep commits focused on single concern

## Clean Code Principles

- DRY (Don't Repeat Yourself)
- KISS (Keep It Simple, Stupid)
- YAGNI (You Aren't Gonna Need It)
- Separation of Concerns
- Single Responsibility Principle
- Open/Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

## Conditional Logic

- Limit conditionals to maximum 2 levels depth
- Extract complex conditionals to methods with descriptive names
- Prefer guard clauses over nested else
- Use ternary operator only for simple cases
- Consider polymorphism or strategy pattern for complex conditionals
- Avoid negative conditionals when possible

## Resource Management

- Use try-with-resources for AutoCloseable resources
- Close resources explicitly (streams, connections, files)
- Implement cleanup in finally blocks
- Release listeners and subscribers to avoid memory leaks
- Implement AutoCloseable for custom resource classes
- Don't rely solely on finalize() for cleanup

## Unused Code

- Remove unused imports
- Don't declare unused variables
- Configure linter to detect dead code
- Remove unused parameters
- Clean up unused private methods
- Remove deprecated code after migration period

## Immutability

- Prefer immutable objects when possible
- Use final for variables that don't change
- Make fields final when they're set only in constructor
- Avoid side effects in methods
- Return defensive copies of mutable fields
- Use Collections.unmodifiableList/Map/Set for immutable collections
- Consider using records (Java 14+) for data carriers

## Dependency Management

- Minimize dependencies between modules
- Use dependency injection
- Avoid circular dependencies
- Keep dependency versions consistent
- Regularly update dependencies for security patches
- Use build tools (Maven/Gradle) for dependency management
- Document required dependencies

## Logging

- Use appropriate log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- Don't log sensitive information
- Use parameterized logging (avoid string concatenation)
- Include context in log messages
- Log exceptions with full stack traces
- Use logging framework (SLF4J, Log4j2)
- Configure log rotation and retention

## Constants and Enums

- Use enums instead of int constants
- Define constants at appropriate scope level
- Group related constants in classes or enums
- Use static imports for frequently used constants
- Prefer enums with behavior over switch statements
- Make enum constructors private

## Method Design

- Methods should do one thing well
- Keep methods small and focused
- Use descriptive method names (verbs)
- Minimize method parameters
- Avoid boolean parameters (use enums or separate methods)
- Return empty collections instead of null
- Use method overloading judiciously
- Consider builder pattern for objects with many parameters
