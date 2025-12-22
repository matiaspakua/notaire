# Plan Detallado de Migración del Frontend Swing

## Objetivo
Migrar completamente el módulo `/frontend-swing` para que funcione con los 3 servicios backend (PostgreSQL, Backend API, y opcionalmente pgAdmin), manteniendo separación de responsabilidades entre capas (presentación, lógica de negocio y acceso a datos).

---

## Fase 1: Análisis y Preparación

### 1.1 Inventario de Componentes Swing Originales
**Estado**: ✅ Completado
- **95 formularios Swing** identificados en `/src.old/main/java/com/licensis/notaire/gui/`
- **Módulos principales**:
  - Login (1 formulario)
  - Principal (ventana principal con JDesktopPane)
  - Clientes (7 formularios)
  - Presupuestos (9 formularios)
  - Gestiones (25 formularios)
  - Protocolo (7 formularios)
  - Pagos (3 formularios)
  - Administración (40 formularios)

### 1.2 Análisis de Dependencias Backend
**Estado**: ⏳ Pendiente
- **Controladores REST existentes**: 19/26 entidades
- **Controladores faltantes críticos**:
  - `UsuarioController` - Necesario para autenticación
  - `PersonaController` - Necesario para módulo Clientes
  - `GestionController` - Necesario para módulo Gestiones
  - `EstadoDeGestionController` - Necesario para estados de gestión
  - `TipoDeTramiteController` - Necesario para administración
  - `TipoDeFolioController` - Necesario para protocolo
  - `PlantillaPresupuestoController` - Necesario para presupuestos
  - `PlantillaTramiteController` - Necesario para trámites
  - `TramitesPersonasController` - Necesario para relaciones

### 1.3 Análisis de Lógica de Negocio
**Estado**: ⏳ Pendiente
- Revisar `ControllerNegocio.java` para identificar métodos de negocio
- Identificar validaciones en `AdministradorValidaciones.java`
- Mapear flujos de usuario en cada módulo

### 1.4 Estructura de Capas Propuesta
```
frontend-swing/
├── gui/                    # Capa de presentación (Swing)
│   ├── login/
│   ├── principal/
│   ├── clientes/
│   ├── presupuestos/
│   ├── gestiones/
│   ├── protocolo/
│   ├── pagos/
│   └── administracion/
├── servicios/              # Capa de lógica de negocio
│   ├── AuthService.java
│   ├── ClienteService.java
│   ├── PresupuestoService.java
│   ├── GestionService.java
│   └── ...
├── api/                    # Capa de acceso a datos (REST)
│   ├── client/
│   │   ├── RestClient.java (✅ existe)
│   │   ├── ApiConfig.java (✅ existe)
│   │   └── BaseRestClient.java
│   └── dto/                # DTOs compartidos
└── util/                   # Utilidades
    ├── SessionManager.java
    └── ErrorHandler.java
```

---

## Fase 2: Creación de Controladores Backend Faltantes

### 2.1 Controladores Críticos (Prioridad Alta)
**Estado**: ⏳ Pendiente

#### 2.1.1 UsuarioController
- **Endpoint**: `/api/v1/usuarios`
- **Métodos necesarios**:
  - `GET /api/v1/usuarios` - Listar usuarios
  - `GET /api/v1/usuarios/{id}` - Obtener usuario
  - `POST /api/v1/usuarios` - Crear usuario
  - `PUT /api/v1/usuarios/{id}` - Actualizar usuario
  - `DELETE /api/v1/usuarios/{id}` - Eliminar usuario
  - `POST /api/v1/usuarios/login` - Autenticación (endpoint especial)
- **Dependencias**: `UsuarioJpaController` (✅ existe)

#### 2.1.2 PersonaController
- **Endpoint**: `/api/v1/personas`
- **Métodos necesarios**: CRUD estándar
- **Dependencias**: `PersonaJpaController` (✅ existe)

#### 2.1.3 GestionController
- **Endpoint**: `/api/v1/gestiones`
- **Métodos necesarios**: CRUD + endpoints especiales
- **Dependencias**: `GestionDeEscrituraJpaController` (✅ existe)

### 2.2 Controladores Secundarios (Prioridad Media)
- `EstadoDeGestionController`
- `TipoDeTramiteController`
- `TipoDeFolioController`
- `PlantillaPresupuestoController`
- `PlantillaTramiteController`
- `TramitesPersonasController`

---

## Fase 3: Creación de Capa de Servicios en Frontend

### 3.1 Servicios Base
**Estado**: ⏳ Pendiente

#### 3.1.1 AuthService
- **Responsabilidad**: Autenticación y gestión de sesión
- **Métodos**:
  - `login(String usuario, String password): DtoUsuario`
  - `logout()`
  - `getCurrentUser(): DtoUsuario`
  - `isAuthenticated(): boolean`
