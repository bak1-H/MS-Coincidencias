# MS-Coincidencias - Microservicio de Gestión de Coincidencias

## Descripción General

MS-Coincidencias es un microservicio escalable de la arquitectura SanoSys Salvos que gestiona la identificación y seguimiento de coincidencias en reportes del sistema. Implementa una arquitectura moderna basada en patrones de diseño reconocidos e integración asíncrona mediante colas de mensajería (RabbitMQ).

---

## Patrones de Diseño Implementados

### 1. **Patrón MVC (Model-View-Controller)**
- **Model**: `coincidenciaModel.java`, `reporteModel.java` - Entidades JPA
- **Controller**: `CoincidenciaController.java` - Endpoints REST
- **Service**: `coincidenciaService.java` - Lógica de negocio

### 2. **Patrón Repository**
- `repositoryCoincidencia.java`
- `reporteRepository.java`
- Implementa abstracción de acceso a datos usando JPA/Hibernate

### 3. **Patrón DTO (Data Transfer Object)**
- `NotificacionDTO.java`
- `ReporteDTO.java`
- Separa la representación de datos de transferencia de las entidades internas

### 4. **Patrón Service Locator / Inyección de Dependencias**
- Spring Framework para gestión de dependencias
- Configuración centralizda en `RabbitConfig.java`

### 5. **Patrón Publisher/Subscriber (Event-Driven)**
- `NotificacionPublisher.java` - Publica mensajes a RabbitMQ
- Listeners de eventos asincronos en `RabbitConfig.java`
- Desacoplamiento entre servicios mediante colas

### 6. **Patrón Configuration**
- `RabbitConfig.java` - Configuración centralizada de RabbitMQ
- Beans de configuración para convertidores de mensajes (Jackson)

---

## Stack Tecnológico

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 4.0.6 | Framework de aplicación |
| **Spring Data JPA** | - | Acceso a datos (ORM) |
| **Spring AMQP** | - | Integración con RabbitMQ |
| **PostgreSQL** | - | Base de datos relacional |
| **RabbitMQ** | - | Message Broker para comunicación asíncrona |
| **Lombok** | - | Reducción de boilerplate |
| **SpringDoc OpenAPI** | 3.0.2 | Documentación automática de API (Swagger) |
| **Maven** | 3.x | Gestor de dependencias y construcción |

---

## Requisitos Previos

- **Java 21** (JDK)
- **Maven 3.8.1+**
- **Docker** (para PostgreSQL y RabbitMQ)
- **Docker Compose** (para orquestar contenedores)
- **Git**

---

## Estructura del Proyecto

```
MS-Coincidencias/
├── src/main/java/com/sanosysalvos/coincidencias/
│   ├── CoincidenciasApplication.java              # Punto de entrada
│   ├── controller/CoincidenciaController.java     # Endpoints REST
│   ├── config/RabbitConfig.java                   # Configuración RabbitMQ
│   ├── messaging/NotificacionPublisher.java       # Publisher de eventos
│   ├── model/
│   │   ├── coincidenciaModel.java                 # Entidad Coincidencia
│   │   ├── reporteModel.java                      # Entidad Reporte
│   │   └── DTO/                                   # Objetos de transferencia
│   ├── repository/                                # Acceso a datos
│   └── services/coincidenciaService.java          # Lógica de negocio
├── src/main/resources/application.properties      # Configuración
├── pom.xml                                        # Dependencias Maven
├── Dockerfile                                     # Imagen Docker
└── docker-compose.yml                             # Orquestación
```

---

## Configuración del Entorno

### 1. Variables de Entorno (archivo `.env`)

Crear un archivo `.env` en la raíz del proyecto:

```properties
# Base de Datos PostgreSQL
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=fullstack_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Aplicación
SERVER_PORT=8082
```

### 2. Configuración de application.properties

El archivo `src/main/resources/application.properties` contiene:

```properties
spring.application.name=coincidencias
server.port=8082

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/fullstack_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Logging
logging.level.cl.sanosysalvos.reporte=DEBUG
```

---

## Instrucciones de Ejecución

### Opción 1: Ejecución Local con Docker

#### Paso 1: Iniciar Servicios Externos

```bash
# Iniciar PostgreSQL y RabbitMQ
docker-compose up -d

# Verificar que los servicios estén corriendo
docker-compose ps
```

