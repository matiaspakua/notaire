---
name: api-rest
description: Generate Bruno API test collections from project API definitions. Use when creating API tests, exporting collections, or testing REST endpoints.
---

# API-to-Bruno Collection Generator Skill

## Description
Analyze a project's full API and generate a Bruno collection with subdirectories per business model, separate `.bru` requests, assertions, JS/Chai tests, and docs. The collection runs in the Bruno app.

## Required inputs
- Project source (repository or API files: routes, controllers, schemas)
- Project base URL
- Schemas and auth details (optional)

## Outputs
- Bruno collection folder: `collection.bru`, `subdirs/*.bru`, `requests/*.bru`, `environments/*.bru`
- Files ready for Git and the Bruno CLI: `bru run --env dev`

## Dependencies
- Bruno >= v1.20 [web:42]
- Chai assertions in tests [web:23]
- `bru.setVar()` for cross-request variables

## Execution
1. Analyze the code and map the endpoints
2. Infer the business model and create subdirectories
3. Generate one `.bru` per request: `http{}`, `assertions{}`, `tests{}`, `docs{}`
4. Validate Bruno YAML/Bru syntax
5. Test locally: open Bruno, load the collection, run all, and require a 100% pass

## Built-in best practices [web:16][web:39][web:40]
- Env vars: `{{base_url}}`, `{{auth_token}}`
- Tests: status, body keys and types, nested objects, arrays and loops, headers, response time
- Assertions: `res.getStatus() eq 200`, `res.getBody() has.property('id')`
- Docs: params, expected response, errors

## Example output structure
api-tests/
├── collection.bru
├── auth/
│ ├── login.bru
│ └── refresh.bru
├── users/
│ ├── GET-users.bru
│ └── POST-user.bru
└── environments/
└── dev.bru
