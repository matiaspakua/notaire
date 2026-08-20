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
5. El sistema muestra un mensaje de éxito y redirige al usuario a su panel principal según su rol.
6. El sistema registra el inicio de sesión en el log de auditoría.

## Alternative Flows
- **3a. Credenciales incorrectas**: El sistema muestra un mensaje de error indicando que las credenciales no coinciden.
- **3b. Usuario no encontrado**: El sistema informa que el usuario no existe.
- **3c. Cuenta bloqueada**: El sistema informa que la cuenta está bloqueada por múltiples intentos fallidos.
- **3d. Error de conexión**: El sistema informa que no puede conectar con el servidor de autenticación.

## Post-conditions
- El usuario queda autenticado para la sesión actual.
- Se registra el inicio de sesión en la bitácora de actividades.
