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
| **GitHub ID** | #254, #267, #280, #281, #282, #283, #307, #309 |

## Alcance Técnico

- Autenticación segura basada en tokens JWT y hash robusto de contraseñas (BCrypt con salt).
- Encriptación de todas las comunicaciones cliente-servidor mediante HTTPS/TLS 1.3.
- Políticas de control de acceso basado en roles (RBAC) para todas las funciones del sistema.
- Aislamiento estricto de la base de datos (sin acceso público directo, solo red interna protegida).
- Escaneo continuo de vulnerabilidades en dependencias y código fuente.

## Procedimiento de Seguridad y Control de Acceso

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El usuario ingresa sus credenciales (nombre de usuario y contraseña). | Valida las credenciales contra el hash seguro en base de datos cifrada. |
| 2 | Si las credenciales son válidas, se genera una sesión autenticada con token firmado. | Transmite el token seguro mediante canal cifrado HTTPS/TLS. |
| 3 | El usuario solicita ejecutar una operación sobre un trámite o cliente. | Valida los permisos de rol del usuario y autoriza la ejecución. |
| 4 | El sistema procesa la operación. | Registra la acción en el log de auditoría inmutable indicando usuario, fecha, hora e IP. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 1.1 | Credenciales incorrectas o usuario inactivo | El sistema rechaza el acceso, incrementa el contador de intentos fallidos y no revela detalles sensibles. |
| 3.1 | Usuario sin permisos para la operación solicitada | El sistema bloquea la acción con código HTTP 403 Forbidden y registra el evento de seguridad en auditoría. |

## Criterios de Aceptación

- [x] Contraseñas almacenadas con cifrado fuerte (BCrypt).
- [x] Canal de comunicación seguro HTTPS/TLS obligatorio en producción.
- [x] Control de acceso basado en roles (RBAC) verificado en todos los endpoints.
- [x] Auditoría de seguridad y eventos de acceso registrada permanentemente.
