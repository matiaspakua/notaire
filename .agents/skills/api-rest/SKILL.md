---
name: api-rest
description: Generate Bruno API test collections from project API definitions. Use when creating API tests, exporting collections, or testing REST endpoints.
---

# API-to-Bruno Collection Generator Skill

## Descripción
Analiza API completa de proyecto y genera colección Bruno con subdirs por modelo de negocio, requests .bru separados, assertions, tests JS/Chai, docs. Ejecutable en Bruno app.

## Inputs Requeridos
- Código fuente proyecto (repositorio/archivos API: routes, controllers, schemas)
- Base URL del proyecto
- Esquemas/auth details (opcional)

## Outputs
- Carpeta colección Bruno: collection.bru, subdirs/*.bru, requests/*.bru, environments/*.bru
- Archivos listos para Git/Bruno CLI: `bru run --env dev`

## Dependencias
- Bruno >= v1.20 [web:42]
- Chai assertions en tests [web:23]
- bru.setVar() para variables cross-request

## Ejecución
1. Analizar código → mapear endpoints
2. Inferir modelo negocio → crear subdirs
3. Generar .bru por request: http{}, assertions{}, tests{}, docs{}
4. Validar sintaxis Bruno YAML/Bru
5. Test local: abrir Bruno → load collection → run all → 100% pass

## Best Practices Integradas [web:16][web:39][web:40]
- Vars env: {{base_url}}, {{auth_token}}
- Tests: status, body keys/types, nested objects, arrays/loops, headers, response time
- Assertions: res.getStatus() eq 200, res.getBody() has.property('id')
- Docs: params, expected response, errors

## Ejemplo Output Structure
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
