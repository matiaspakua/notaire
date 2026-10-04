# CU84 – Login al sistema

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU84 – Login al sistema |
| **Actores** | Usuario, Sistema de Autenticación |
| **Propósito** | Proteger la integridad de los datos y la privacidad de los usuarios exigiendo autenticación antes de acceder a las funciones administrativas y operativas. |
| **Descripción** | Todos los usuarios (excepto clientes en módulos específicos) se autentican con nombre de usuario y contraseña; el sistema establece la sesión del navegador y registra el acceso en la bitácora de auditoría. |
| **Tipo** | Soporte / Seguridad |
| **Precondiciones** | El usuario tiene una cuenta registrada y activa, con credenciales vigentes. |
| **Referencias Cruzadas** | RF #82 (Acceso de usuarios), RF #1224 (Login al sistema) |
| **GitHub ID** | #1224, #1051, #1052, #1053, #1056 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El usuario accede a la pantalla de Login. |  |
| 2 | El usuario ingresa su nombre de usuario y contraseña y presiona "Ingresar". |  |
| 3 |  | Valida las credenciales con la base de datos. |
| 4 |  | Si son válidas, establece la sesión del navegador con la cookie HttpOnly `notaire-auth-token` (SameSite=Lax; Secure en producción) y cookies de experiencia no credenciales para el edge proxy (`proxy.ts`, #1052 / #1056). El JWT **no** se guarda en `localStorage` (issue #1051). |
| 5 |  | Muestra un mensaje de éxito y redirige al usuario a su panel principal según su rol. |
| 6 |  | Registra el inicio de sesión en el log de auditoría. |
| 7 | Al cerrar sesión, el cliente llama al endpoint de logout. | Limpia la cookie HttpOnly; luego el cliente limpia su estado. |

## Excepciones / Flujos Alternativos

- **3a. Credenciales incorrectas**: el sistema muestra un mensaje de error indicando que las credenciales no coinciden.
- **3b. Usuario no encontrado**: el sistema informa que el usuario no existe.
- **3c. Cuenta bloqueada**: el sistema informa que la cuenta está bloqueada por múltiples intentos fallidos.
- **3d. Error de conexión**: el sistema informa que no puede conectar con el servidor de autenticación.
- **3e. Sesión expirada (HTTP 401 en petición autenticada)**: mientras el usuario cree estar autenticado, si el API responde `401 Unauthorized` (por ejemplo JWT vencido), el cliente intenta limpiar la cookie HttpOnly vía logout, limpia el estado local de autenticación, redirige a `/login?expired=1` y muestra un mensaje claro de que la sesión expiró y debe iniciar sesión nuevamente (#1053). Un intento de login con credenciales inválidas (también `401`) **no** dispara este flujo de expiración.

## Postcondiciones

- El usuario queda autenticado para la sesión actual (cookie HttpOnly + estado de cliente sin JWT legible por script).
- Se registra el inicio de sesión en la bitácora de actividades.
- Si la sesión termina por expiración (flujo 3e) o logout, la cookie de autenticación queda invalidada y el usuario debe completar de nuevo el curso de eventos.
