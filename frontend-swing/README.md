# Frontend Swing - Notaire

`frontend-swing` es la aplicación cliente de escritorio en Java Swing que consume la API REST de Notaire.

- Interfaz de usuario basada en Swing
- Consume `backend-api` a través de REST
- No accede directamente a la base de datos

## Ejecutar

```bash
cd frontend-swing
mvn clean package
java -jar target/frontend-swing-*.jar
```

## Documentación relevante

- `/docs/03-development/` — setup, build y pruebas
- `/docs/05-api/` — especificación de endpoints

## Nota

La aplicación requiere que el backend esté disponible en `http://localhost:8080`.
