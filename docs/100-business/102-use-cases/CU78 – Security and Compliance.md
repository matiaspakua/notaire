# CU78 – Seguridad, Privacidad y Cumplimiento (Security and Compliance)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU78 – Seguridad, Privacidad y Cumplimiento |
| **Actores** | Oficial de Seguridad, Administrador del Sistema, Equipo de Desarrollo |
| **Propósito** | Garantizar la confidencialidad, integridad y disponibilidad de la información notarial mediante autenticación robusta, cifrado de contraseñas, encriptación en tránsito (HTTPS/TLS), control de acceso a base de datos y cumplimiento de normativas de privacidad. |
| **Descripción** | Establece los controles de seguridad esenciales para proteger datos personales de clientes, escrituras y trámites frente a accesos no autorizados o vulnerabilidades (OWASP Top 10). |
| **Tipo** | Soporte / Seguridad |
| **Referencias Cruzadas** | RF #81 (Seguridad y privacidad), RF #82 (Acceso de usuarios), RF #83 (Cifrado de contraseña), RF #84 (Transporte de información por red), RF #85 (Acceso a la base de datos); CU20, CU21 |
| **GitHub ID** | #254, #267, #280, #281, #282, #283, #307, #309, #1044, #1045, #1046, #1051, #1055 |

## Alcance Técnico

- Autenticación segura basada en tokens JWT y hash robusto de contraseñas (BCrypt con salt).
- Sesión de navegador con JWT en cookie HttpOnly / SameSite / Secure (prod) — no en `localStorage` (issue #1051). El API acepta cookie o `Authorization: Bearer` (herramientas).
- CSP de producción con `script-src` basado en nonce y sin `'unsafe-eval'` (issue #1051).
- Encriptación de todas las comunicaciones cliente-servidor mediante HTTPS/TLS 1.3.
- Políticas de control de acceso basado en roles (RBAC) para todas las funciones del sistema.
- Aislamiento estricto de la base de datos (sin acceso público directo, solo red interna protegida).
- Artefacto de despliegue de producción (`docker-compose.prod.yml`, issue #1044): sin pgAdmin, sin puertos de host para Postgres/backend/frontend, ingreso solo por reverse proxy, secretos obligatorios `${VAR:?}`, `ENVIRONMENT=production`.
- Escaneo continuo de vulnerabilidades en dependencias y código fuente.
- Higiene Dependabot (issue #1046): sin árbol Swing con Log4j 1.x; override npm
  `smol-toml` ≥1.7.1 (pin `^1.9.0`) en el frontend.
- Imágenes de contenedor pinneadas a minor/digest y Dependabot docker
  (`/backend-api`, `/frontend`) además de npm (issue #1045).
- URL del backend de API resuelta en tiempo de request vía BFF (`BACKEND_URL`
  server-only); la página pública `/login` no revela hostnames internos
  (issue #1055).

## Procedimiento de Seguridad y Control de Acceso

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El usuario ingresa sus credenciales (nombre de usuario y contraseña). | Valida las credenciales contra el hash seguro en base de datos cifrada. |
| 2 | Si las credenciales son válidas, se genera una sesión autenticada con token firmado. | Emite cookie HttpOnly `notaire-auth-token` (y opcionalmente JSON token para clientes API) por canal HTTPS/TLS. |
| 3 | El usuario solicita ejecutar una operación sobre un trámite o cliente. | Valida los permisos de rol del usuario y autoriza la ejecución. |
| 4 | El sistema procesa la operación. | Registra la acción en el log de auditoría inmutable indicando usuario, fecha, hora e IP. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 1.1 | Credenciales incorrectas o usuario inactivo | El sistema rechaza el acceso, incrementa el contador de intentos fallidos y no revela detalles sensibles. |
| 3.1 | Usuario sin permisos para la operación solicitada | El sistema bloquea la acción con código HTTP 403 Forbidden y registra el evento de seguridad en auditoría. |
| 3.2 | Usuario autenticado no administrador navega a `/dashboard/administracion/**` (issue #1052) | El cliente (edge proxy + layout) redirige a `/dashboard?forbidden=1` y muestra un mensaje de acceso denegado. El control real de API permanece en RBAC del backend (#559). Cookies UX `notaire-auth-status` / `notaire-auth-role` no son credenciales; la sesión API usa cookie HttpOnly (#1051). |

## Criterios de Aceptación

- [x] Contraseñas almacenadas con cifrado fuerte (BCrypt).
- [x] Canal de comunicación seguro HTTPS/TLS obligatorio en producción.
- [x] Control de acceso basado en roles (RBAC) verificado en todos los endpoints.
- [x] Auditoría de seguridad y eventos de acceso registrada permanentemente.
- [x] Pantallas de administración del frontend no accesibles por URL para usuarios no administradores (guard de layout + edge; Playwright TS-0094).
- [x] Compose de producción sin pgAdmin ni exposición de DB/app en el host; solo reverse proxy publica puertos; secretos sin defaults `admin` (issue #1044).
- [x] JWT de sesión del navegador en cookie HttpOnly; sin JWT usable en `localStorage` (issue #1051).
- [x] CSP de producción sin `'unsafe-eval'` y con nonce en `script-src` (issue #1051).
- [x] Alertas Dependabot críticas/altas por `log4j:log4j` en Swing muerto y por
      `smol-toml` resueltas: árbol `deprecated-frontend-swing/` eliminado; override
      npm `smol-toml` `^1.9.0` (issue #1046). Guard: `security/tests/test_dependabot_hygiene.py`.
- [x] Imágenes compose/Dockerfile/CI pinneadas (sin `:latest` / `sonarqube:community`
      / postgres major-only); Dependabot con npm `/frontend` y docker
      `/backend-api` + `/frontend` (issue #1045). Guard:
      `security/tests/test_image_pins_and_dependabot.py`.
- [x] Proxy BFF de `/api/v1` con `BACKEND_URL` en runtime; sin filtrar URL interna
      del backend en `/login` (issue #1055).