- **Dependencias**: `UsuarioController` (crear en Fase 2)

#### 3.1.2 SessionManager
- **Responsabilidad**: Gestión de sesión de usuario
- **Patrón**: Singleton
- **Almacenamiento**: Usuario actual, permisos, configuración

### 3.2 Servicios por Módulo

#### 3.2.1 ClienteService
- **Métodos**:
  - `buscarClientes(String criterio): List<DtoPersona>`
  - `obtenerCliente(Integer id): DtoPersona`
  - `crearCliente(DtoPersona cliente): DtoPersona`
  - `actualizarCliente(DtoPersona cliente): DtoPersona`
  - `eliminarCliente(Integer id): boolean`
  - `buscarGestionesCliente(Integer idCliente): List<DtoGestion>`
- **Dependencias**: `PersonaController`

#### 3.2.2 PresupuestoService
- **Métodos**: CRUD + búsquedas especializadas
- **Dependencias**: `PresupuestoController` (✅ existe)

#### 3.2.3 GestionService
- **Métodos**: CRUD + operaciones de gestión
- **Dependencias**: `GestionController` (crear en Fase 2)

#### 3.2.4 ConceptoService, TramiteService, etc.
- Servicios para cada entidad del sistema

### 3.3 Manejo de Errores
- **ErrorHandler**: Clase centralizada para manejo de excepciones
- **Mensajes de error**: Traducción de errores HTTP a mensajes amigables
- **Logging**: Integración con log4j

---

## Fase 4: Migración de Formularios Swing

### 4.1 Componentes Base (Prioridad 1)
**Estado**: ⏳ Pendiente

#### 4.1.1 Login.java
- **Cambios necesarios**:
  - Reemplazar `AdministradorSesion.validarUsuario()` por `AuthService.login()`
  - Usar `SessionManager` para almacenar sesión
  - Manejar errores de conexión con backend
- **Pruebas**: Login exitoso, credenciales inválidas, backend no disponible

#### 4.1.2 Principal.java
- **Cambios necesarios**:
  - Mantener estructura actual (JDesktopPane)
  - Cargar usuario desde `SessionManager`
  - Manejar permisos según tipo de usuario
- **Pruebas**: Carga correcta, menús según permisos

### 4.2 Módulo Clientes (Prioridad 2)
**Estado**: ⏳ Pendiente

#### 4.2.1 Clientes.java (formulario principal)
- **Cambios**: Usar `ClienteService` en lugar de `ControllerNegocio`
- **Formularios hijos**:
  - `DarAltaPersona.java` → Usar `ClienteService.crearCliente()`
  - `ModificarCliente.java` → Usar `ClienteService.actualizarCliente()`
  - `BuscarCliente.java` → Usar `ClienteService.buscarClientes()`
  - `BuscarGestionesCliente.java` → Usar `ClienteService.buscarGestionesCliente()`
  - `ListarPersonas.java` → Usar `ClienteService.buscarClientes()`
  - `AdministrarCliente.java` → Combinar servicios

### 4.3 Módulo Presupuestos (Prioridad 3)
**Estado**: ⏳ Pendiente
- **9 formularios** a migrar
- Usar `PresupuestoService` (ya existe `PresupuestoController`)

### 4.4 Módulo Gestiones (Prioridad 4)
**Estado**: ⏳ Pendiente
- **25 formularios** a migrar
- Requiere `GestionController` (crear en Fase 2)

### 4.5 Módulo Protocolo (Prioridad 5)
**Estado**: ⏳ Pendiente
- **7 formularios** a migrar

### 4.6 Módulo Pagos (Prioridad 6)
**Estado**: ⏳ Pendiente
- **3 formularios** a migrar
- Usar `PagoController` (✅ existe)

### 4.7 Módulo Administración (Prioridad 7)
**Estado**: ⏳ Pendiente
- **40 formularios** a migrar
- Requiere múltiples controladores

---

## Fase 5: Configuración y Dependencias

### 5.1 Actualización de pom.xml
**Estado**: ⏳ Pendiente
- **Dependencias actuales**:
  - ✅ log4j
  - ✅ jcalendar
  - ✅ jackson-databind
- **Dependencias a agregar**:
  - ❌ Swing (incluido en JDK, no requiere dependencia)
  - ✅ Verificar compatibilidad de versiones

### 5.2 Configuración de API
**Estado**: ⏳ Pendiente
- **Archivo de configuración**: `application.properties` o `config.properties`
- **Propiedades**:
  - `api.base.url=http://localhost:8080/api/v1`
  - `api.connection.timeout=10000`
  - `api.read.timeout=10000`