#### Paso 2: Compilar el Proyecto

```bash
mvn clean install
```

#### Paso 3: Ejecutar la Aplicación

```bash
# Opción A: Desde Maven
mvn spring-boot:run

# Opción B: Desde JAR
mvn clean package
java -jar target/coincidencias-0.0.1-SNAPSHOT.jar
```

#### Paso 4: Verificar que la Aplicación está Corriendo

```bash
# La aplicación estará disponible en:
# http://localhost:8082

# Swagger UI (Documentación de API):
# http://localhost:8082/swagger-ui.html
```

---

### Opción 2: Ejecución Completa con Docker Compose

Levanta los tres servicios (PostgreSQL, RabbitMQ y la aplicación) en una sola red orquestada:

```bash
# Construir imagen y levantar el stack completo
docker compose up --build -d

# Verificar estado y health de cada contenedor
docker compose ps

# Ver logs de la aplicación
docker compose logs -f coincidencias-service

# Detener y eliminar contenedores (conserva volúmenes)
docker compose down

# Detener y eliminar todo, incluyendo volúmenes
docker compose down -v
```

El `docker-compose.yml` orquesta tres servicios con:
- **Health checks** en todos los servicios
- **Límites de recursos** (CPU y memoria) por contenedor
- **Dependencias ordenadas**: la app espera que PostgreSQL y RabbitMQ estén `healthy` antes de iniciar
- **Reinicio automático** (`restart: unless-stopped`) ante fallos

---

## Pruebas y Validación

### Pruebas Básicas con cURL

```bash
# Obtener todas las coincidencias
curl -X GET "http://localhost:8082/api/coincidencias"

# Obtener coincidencia por ID
curl -X GET "http://localhost:8082/api/coincidencias/1"

# Crear nueva coincidencia
curl -X POST "http://localhost:8082/api/coincidencias" \
  -H "Content-Type: application/json" \
  -d '{"descripcion":"Test","estado":"PENDIENTE"}'

# Actualizar coincidencia
curl -X PUT "http://localhost:8082/api/coincidencias/1" \
  -H "Content-Type: application/json" \
  -d '{"descripcion":"Actualizado","estado":"PROCESADA"}'

# Eliminar coincidencia
curl -X DELETE "http://localhost:8082/api/coincidencias/1"
```

### Verificación de Servicios

```bash
# RabbitMQ Management UI
# http://localhost:15672 (usuario: guest, contraseña: guest)

# PostgreSQL
docker exec -it ms-coincidencias-postgres psql -U postgres -d fullstack_db -c "SELECT * FROM coincidencia;"
```

---

### Endpoints Principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/coincidencias` | Obtener todas las coincidencias |
| GET | `/api/coincidencias/{id}` | Obtener coincidencia por ID |
| POST | `/api/coincidencias` | Crear nueva coincidencia |
| PUT | `/api/coincidencias/{id}` | Actualizar coincidencia |
| DELETE | `/api/coincidencias/{id}` | Eliminar coincidencia |
| GET | `/api/reportes` | Obtener todos los reportes |

---

## Troubleshooting y Debugging

### Problema: Port ya está en uso

```bash
# Windows - Encontrar qué proceso usa el puerto 8082
netstat -ano | findstr :8082

# Cambiar el puerto en application.properties
server.port=8083
```

### Problema: PostgreSQL no conecta

```bash
# Verificar estado del contenedor
docker ps | grep postgres

# Reiniciar PostgreSQL
docker-compose restart postgres

# Ver logs
docker logs ms-coincidencias-postgres
```

### Problema: RabbitMQ no conecta

```bash
# Acceder a Management UI: http://localhost:15672
# Usuario: guest, Contraseña: guest

# Reiniciar RabbitMQ
docker-compose restart rabbitmq
```

### Habilitar Logs de Debug

En `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.sanosysalvos=DEBUG
logging.level.org.springframework.web=DEBUG
```

Ver logs en tiempo real:
```bash
# Docker
docker logs -f coincidencias-service

# Local
# Los logs se mostrarán en la consola del terminal
```

---

## Pipeline CI/CD

El proyecto implementa un pipeline completo de integración y entrega continua mediante **GitHub Actions** con cinco etapas secuenciales:

