# README #

El proyecto Notaire es un sistema de administración para gestión de escribanía. Está desarrollado en Java, MySql y Apache.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###


* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact

# Guia de instalación #

1. Instalar el servidor WAMP, que se encuenta en la carpeta "WAMP".
2. Usando phpMyadmin, crear una base de datos con el nombre "notaire".
3. Importar base de datos "notaire.sql", que se encuentra en la carpeta "BBDD". NOTA: usar usuario "root", sin clave.
4. Instalar Java 1.7 JRE, que se encuenta en la carpeta "Java VM"
5. Copiar la carpeta "reportes" en c:\
6. Copiar el contenido de la carpeta "Ejecutable" en c:\notarire\
7. Ejecutar el archivo "notaire.jar" haciendo doble click ó en una consola: c:\java -jar notaire.jar
7. En el primer inicio, usar el usuario y contraseña: "root" - "admin" (usuario ADMINISTRADOR).


---

# Notaire - Docker Setup Guide

Este documento describe cómo ejecutar la aplicación Notaire con PostgreSQL en Docker.

## Prerequisitos

- Docker Desktop (o Docker Engine + Docker Compose)
- Java 8+ (para ejecutar la aplicación)
- Maven 3.x (para compilar)

## Inicio Rápido

### 1. Configurar Variables de Entorno

```bash
# Copiar el template de variables
cp .env.example .env

# Editar si es necesario (los valores por defecto funcionan)
nano .env
```

### 2. Iniciar PostgreSQL

```bash
# Iniciar solo PostgreSQL
docker-compose up -d postgres

# Verificar que está corriendo
docker-compose ps

# Ver logs
docker-compose logs -f postgres
```

### 3. Verificar la Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it notaire-postgres psql -U notaire -d notaire

# Listar tablas
\dt

# Contar tablas (esperado: 27)
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';

# Ver datos de ejemplo
SELECT * FROM conceptos;
SELECT * FROM usuarios;

# Salir
\q
```

### 4. Compilar la Aplicación

```bash
cd project/notaire
mvn clean package -DskipTests
```

### 5. Ejecutar la Aplicación

El proceso de build ahora genera un "Fat JAR" (Uber-JAR) que contiene todas las dependencias necesarias.

```bash
java -jar target/Notaire-1.0-SNAPSHOT.jar
```

**Credenciales de acceso:**
- Usuario: `admin`
- Contraseña: `admin`

> **Nota:** Al ser una aplicación Swing, requiere un entorno gráfico (Display). Si estás ejecutando esto en un servidor sin entorno gráfico, obtendrás una excepción `HeadlessException`.

---

## pgAdmin (Opcional)

Para gestión visual de la base de datos:

```bash
# Iniciar con perfil admin
docker-compose --profile admin up -d

# Acceder en el navegador
# URL: http://localhost:5050
# Email: admin@notaire.local
# Password: admin

# Agregar servidor PostgreSQL:
# - Host: postgres
# - Port: 5432
# - Database: notaire
# - Username: notaire
# - Password: notaire_password
```

---

## Comandos Útiles

### Docker

```bash
# Iniciar servicios
docker-compose up -d

# Detener servicios
docker-compose down

# Detener y eliminar datos
docker-compose down -v

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar PostgreSQL (mantiene datos)
docker-compose restart postgres

# Recrear base de datos desde cero
docker-compose down -v
docker-compose up -d postgres
```

### Base de Datos

```bash
# Backup de la base de datos
docker exec notaire-postgres pg_dump -U notaire notaire > backup.sql

# Restaurar backup
docker exec -i notaire-postgres psql -U notaire -d notaire < backup.sql

# Ejecutar SQL desde archivo
docker exec -i notaire-postgres psql -U notaire -d notaire < script.sql
```

### Aplicación

```bash
# Compilar con logs detallados
mvn clean package -DskipTests -X

# Ejecutar con logging debug
java -Dlog4j.debug -jar target/Notaire-1.0-SNAPSHOT.jar
```

---

## Troubleshooting

### Error: "Connection refused to localhost:5432"

**Causa:** PostgreSQL no está corriendo o no está listo.

```bash
# Verificar estado
docker-compose ps

# Ver logs
docker-compose logs postgres

# Reiniciar si es necesario
docker-compose restart postgres
```

### Error: "password authentication failed"

**Causa:** Credenciales incorrectas o base de datos no inicializada.

```bash
# Verificar credenciales en .env
cat .env

# Recrear base de datos
docker-compose down -v
docker-compose up -d postgres
```

### Error: "relation does not exist"

**Causa:** Las tablas no fueron creadas.

```bash
# Verificar si los scripts de init se ejecutaron
docker exec -it notaire-postgres psql -U notaire -d notaire -c "\dt"

# Si no hay tablas, reiniciar desde cero
docker-compose down -v
docker-compose up -d postgres
```

### La aplicación no compila

```bash
# Limpiar cache de Maven
mvn clean
rm -rf ~/.m2/repository/org/postgresql

# Volver a compilar
mvn package -DskipTests
```

### Puerto 5432 ya está en uso

```bash
# Ver qué proceso usa el puerto
sudo lsof -i :5432

# Opción 1: Detener el proceso
sudo kill <PID>

# Opción 2: Cambiar el puerto en docker-compose.yml
# Cambiar "5432:5432" a "5433:5432" y actualizar persistence.xml
```

---

## Estructura de Archivos Docker

```
notaire/
├── docker-compose.yml      # Configuración de servicios
├── .env.example            # Template de variables
├── .env                    # Variables (crear desde template)
├── init-db/
│   ├── 01-schema.sql       # Estructura de tablas
│   └── 02-data.sql         # Datos iniciales
└── migrate.load            # Script pgloader (alternativo)
```

---

## Migración desde MySQL Original

Si tienes datos en MySQL que quieres migrar:

### Opción 1: pgloader (Recomendado)

```bash
# Instalar pgloader
sudo apt-get install pgloader

# Editar migrate.load con tu URL MySQL
nano migrate.load

# Ejecutar migración
pgloader migrate.load
```

### Opción 2: Dump SQL Manual

```bash
# Exportar de MySQL
mysqldump -u root -p notaire > mysql_backup.sql

# Convertir a PostgreSQL (herramienta online o manual)
# Importar a PostgreSQL
docker exec -i notaire-postgres psql -U notaire -d notaire < postgres_backup.sql
```
