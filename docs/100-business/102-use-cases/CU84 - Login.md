# CU-84: Login al sistema

## Context
El acceso al sistema es fundamental para proteger la integridad de los datos y la privacidad de los usuarios. Todos los usuarios (excepto clientes en módulos específicos) deben autenticarse para acceder a las funciones administrativas y operativas.

## Actors
- **Usuario** (Actor)
- **Sistema de Autenticación** (Sistema Externo)

## Pre-conditions
- El usuario debe tener una cuenta registrada y activa.
- El usuario debe tener las credenciales vigentes.

## Main Flow
1. El usuario accede a la pantalla de Login.
2. El usuario ingresa su Nombre de Usuario (Username) y Contraseña (Password).
3. El usuario presiona el botón "Ingresar".
4. El sistema valida las credenciales con la base de datos.
5. Si son válidas, el sistema establece la sesión del navegador con cookie HttpOnly `notaire-auth-token` (SameSite=Lax; Secure en producción) y cookies UX no-credenciales para el middleware (#1052). El JWT **no** se guarda en `localStorage` (issue #1051).
6. El sistema muestra un mensaje de éxito y redirige al usuario a su panel principal según su rol.
7. El sistema registra el inicio de sesión en el log de auditoría.
8. Al cerrar sesión, el cliente llama al endpoint de logout para limpiar la cookie HttpOnly y luego limpia el estado de cliente.

## Alternative Flows
- **3a. Credenciales incorrectas**: El sistema muestra un mensaje de error indicando que las credenciales no coinciden.
- **3b. Usuario no encontrado**: El sistema informa que el usuario no existe.
- **3c. Cuenta bloqueada**: El sistema informa que la cuenta está bloqueada por múltiples intentos fallidos.
- **3d. Error de conexión**: El sistema informa que no puede conectar con el servidor de autenticación.
- **3e. Sesión expirada (HTTP 401 en petición autenticada)**: Mientras el usuario cree estar autenticado, si el API responde `401 Unauthorized` (por ejemplo JWT vencido), el cliente intenta limpiar la cookie HttpOnly vía logout, limpia el estado local de autenticación, redirige a `/login?expired=1` y muestra un mensaje claro de que la sesión expiró y debe iniciar sesión nuevamente. Un intento de login con credenciales inválidas (también `401`) **no** dispara este flujo de expiración.

## Post-conditions
- El usuario queda autenticado para la sesión actual (cookie HttpOnly + estado de cliente sin JWT legible por script).
- Se registra el inicio de sesión en la bitácora de actividades.
- Si la sesión termina por expiración (flujo 3e) o logout, la cookie de autenticación queda invalidada/limpiada y el usuario debe completar de nuevo el flujo principal de login.
