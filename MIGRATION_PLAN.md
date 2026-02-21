# Plan de Migración: Monolito → Microservicios + Kubernetes Local

## Estado de la Migración (21/02/2026)

### Resumen Ejecutivo

| Componente | Estado | Detalle |
|------------|--------|---------|
| **Backend API** | ~95% | Endpoints críticos listos; faltan 2-3 para casos específicos |
| **Swing Forms** | ~70% | ~30 formularios aún usan ControllerNegocio |
| **Reportes PDF** | ✅ Listo | ReporteController con 10 endpoints JasperReports |
| **Docker Compose** | ✅ Listo | postgres + backend + pgadmin |
| **Kubernetes** | ❌ Pendiente | No existe configuración K8s |
| **Tests E2E** | Parcial | Shell tests; JUnit domain tests |

---

## Lo que FALTA migrar

### 1. Formularios Swing pendientes (ControllerNegocio → REST)

**Clientes (6 formularios)** — ✅ Batch B completado
- [x] `Clientes.java` — `buscarPersonasClientes()` → usar `GET /personas/buscar?esCliente=true`
- [x] `BuscarCliente.java` — `buscarPersonaNombreApellido`, `asociarFkTipoIdentificacion`, `buscarPersonaTipoNumeroIdentificacion`, `listarTiposIdentificacion`
- [x] `DarAltaPersona.java` — `listarTiposIdentificacion`
- [x] `AdministrarCliente.java` — `asociarFkTipoIdentificacion`, `listarTiposIdentificacion`
- [x] `ListarPersonas.java` — `asociarFkTipoIdentificacion`, `buscarPersonaTipoNumeroIdentificacion`
- [x] `ModificarCliente.java` — ya parcialmente migrado; verificar completitud

**Usuarios (3 formularios)** — ✅ Batch A completado (21/02/2026)
- [x] `ActividadUsuario.java` — migrado a REST (`usuarioClient.findAll()`, `findFromPath("persona/{id}")`)
- [x] `DarAltaUsuario.java` — migrado (`usuarioClient.findFromPath("persona/{id}")` para verificar si persona tiene usuario)
- [x] `ListarPersonasUsuario.java` — migrado (`RestMapper.asociarFkTipoIdentificacion`, `personaClient.findAllByPath("buscar?...)`)

**Gestiones (6 formularios)**
- [ ] `ModificarGestion.java` — eliminar ControllerNegocio, usar REST
- [ ] `IniciarGestion.java` — eliminar ControllerNegocio; ya usa adminJpa REST
- [ ] `BuscarGestion.java` — `listarTiposIdentificacion`, `buscarPersonaNombreApellidoConGestion`, `asociarFkTipoIdentificacion`, `buscarPersonaTipoNumeroIdentificacionConGestion`
- [ ] `DetalleGestion.java` — eliminar ControllerNegocio
- [ ] `ListaGestionesCliente.java` — `obtenerEstadoActualDeGestion` → usar historial/estado-gestion
- [ ] `ListaEscrituras.java`, `BuscarEscritura.java`, `DetalleEscritura.java` — migrar a REST

**Testimonios (3 formularios)**
- [ ] `GenerarTestimonio.java`, `VerificarTestimonio.java`, `RetirarTestimonio.java` — migrar a REST (testimonio, movimiento-testimonio)

**Inscripciones (3 formularios)**
- [ ] `IngresarParaInscripcion.java`, `RegistrarInscripcion.java`, `RegistrarReingreso.java` — migrar a REST

**Presupuestos (4 formularios)**
- [ ] `CrearPresupuesto.java` — eliminar ControllerNegocio
- [ ] `DetalleValoresTramites.java`, `BuscarInmueble.java` — migrar a REST
- [ ] `AdministradorReportes.java` — conectar con endpoints `/api/v1/reportes/*`

**Protocolo (4 formularios)**
- [ ] `ModificarFolio.java`, `IngresarFolios.java` — migrar a REST (folio, tipo-folio)
- [ ] `GenerarIndices.java`, `GenerarDDJJ.java`, `DeclaracionJurada.java`, `DeclaracionJuradaRentas.java` — usar reportes API

**Plantillas Presupuesto (2 formularios)**
- [ ] `CrearPlantillaPresupuesto.java`, `ModificarPlantillaPresupuesto.java` — eliminar ControllerNegocio

**Auxiliares**
- [ ] `AdministradorValidaciones.java` — `isPasswordCorrect` → endpoint `POST /usuarios/validate-password`

---

### 2. APIs faltantes o por extender

| API faltante | Uso | Acción |
|--------------|-----|--------|
| `GET /personas/buscar?esCliente=true` | Clientes.java (personas con gestiones como clientes) | ✅ Agregar parámetro opcional `esCliente` en PersonaController |
| `GET /usuarios?nombre=X` | DarAltaUsuario, ActividadUsuario (buscar usuario por nombre) | Agregar query param en UsuarioController |
| `GET /gestiones/{id}/estado-actual` o `GET /historial/gestion/{id}` (último estado) | ListaGestionesCliente | Usar historial existente o agregar endpoint |
| `POST /usuarios/validate-password` | ModificarUsuario (cambiar contraseña) | Nuevo endpoint para validar contraseña sin exponer hash |

---

## Recomendaciones de implementación

### Migración Swing → REST (por formulario)

