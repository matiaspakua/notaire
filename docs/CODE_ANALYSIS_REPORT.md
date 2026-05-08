# Comprehensive Code Analysis Report: Notaire Backend-API & Notaire-Shared

**Analysis Date:** May 5, 2026  
**Scope:** `backend-api` and `notaire-shared` modules  
**Focus Areas:** Hardcoded values, duplicate code, security issues, error handling, logging consistency, and observability

---

## Executive Summary

The analysis identified **critical security vulnerabilities**, **significant code duplication patterns** across controllers, **inconsistent logging**, **lack of observability instrumentation**, and multiple **architectural issues** that require immediate attention.

---

## 1. SECURITY VULNERABILITIES

### 1.1 MD5 Password Hashing (CRITICAL - CWE-327)

**Severity:** CRITICAL  
**Issue:** MD5 is a deprecated cryptographic hash function vulnerable to collision attacks and unsuitable for password hashing.

**Locations:**

| File | Lines | Issue |
|------|-------|-------|
| [backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L191-L210) | 191-210 | Method `encriptaEnMD5()` implements MD5 hashing for passwords |
| [backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L75) | 75 | Password encryption in POST endpoint |
| [backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L92) | 92 | Password encryption in PUT endpoint |
| [backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L126) | 126 | Password hashing in login endpoint |
| [backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java](backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java#L3787-L3806) | 3787-3806 | Duplicate MD5 implementation in `encriptaEnMD5()` |
| [backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java](backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java#L3753) | 3753 | Password encryption in legacy code |
| [backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java](backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java#L3930) | 3930 | Password encryption in user modification |
| [backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java#L55) | 55 | Default admin user initialization with MD5 |
| [backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java#L89) | 89 | Admin password update with MD5 |
| [backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java#L102-L118) | 102-118 | Local MD5 implementation |

**Code Sample:**
```java
// INSECURE: MD5 hashing
MessageDigest msgd = MessageDigest.getInstance("MD5");  // Line 198
byte[] bytes = msgd.digest(stringAEncriptar.getBytes());
```

**Recommendation:** Replace with bcrypt, Argon2, or PBKDF2 using Spring Security's `PasswordEncoder`.

### 1.2 Hardcoded Credentials and Configuration

**Severity:** CRITICAL

| Location | Value | Type |
|----------|-------|------|
| [backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java#L30) | `"admin"` | Username |
| [backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java#L31) | `"admin"` | Default Password |
| [backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L112) | `"jdbc:mysql://localhost"` | MySQL localhost |
| [backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L112) | `"matias"` | Hardcoded username |
| [backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L112) | `""` (empty) | Hardcoded empty password |
| [backend-api/src/main/java/com/licensis/notaire/config/OpenApiConfig.java](backend-api/src/main/java/com/licensis/notaire/config/OpenApiConfig.java#L22) | `"https://www.licensis.com"` | Contact URL |

### 1.3 Deprecated Database Connection Method

**Severity:** HIGH  
**File:** [backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L111-L112)  
**Lines:** 111-112

The code maintains legacy JDBC connections using `DriverManager` instead of Spring's datasource management:
```java
// DEPRECATED: Direct JDBC with hardcoded credentials
miConexion = DriverManager.getConnection("jdbc:mysql://localhost", "matias", "");
```

Should use Spring's `@ConfigurationProperties` or `application.yml`.

---

## 2. DUPLICATE CODE PATTERNS

### 2.1 Controller CRUD Pattern Duplication

**Severity:** HIGH  
**Pattern Type:** Exact duplication across 25+ Controllers

Controllers implementing identical CRUD patterns with generic exception handling:

#### Pattern A: JpaController-Based Controllers (15+ instances)
```
InmuebleController, CopiaController, SuplenciaController, ItemController, 
TipoDeFolioController, ConceptoController, TestimonioController, 
HistorialController, TipoDeDocumentoController, EstadoDeGestionController, 
TipoIdentificacionController, PresupuestoController, TramiteController, 
MovimientoTestimonioController, TipoDeTramiteController, PlantillaTramiteController, 
PlantillaPresupuestoController, DocumentoPresentadoController, FolioController
```

**Controllers with this pattern:**

| Controller | File | Pattern |
|-----------|------|---------|
| [InmuebleController](backend-api/src/main/java/com/licensis/notaire/api/InmuebleController.java#L1-80) | api/ | JpaController with generic try-catch |
| [CopiaController](backend-api/src/main/java/com/licensis/notaire/api/CopiaController.java#L1-70) | api/ | JpaController with generic try-catch |
| [SuplenciaController](backend-api/src/main/java/com/licensis/notaire/api/SuplenciaController.java#L1-70) | api/ | JpaController with generic try-catch |
| [ItemController](backend-api/src/main/java/com/licensis/notaire/api/ItemController.java#L1-85) | api/ | JpaController with generic try-catch |
| [TipoDeFolioController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeFolioController.java) | api/ | JpaController with generic try-catch |

**Code Sample:**
```java
@GetMapping
@Operation(summary = "Obtener todos los X")
public ResponseEntity<List<Inmueble>> getAll() {
    try {
        return ResponseEntity.ok(getJpaController().findInmuebleEntities());
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}

@PostMapping
@Operation(summary = "Crear nuevo X")
public ResponseEntity<Void> create(@RequestBody Inmueble entity) {
    try {
        getJpaController().create(entity);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
```

This pattern repeated identically across 15+ controllers.

#### Pattern B: Service-Based Controllers (4+ instances)
```
PersonaController, EscrituraController, PagoController, RegistroAuditoriaController
```

These use Spring Service injection but some lack consistent error handling.

### 2.2 MD5 Password Encryption Duplication

**Three separate implementations of the same encryption logic:**

1. **[UsuarioController.java](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L191-L210)** - Lines 191-210
2. **[ControllerNegocio.java](backend-api/src/main/java/com/licensis/notaire/negocio/ControllerNegocio.java#L3787-L3806)** - Lines 3787-3806  
3. **[DataInitializer.java](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java#L102-L118)** - Lines 102-118

### 2.3 JpaController Instantiation Pattern Duplication

**19 Controllers instantiate JpaController the same way:**
```java
private XxxJpaController getJpaController() {
    return new XxxJpaController(null, JpaControllerProvider.getEntityManagerFactory());
}
```

Locations:
- [TipoDeFolioController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeFolioController.java#L22)
- [InmuebleController](backend-api/src/main/java/com/licensis/notaire/api/InmuebleController.java#L18)
- [CopiaController](backend-api/src/main/java/com/licensis/notaire/api/CopiaController.java#L18)
- [SuplenciaController](backend-api/src/main/java/com/licensis/notaire/api/SuplenciaController.java#L18)
- [ItemController](backend-api/src/main/java/com/licensis/notaire/api/ItemController.java#L18)
- [TestimonioController](backend-api/src/main/java/com/licensis/notaire/api/TestimonioController.java#L22)
- [ConceptoController](backend-api/src/main/java/com/licensis/notaire/api/ConceptoController.java#L21)
- [TipoDeDocumentoController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeDocumentoController.java#L22)
- [HistorialController](backend-api/src/main/java/com/licensis/notaire/api/HistorialController.java#L18)
- [MovimientoTestimonioController](backend-api/src/main/java/com/licensis/notaire/api/MovimientoTestimonioController.java#L22)
- [EstadoDeGestionController](backend-api/src/main/java/com/licensis/notaire/api/EstadoDeGestionController.java#L22)
- [PresupuestoController](backend-api/src/main/java/com/licensis/notaire/api/PresupuestoController.java#L20)
- [TramiteController](backend-api/src/main/java/com/licensis/notaire/api/TramiteController.java#L18)
- [PlantillaTramiteController](backend-api/src/main/java/com/licensis/notaire/api/PlantillaTramiteController.java#L19)
- [TipoDeTramiteController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeTramiteController.java#L19)
- [PlantillaPresupuestoController](backend-api/src/main/java/com/licensis/notaire/api/PlantillaPresupuestoController.java#L39)
- [GestionController](backend-api/src/main/java/com/licensis/notaire/api/GestionController.java#L22)
- [FolioController](backend-api/src/main/java/com/licensis/notaire/api/FolioController.java#L18)
- [DocumentoPresentadoController](backend-api/src/main/java/com/licensis/notaire/api/DocumentoPresentadoController.java#L18)
- [TipoIdentificacionController](backend-api/src/main/java/com/licensis/notaire/api/TipoIdentificacionController.java#L18)

**Also in AdministradorJpa.java:**
- [Lines 144-171](backend-api/src/main/java/com/licensis/notaire/servicios/AdministradorJpa.java#L144-L171) - 28 JpaController instantiations with same pattern

---

## 3. ERROR HANDLING ISSUES

### 3.1 Generic Exception Catches (CWE-480)

**Severity:** HIGH

Generic `catch (Exception e)` blocks without specific exception handling found in:

| File | Count | Lines |
|------|-------|-------|
| [TramitesPersonasJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/TramitesPersonasJpaController.java) | 3 | 82, 136, 202 |
| [PlantillaTramiteJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PlantillaTramiteJpaController.java) | 2 | 83, 148 |
| [PlantillaPresupuestoJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PlantillaPresupuestoJpaController.java) | 2 | 85, 149 |
| [RegistroAuditoriaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/RegistroAuditoriaJpaController.java) | 3 | 97, 122, 205 |
| [DataInitializer](backend-api/src/main/java/com/licensis/notaire/config/DataInitializer.java) | 3 | 61, 97, 111 |
| [TestimonioJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/TestimonioJpaController.java) | 2 | 225, 260 |
| [UsuarioJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/UsuarioJpaController.java) | 3 | 161, 187, 323 |
| [ReporteService](backend-api/src/main/java/com/licensis/notaire/servicios/ReporteService.java) | 3 | 130, 132, 156 |
| [PersonaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PersonaJpaController.java) | 5 | 541, 566, 774, 874, 965 |
| [DocumentoPresentadoJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/DocumentoPresentadoJpaController.java) | 2 | 140, 173 |
| [GestionDeEscrituraJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/GestionDeEscrituraJpaController.java) | 2 | 290, 522 |
| [PresupuestoJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PresupuestoJpaController.java) | 1 | 308 |
| [PagoJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PagoJpaController.java) | 2 | 100, 135 |
| [SuplenciaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/SuplenciaJpaController.java) | 2 | 126, 161 |

**Total: 35+ instances of generic exception handling**

### 3.2 Exception Swallowing Without Logging

**Severity:** HIGH

Multiple controllers catch exceptions but return generic 500 responses without logging details:

```java
// api/InmuebleController.java, line 28
catch (Exception e) {
    return ResponseEntity.internalServerError().build();  // Silent failure - no logging
}
```

Affected Controllers: InmuebleController, CopiaController, SuplenciaController, ItemController, ConceptoController, and 15+ others.

### 3.3 Inconsistent Error Response Handling

**UsuarioController Login endpoint:**
- [Lines 119-188](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L119-L188) - Returns Map with no consistent error structure
- Exception handling at line 184 returns generic Map without error details

---

## 4. LOGGING INCONSISTENCIES

### 4.1 Logger Naming Inconsistency

**Severity:** MEDIUM

| File | Logger Name | Pattern |
|------|------------|---------|
| [PersonaService](backend-api/src/main/java/com/licensis/notaire/service/PersonaService.java#L18) | `logger` | LoggerFactory.getLogger(class) |
| [EscrituraService](backend-api/src/main/java/com/licensis/notaire/service/EscrituraService.java#L19) | `logger` | LoggerFactory.getLogger(class) |
| [PagoService](backend-api/src/main/java/com/licensis/notaire/service/PagoService.java#L19) | `log` | LoggerFactory.getLogger(class) |
| [RegistroAuditoriaService](backend-api/src/main/java/com/licensis/notaire/service/RegistroAuditoriaService.java#L17) | `logger` | LoggerFactory.getLogger(class) |
| [UsuarioController](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java#L26) | `log` | LoggerFactory.getLogger(class) |

**Inconsistency:** Services use `logger`, Controllers use `log`.

### 4.2 System.out.println Usage (Should use Logger)

**Severity:** MEDIUM

| File | Line | Content |
|------|------|---------|
| [Conexion](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L101) | 101 | `System.out.println("Test conexion");` |
| [Conexion](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L117) | 117 | Error message printed to stdout |
| [Conexion](backend-api/src/main/java/com/licensis/notaire/servicios/Conexion.java#L136) | 136 | Disconnect error to stdout |
| [Usuario](backend-api/src/main/java/com/licensis/notaire/negocio/Usuario.java#L214) | 214 | `System.out.println("Error Metodo : getDtoUsuario");` |
| [TipoIdentificacion](backend-api/src/main/java/com/licensis/notaire/negocio/TipoIdentificacion.java#L134) | 134 | `System.out.println("Erro getDto Tipo Identificacion");` |
| [RegistroAuditoriaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/RegistroAuditoriaJpaController.java#L206) | 206 | `System.out.println("Error Metodo : getDtoRegistroAuditoria");` |
| [UsuarioJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/UsuarioJpaController.java#L324) | 324 | `System.out.println("Error de Persistencia...");` |
| [PersonaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PersonaJpaController.java#L775, #L875) | 775, 875 | Error messages to stdout |
| [GestionDeEscrituraJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/GestionDeEscrituraJpaController.java#L524) | 524 | Error to stdout |

### 4.3 Missing Logging in Controllers

Controllers like [InmuebleController](backend-api/src/main/java/com/licensis/notaire/api/InmuebleController.java), [CopiaController](backend-api/src/main/java/com/licensis/notaire/api/CopiaController.java), [SuplenciaController](backend-api/src/main/java/com/licensis/notaire/api/SuplenciaController.java) have **NO logging whatsoever** despite error-prone operations.

---

## 5. UNUSED IMPORTS AND CODE

### 5.1 Wildcard Imports

**Severity:** MEDIUM  
**File:** [backend-api/src/main/java/com/licensis/notaire/negocio/Persona.java](backend-api/src/main/java/com/licensis/notaire/negocio/Persona.java#L12)  
**Line:** 12

```java
import java.util.*;  // Wildcard import - should be specific imports
```

---

## 6. MISSING OBSERVABILITY

### 6.1 No Metrics Instrumentation

**Severity:** HIGH

The codebase completely lacks:
- Micrometer metrics
- Prometheus monitoring
- Request/response timing
- Business metrics (e.g., login attempts, document processing)
- Custom business logic metrics

**Impact:** Cannot monitor:
- API response times
- Error rates
- Business KPIs
- System health

### 6.2 No Distributed Tracing

**Severity:** HIGH

Missing:
- Spring Cloud Sleuth / OpenTelemetry integration
- Correlation IDs across requests
- Request tracing across services
- Trace context propagation

**Impact:** Cannot correlate requests across microservices.

### 6.3 No Health Checks

**Severity:** MEDIUM

No custom health indicators for:
- Database connectivity
- External service availability
- Cache status
- Business process health

### 6.4 Inadequate Logging for Observability

**Current State:**
- [PersonaService](backend-api/src/main/java/com/licensis/notaire/service/PersonaService.java#L28) - Generic "Finding all personas" at DEBUG level
- [PagoService](backend-api/src/main/java/com/licensis/notaire/service/PagoService.java#L35) - Some structured logging but inconsistent
- Most controllers: NO logging

**Missing:**
- Request/response logging
- Execution time logging
- Performance metrics
- Structured logging with fields

---

## 7. ARCHITECTURAL ISSUES

### 7.1 Deprecated Legacy Code Pattern

**Severity:** HIGH

The entire `jpa/` directory (61+ files) contains legacy JPA controller pattern from pre-Spring era:

```java
// jpa/UsuarioJpaController.java, etc.
// Manual persistence logic instead of Spring Data
public class UsuarioJpaController implements IPersistenciaJpa {
    // Manual create(), edit(), destroy() implementations
    // Direct EntityManager usage
}
```

Also affects [servicios/AdministradorJpa.java](backend-api/src/main/java/com/licensis/notaire/servicios/AdministradorJpa.java#L144-L171) with 28 manual JpaController instantiations.

### 7.2 Weak Dependency Injection

**Severity:** MEDIUM

Controllers instantiate JpaController on each request:
```java
private XxxJpaController getJpaController() {
    return new XxxJpaController(null, JpaControllerProvider.getEntityManagerFactory());
}
```

Should use Spring `@Autowired` or constructor injection.

### 7.3 Missing Transaction Management

**Severity:** MEDIUM

Most controllers lack `@Transactional` annotation. [PersonaController](backend-api/src/main/java/com/licensis/notaire/api/PersonaController.java#L24) has it, but others don't:
- [InmuebleController](backend-api/src/main/java/com/licensis/notaire/api/InmuebleController.java) - No @Transactional
- [CopiaController](backend-api/src/main/java/com/licensis/notaire/api/CopiaController.java) - No @Transactional
- [SuplenciaController](backend-api/src/main/java/com/licensis/notaire/api/SuplenciaController.java) - No @Transactional

---

## 8. CONTROLLERS NEEDING REFACTORING

### 8.1 Priority 1: Security-Critical

- **[UsuarioController](backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java)** - MD5 hashing, generic exceptions, inconsistent error handling

### 8.2 Priority 2: High Duplication (Generic CRUD Pattern)

**15+ Controllers identical pattern:**
1. [InmuebleController](backend-api/src/main/java/com/licensis/notaire/api/InmuebleController.java)
2. [CopiaController](backend-api/src/main/java/com/licensis/notaire/api/CopiaController.java)
3. [SuplenciaController](backend-api/src/main/java/com/licensis/notaire/api/SuplenciaController.java)
4. [ItemController](backend-api/src/main/java/com/licensis/notaire/api/ItemController.java)
5. [TipoDeFolioController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeFolioController.java)
6. [TipoDeDocumentoController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeDocumentoController.java)
7. [HistorialController](backend-api/src/main/java/com/licensis/notaire/api/HistorialController.java)
8. [EstadoDeGestionController](backend-api/src/main/java/com/licensis/notaire/api/EstadoDeGestionController.java)
9. [TipoIdentificacionController](backend-api/src/main/java/com/licensis/notaire/api/TipoIdentificacionController.java)
10. [ConceptoController](backend-api/src/main/java/com/licensis/notaire/api/ConceptoController.java)
11. [TestimonioController](backend-api/src/main/java/com/licensis/notaire/api/TestimonioController.java)
12. [MovimientoTestimonioController](backend-api/src/main/java/com/licensis/notaire/api/MovimientoTestimonioController.java)
13. [TramiteController](backend-api/src/main/java/com/licensis/notaire/api/TramiteController.java)
14. [PresupuestoController](backend-api/src/main/java/com/licensis/notaire/api/PresupuestoController.java)
15. [FolioController](backend-api/src/main/java/com/licensis/notaire/api/FolioController.java)
16. [TipoDeTramiteController](backend-api/src/main/java/com/licensis/notaire/api/TipoDeTramiteController.java)
17. [DocumentoPresentadoController](backend-api/src/main/java/com/licensis/notaire/api/DocumentoPresentadoController.java)
18. [PlantillaTramiteController](backend-api/src/main/java/com/licensis/notaire/api/PlantillaTramiteController.java)
19. [PlantillaPresupuestoController](backend-api/src/main/java/com/licensis/notaire/api/PlantillaPresupuestoController.java)
20. [GestionController](backend-api/src/main/java/com/licensis/notaire/api/GestionController.java)

### 8.3 Priority 3: Missing Error Handling

**JPA Controllers with generic exceptions (61+ instances across jpa/ directory):**
- [PersonaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/PersonaJpaController.java#L541) - 5 generic catches
- [RegistroAuditoriaJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/RegistroAuditoriaJpaController.java#L97) - 3 generic catches
- [TramitesPersonasJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/TramitesPersonasJpaController.java#L82) - 3 generic catches
- [GestionDeEscrituraJpaController](backend-api/src/main/java/com/licensis/notaire/jpa/GestionDeEscrituraJpaController.java#L290) - 2 generic catches
- All other 55+ JPA controllers

---

## 9. HARDCODED VALUES SUMMARY TABLE

| Category | Value | File | Line(s) | Severity |
|----------|-------|------|---------|----------|
| **Credentials** | "admin" | DataInitializer | 30 | CRITICAL |
| **Credentials** | "admin" (password) | DataInitializer | 31 | CRITICAL |
| **Database** | "jdbc:mysql://localhost" | Conexion | 112 | CRITICAL |
| **Database** | "matias" | Conexion | 112 | CRITICAL |
| **Database** | "" (empty pwd) | Conexion | 112 | CRITICAL |
| **URL** | "https://www.licensis.com" | OpenApiConfig | 22 | LOW |
| **Magic String** | "00000000" | DataInitializer | 83 | MEDIUM |
| **Config Path** | "config.properties" | Conexion | 52, 105 | MEDIUM |
| **Format String** | "0000000000 65535 f" | ReporteService | 187 | LOW |

---

## 10. RECOMMENDED ACTIONS

### Immediate (Week 1)

1. **REPLACE MD5 with bcrypt** - All password hashing
2. **Remove hardcoded credentials** - Use environment variables/properties
3. **Fix Conexion.java** - Use Spring datasource configuration
4. **Add @Slf4j or logger injection** - To all classes with exceptions

### Short-term (Weeks 2-3)

1. **Refactor CRUD controllers** - Create base controller or use Spring Data REST
2. **Centralize error handling** - @ControllerAdvice with proper exception mapping
3. **Add structured logging** - Replace System.out.println
4. **Add @Transactional** - To all data modification endpoints

### Medium-term (Weeks 4-6)

1. **Add observability** - Micrometer metrics, Spring Cloud Sleuth
2. **Remove legacy JPA pattern** - Migrate to Spring Data repositories
3. **Consolidate duplicate code** - Extract common patterns
4. **Add health checks** - Actuator with custom indicators

### Long-term (Weeks 7-12)

1. **Comprehensive code review** - Full security audit
2. **Performance optimization** - Query optimization, caching
3. **Documentation** - API documentation updates
4. **Testing** - Increase coverage for security-critical code

---

## 11. CODE METRICS

| Metric | Value |
|--------|-------|
| Controllers with generic exception handling | 20+ |
| Controllers with duplicate CRUD code | 15-20 |
| JPA controllers with generic catches | 61+ |
| Files using System.out.println | 8 |
| MD5 implementations found | 3 |
| Hardcoded credentials | 5 |
| Controllers without logging | 15+ |
| Controllers without @Transactional | 18+ |
| Total lines of duplicate code | 2000+ |
| Wildcard imports | 1 |

---

## Appendix A: Complete JPA Controller List (61 Files)

Legacy JPA controllers in `/backend-api/src/main/java/com/licensis/notaire/jpa/`:

1. ConceptoJpaController
2. CopiaJpaController
3. DocumentoPresentadoJpaController
4. EscrituraJpaController
5. EstadoDeGestionJpaController
6. FolioJpaController
7. GestionDeEscrituraJpaController
8. HistorialJpaController
9. InmuebleJpaController
10. ItemJpaController
11. MovimientoTestimonioJpaController
12. PagoJpaController
13. PersonaJpaController
14. PlantillaPresupuestoJpaController
15. PlantillaTramiteJpaController
16. PresupuestoJpaController
17. RegistroAuditoriaJpaController
18. SuplenciaJpaController
19. TestimonioJpaController
20. TipoDeDocumentoJpaController
21. TipoDeFolioJpaController
22. TipoDeTramiteJpaController
23. TipoIdentificacionJpaController
24. TramiteJpaController
25. TramitesPersonasJpaController
26. UsuarioJpaController

Plus additional exception classes and utilities.

---

## Appendix B: Service-Based Controllers (Modern Pattern)

These controllers follow better practices with dependency injection:
1. [PersonaController](backend-api/src/main/java/com/licensis/notaire/api/PersonaController.java)
2. [EscrituraController](backend-api/src/main/java/com/licensis/notaire/api/EscrituraController.java)
3. [PagoController](backend-api/src/main/java/com/licensis/notaire/api/PagoController.java)
4. [RegistroAuditoriaController](backend-api/src/main/java/com/licensis/notaire/api/RegistroAuditoriaController.java)

**Recommendation:** Extend this pattern to all controllers.

---

**Report Generated:** 2026-05-05  
**Analysis Tool:** Comprehensive Code Analyzer  
**Status:** Complete