### 5.3 Manejo de Recursos
**Estado**: ✅ Verificado
- Iconos: `/frontend-swing/src/main/resources/iconos/` (✅ existe)
- Log4j: `/frontend-swing/src/main/resources/log4j.properties` (✅ existe)

---

## Fase 6: Pruebas y Verificación

### 6.1 Pruebas Unitarias (TDD cuando sea posible)
**Estado**: ⏳ Pendiente
- **Servicios**: Pruebas con mocks de REST client
- **Formularios**: Pruebas de lógica de negocio (sin UI)

### 6.2 Pruebas de Integración
**Estado**: ⏳ Pendiente
- **Escenarios**:
  1. Backend no disponible → Mostrar mensaje de error
  2. Timeout de conexión → Manejo apropiado
  3. Respuestas HTTP de error → Mensajes amigables
  4. Flujos completos de usuario → End-to-end

### 6.3 Verificación de Servicios
**Estado**: ⏳ Pendiente
- **PostgreSQL**: Verificar que está corriendo
- **Backend API**: Verificar que responde en puerto 8080
- **Swagger UI**: Verificar documentación en `/swagger-ui.html`

### 6.4 Pruebas Funcionales por Módulo
**Estado**: ⏳ Pendiente
- Login y autenticación
- CRUD de Clientes
- CRUD de Presupuestos
- Operaciones de Gestiones
- Operaciones de Protocolo
- Operaciones de Pagos
- Configuración de Administración

---

## Fase 7: Optimización y Refactorización

### 7.1 Mejoras de Rendimiento
**Estado**: ⏳ Pendiente
- Caché de datos frecuentemente accedidos
- Lazy loading en tablas grandes
- Paginación en listados

### 7.2 Mejoras de UX
**Estado**: ⏳ Pendiente
- Indicadores de carga (progress bars)
- Mensajes de confirmación
- Validación en tiempo real

### 7.3 Documentación
**Estado**: ⏳ Pendiente
- Javadoc en servicios
- README de uso
- Guía de desarrollo

---

## Orden de Implementación Recomendado

### Sprint 1: Infraestructura Base
1. ✅ Crear `UsuarioController` y `PersonaController` en backend
2. ✅ Crear `AuthService` y `SessionManager` en frontend
3. ✅ Migrar `Login.java`
4. ✅ Migrar `Principal.java`
5. ✅ Pruebas de autenticación

### Sprint 2: Módulo Clientes
1. ✅ Crear `ClienteService`
2. ✅ Migrar `Clientes.java` y formularios hijos
3. ✅ Pruebas del módulo completo

### Sprint 3: Módulo Presupuestos
1. ✅ Crear `PresupuestoService`
2. ✅ Migrar formularios de presupuestos
3. ✅ Pruebas del módulo

### Sprint 4: Módulo Gestiones
1. ✅ Crear `GestionController` en backend
2. ✅ Crear `GestionService` en frontend
3. ✅ Migrar formularios de gestiones
4. ✅ Pruebas del módulo

### Sprint 5: Módulos Restantes
1. ✅ Protocolo
2. ✅ Pagos
3. ✅ Administración

### Sprint 6: Pruebas Finales y Ajustes
1. ✅ Pruebas end-to-end
2. ✅ Corrección de bugs
3. ✅ Optimizaciones
4. ✅ Documentación

---

## Riesgos y Mitigaciones

### Riesgo 1: Controladores Backend Faltantes
- **Mitigación**: Crear controladores faltantes en Fase 2 antes de migrar frontend

### Riesgo 2: Incompatibilidades de DTOs
- **Mitigación**: Verificar estructura de DTOs entre frontend y backend

### Riesgo 3: Lógica de Negocio Compleja
- **Mitigación**: Revisar `ControllerNegocio.java` y mapear métodos a servicios REST

### Riesgo 4: Rendimiento de REST vs JPA Directo
- **Mitigación**: Implementar caché y optimizar queries en backend

---

## Criterios de Éxito

✅ Los 3 servicios (PostgreSQL, Backend API, Frontend Swing) se levantan correctamente
✅ Login funciona con autenticación REST
✅ Todos los módulos principales son accesibles
✅ CRUD básico funciona en cada módulo
✅ Manejo de errores apropiado
✅ Separación clara de capas (presentación, negocio, datos)

---

## Notas Técnicas

### Patrones a Usar
- **Singleton**: `SessionManager`, servicios principales
- **Service Layer**: Capa de servicios entre GUI y REST
- **DTO**: Transferencia de datos entre capas
- **Factory**: Creación de servicios según configuración

### Convenciones
- Servicios: `{Entidad}Service.java`
- DTOs: `Dto{Entidad}.java`
- Formularios: Mantener nombres originales
- Paquetes: `com.licensis.notaire.{capa}.{modulo}`

---

**Última actualización**: 2024-12-21
**Estado general**: ⏳ Pendiente de implementación