1. Identificar todas las llamadas a `ControllerNegocio.*` en el formulario
2. Mapear cada llamada a un endpoint REST existente o crearlo
3. Usar `AdministradorJpa.getInstancia().getXxxJpa()` y `GenericRestClient`
4. Usar `RestMapper.asociarFkTipoIdentificacion(nombre)` para tipos de identificación
5. Para búsquedas: usar `personaClient` con `findAll()` y filtrar, o preferir endpoints con query params
6. Reemplazar `ControllerNegocio` por código REST
7. Probar con `bash scripts/start.sh --no-frontend --no-admin` y cliente Swing local

### Orden sugerido de migración

1. **Batch A** ✅: Usuarios (`ActividadUsuario`, `DarAltaUsuario`, `ListarPersonasUsuario`) — COMPLETADO 21/02/2026
   - API agregada: `GET /api/v1/usuarios/persona/{idPersona}`
2. **Batch B** ✅: Clientes (`Clientes`, `BuscarCliente`, `DarAltaPersona`, `AdministrarCliente`, `ListarPersonas`) + API `personas/buscar?esCliente=true` — COMPLETADO
3. **Batch C**: Gestiones restantes (`BuscarGestion`, `DetalleGestion`, `ListaGestionesCliente`, `ModificarGestion`)
4. **Batch D**: Escrituras, Testimonios, Inscripciones
5. **Batch E**: Presupuestos, Protocolo, Plantillas
6. **Batch F**: `AdministradorReportes`, `AdministradorValidaciones`

---

## Despliegue en Kubernetes (Local)

### Objetivo

Poder ejecutar la aplicación completa en Kubernetes local (minikube, kind, Docker Desktop K8s) para validar el despliegue antes de cloud.

### Recomendaciones de implementación

#### 1. Estructura de manifests

```
k8s/
├── namespace.yaml
├── configmap.yaml
├── secrets.yaml
├── postgres/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── pvc.yaml
├── backend/
│   ├── deployment.yaml
│   └── service.yaml
└── ingress.yaml (opcional, para acceso externo)
```

#### 2. Base de datos PostgreSQL en K8s

- Usar `StatefulSet` o `Deployment` con `PersistentVolumeClaim` para datos
- Variables de entorno vía `ConfigMap` y `Secret`
- Health checks con `livenessProbe` / `readinessProbe`

#### 3. Backend Spring Boot

- Image: `eclipse-temurin:21-jre-alpine` o imagen propia desde `Dockerfile.backend`
- Variables: `SPRING_DATASOURCE_URL`, `USER`, `PASSWORD` desde Secret
- Probes: `GET /actuator/health`
- Resource limits: `requests` y `limits` para CPU/memoria

#### 4. Orden de arranque

- Usar `initContainers` en backend para esperar PostgreSQL
- O `depends_on` vía Jobs/scripts

#### 5. ConfigMap y Secrets de ejemplo

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: notaire-config
  namespace: notaire
data:
  POSTGRES_DB: "notaire"
  POSTGRES_USER: "notaire"
```

```yaml
# secrets.yaml (base64 para producción; para local se puede usar literales)
apiVersion: v1
kind: Secret
metadata:
  name: notaire-secrets
  namespace: notaire
type: Opaque
stringData:
  POSTGRES_PASSWORD: "notaire_password"
```

#### 6. Servicio backend

- `ClusterIP` para acceso interno
- `NodePort` o `LoadBalancer` si se necesita acceso desde host
- Para minikube: `minikube service notaire-backend --url`

#### 7. Frontend Swing (cliente de escritorio)

- El frontend Swing NO se despliega en K8s
- Se ejecuta en la máquina del usuario
- `ApiConfig` debe apuntar a la URL del backend expuesta (ej. `http://localhost:30080` vía NodePort o `kubectl port-forward`)

#### 8. Comandos útiles

```bash
# Crear namespace
kubectl apply -f k8s/namespace.yaml

# Aplicar todo
kubectl apply -f k8s/

# Port-forward para desarrollo
kubectl port-forward svc/notaire-backend 8080:8080 -n notaire

# Ver logs
kubectl logs -f deployment/notaire-backend -n notaire
```

#### 9. Alternativa: Helm

- Crear chart `notaire` con valores para DB y backend
- Facilita despliegue en distintos entornos (dev, staging)

### Checklist pre-Kubernetes

- [ ] Dockerfile.backend funcional y optimizado (multi-stage build)
- [ ] Variables de entorno externalizadas (no hardcode)
- [ ] Health endpoint `/actuator/health` respondiendo
- [ ] PostgreSQL con datos iniciales (init-db) o migraciones
- [ ] Tests E2E pasando contra backend en Docker

---

## Criterios de finalización

1. **0 usos** de `ControllerNegocio` en formularios Swing (excepto deprecación gradual)
2. **100%** formularios usando REST vía `AdministradorJpa` / `GenericRestClient`
3. **Todas** las APIs necesarias implementadas y documentadas en Swagger
4. **Stack** desplegable con `kubectl apply -f k8s/` en entorno local
5. **Tests** `bash scripts/test.sh` en verde

---

## Próximos pasos inmediatos

1. Implementar APIs faltantes (`personas/buscar?esCliente`, `usuarios?nombre`, `validate-password`)
2. Migrar Batch A (Usuarios) en Swing
3. Crear estructura base `k8s/` con Deployment de postgres y backend
4. Validar despliegue local con minikube o kind
