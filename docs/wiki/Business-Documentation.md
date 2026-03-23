# Documentación de Negocio

## Estructura de Documentos

La documentación de negocio está organizada en `docs/business/`:

```
docs/business/
├── 00_Cronograma_Proyecto/     # Cronogramas del proyecto
├── 01_RF - Requerimientos Funcionales/   # SRS y requerimientos
├── 02_IA - Identificación de Actores/     # Actores del sistema
├── 03_CU - Casos de Uso/                # Casos de uso
├── 04 _MD - Modelo de Datos/             # Modelo de datos
├── 05_PS - Progreso Sistema/            # Reportes de progreso
├── 06_Manuales_del_producto/           # Manuales de usuario
├── 07_EA - Proyecto Enterprise Architect/ # Diagramas EA
├── 08_Aplicacion/                      # Documentación de la app
└── 09_templates/                       # Plantillas
```

## Requerimientos Funcionales

Ubicación: `01_RF - Requerimientos Funcionales/`

| Archivo | Descripción |
|---------|-------------|
| `RS - Relevamiento del Sistema.md` | Levantamiento de requerimientos |
| `SRS - Especificacion de Requerimientos.md` | Especificación formal |
| `requerimientos.csv` | Lista de requerimientos con IDs de GitHub |
| `labels.md` | Estrategia de labels para issues |

### Categorías de Requerimientos

- **RF-01 a RF-67**: Requerimientos funcionales originales
- **RF-68 a RF-95**: Requerimientos adicionales del relevamiento
- **RNF-01 a RNF-25**: Requerimientos no funcionales

## Modelo de Datos

Ubicación: `04_MD - Modelo de Datos/`

Contiene:
- Diagrama ER
- Esquema de base de datos
- Definición de entidades

## Casos de Uso

Ubicación: `03_CU - Casos de Uso/`

Contiene:
- Diagramas de casos de uso
- Especificaciones de cada caso de uso
- Flujos principales y alternativos

## Actores del Sistema

Ubicación: `02_IA - Identificación de Actores/`

Identifica:
- Escribano titular
- Escribano suplente
- Recepcionista/Secretaria
- Gestora
- Administrador del sistema