```
push / PR
    │
    ▼
[1] build          → Compila el proyecto con Maven
    │
    ├──────────────────────────┐
    ▼                          ▼
[2] test           [3] security-scan
    Ejecuta 19         Snyk analiza dependencias
    tests unitarios    BLOQUEA en HIGH/CRITICAL
    │                          │
    └──────────────────────────┘
                  │
                  ▼
          [4] docker-build
              Construye imagen multi-stage
              Push a ghcr.io con tags:
              - branch name
              - sha-{commit}
              - latest (solo en main/master)
                  │
                  ▼  (solo en push, no en PRs)
          [5] deploy
              docker compose up --build -d
              Espera health check (actuator/health)
              Smoke test: GET /coincidencias → 200 OK
              docker compose down -v
```

### Archivos del pipeline

| Archivo | Propósito |
|---|---|
| `.github/workflows/ci-cd.yml` | Pipeline principal (5 jobs) |
| `.github/workflows/security.yml` | Escaneo de seguridad con Snyk + CodeQL |
| `.github/dependabot.yml` | Actualizaciones automáticas de dependencias Maven y Actions |

### Configuración requerida

Agregar en GitHub → Settings → Secrets and variables → Actions:

| Secret | Descripción |
|---|---|
| `SNYK_TOKEN` | Token de autenticación de [snyk.io](https://snyk.io) (cuenta gratuita) |
| `GITHUB_TOKEN` | Generado automáticamente por GitHub (no requiere configuración manual) |

---

## Pruebas Automatizadas

Las pruebas se ejecutan en el job `test` del pipeline y **no requieren base de datos ni RabbitMQ** — usan mocks de Mockito.

| Clase | Tests | Qué cubre |
|---|---|---|
| `CoincidenciaServiceTest` | 8 | Algoritmo de scoring (umbral 0.60), lógica PERDIDO/VISTO, casos borde |
| `CoincidenciaControllerTest` | 10 | Endpoints REST: GET, DELETE — status codes y cuerpo JSON |
| `CoincidenciasApplicationTests` | 1 | Smoke test de arranque |

```bash
# Ejecutar todos los tests localmente
./mvnw test

# Ver reporte de resultados
ls target/surefire-reports/
```

---

## Análisis de Seguridad

El job `security-scan` del pipeline **bloquea automáticamente** el despliegue si detecta vulnerabilidades de severidad HIGH o CRITICAL:

- **Snyk**: analiza dependencias Maven declaradas en `pom.xml`
- **CodeQL** (workflow `security.yml`): análisis estático del código Java con reglas `security-and-quality`
- **Dependabot**: genera pull requests automáticos cada lunes con actualizaciones de dependencias

Los resultados se visualizan en GitHub → pestaña **Security → Code scanning alerts**.

---

## Trazabilidad: Del Desarrollo a Producción

Cada ejecución del pipeline genera evidencia en cada etapa:

| Etapa | Evidencia de trazabilidad |
|---|---|
| **Commit** | SHA del commit en todos los tags de la imagen Docker (`sha-{short_sha}`) |
| **Tests** | Reporte XML subido como Artifact en GitHub Actions (retención 15 días) |
| **Seguridad** | Reporte SARIF visible en GitHub Security tab |
| **Imagen Docker** | Digest SHA256 único impreso en logs del job `docker-build` y publicado en `ghcr.io` |
| **Deploy** | Logs del stack (`docker compose logs`) capturados en el job `deploy` |

La imagen Docker publicada incluye etiquetas de trazabilidad:

```
ghcr.io/<usuario>/<repo>:latest          ← rama principal
ghcr.io/<usuario>/<repo>:main            ← nombre de rama
ghcr.io/<usuario>/<repo>:sha-a1b2c3d     ← commit exacto
```

---

## Declaración de Uso de Inteligencia Artificial

En el desarrollo de este proyecto se utilizaron herramientas de IA como apoyo técnico:

| Herramienta | Uso aplicado |
|---|---|
| **Claude (Anthropic)** | Apoyo en la generación de configuraciones de GitHub Actions, estructura de tests unitarios con Mockito y revisión de la configuración de Docker Compose |

Todo el contenido generado con IA fue revisado, validado y ajustado por el equipo para asegurar coherencia con los requerimientos del proyecto. Las justificaciones técnicas, reflexiones individuales y decisiones de arquitectura son propias del equipo.

Citado según: https://bibliotecas.duoc.cl/ia

---
