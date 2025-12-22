# Agent Instructions for Refactoring

## Context
Refactoring monolithic Java app to microservices. Follow strict separation of concerns.

## When Working on Backend
- Always create DTOs for API endpoints
- Use constructor injection for services
- Add validation annotations
- Implement proper exception handling
- No Swing dependencies allowed

## When Working on GUI
- Replace database calls with API client calls
- Remove business logic
- Keep only presentation logic
- Use SwingWorker for async operations
- Show loading indicators

## When Creating REST Endpoints
- Follow /api/v1/resource pattern
- Return appropriate HTTP status codes
- Use ResponseEntity wrapper
- Add pagination for lists
- Document with Swagger annotations