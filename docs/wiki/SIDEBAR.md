# Wiki del Proyecto Notaire

Bienvenido a la wiki del proyecto Notaire. Esta wiki documenta el proceso de desarrollo, arquitectura y estructura del proyecto.

## Navegación

| Página | Descripción |
|--------|-------------|
| [Home](Home) | Página principal del proceso de desarrollo |
| [Business Documentation](Business-Documentation) | Documentación de negocio y requerimientos |
| [Refactoring Plan](Refactoring-Plan) | Plan de refactoring del monolito |
| [Development Setup](Development-Setup) | Configuración del ambiente de desarrollo |
| [DevSecOps Pipeline](DevSecOps-Pipeline) | Pipeline CI/CD y seguridad |

## Agregar Páginas a GitHub Wiki

### Método 1: Git Clone

```bash
# Clonar el wiki repository
git clone https://github.com/matiaspakua/notaire.wiki.git

# Agregar archivos
cp *.md notaire.wiki/

# Commit y push
cd notaire.wiki
git add .
git commit -m "Add wiki pages"
git push
```

### Método 2: GitHub Web Interface

1. Ir a https://github.com/matiaspakua/notaire/wiki
2. Click en "New Page"
3. Copiar el contenido del archivo `.md` correspondiente
4. Nombrar la página (sin extensión .md)

## Archivos Disponibles en `docs/wiki/`

| Archivo | Título en Wiki |
|---------|---------------|
| `Home.md` | Home |
| `Business-Documentation.md` | Business Documentation |
| `Refactoring-Plan.md` | Refactoring Plan |
| `Development-Setup.md` | Development Setup |

## Configurar Wiki en GitHub

Si el wiki no está habilitado:

1. Ir a Settings del repositorio
2. Buscar "Features"
3. Marcar "Wikis"
4. Save

## Contribuir a la Wiki

1. Crear branch desde wiki repo
2. Editar o agregar archivos markdown
3. Crear Pull Request
4. Merge para publicar cambios
