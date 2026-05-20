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

### Opción 2: Ejecución Completa con Docker

#### Paso 1: Construir Imagen Docker

```bash
mvn clean package
docker build -t ms-coincidencias:latest .
```

#### Paso 2: Ejecutar con Docker Compose

Crear archivo `docker-compose.yml` (si no existe):

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:latest
    environment:
      POSTGRES_DB: fullstack_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"
      - "15672:15672"  # Management UI
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  coincidencias-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/fullstack_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_RABBITMQ_HOST: rabbitmq
    depends_on:
      - postgres
      - rabbitmq

volumes:
  postgres_data:
  rabbitmq_data:
```

Ejecutar:

```bash
docker-compose up -d
```

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
