# MS-Coincidencias — Evaluación Parcial N°2
## Ingeniería DevOps ·

**Integrantes:** Maximiliano Arturo Huerta Gonzalez

---

## ¿Qué hace este microservicio?

MS-Coincidencias es un microservicio que detecta automáticamente coincidencias entre reportes de mascotas perdidas y mascotas encontradas. Cuando llega un nuevo reporte a través de RabbitMQ, el servicio lo compara con todos los reportes existentes del tipo contrario usando un algoritmo de puntuación. Si la puntuación supera el umbral de 0.60, se guarda la coincidencia en la base de datos y se publica una notificación.

El algoritmo considera los siguientes criterios:

| Criterio | Peso |
|---|---|
| Tipo de mascota | 0.25 |
| Raza | 0.25 |
| Tamaño | 0.15 |
| Proximidad geográfica (Haversine) | 0.20 |
| Color | 0.10 |
| Sexo | 0.05 |

---

## Stack tecnológico

| Tecnología | Versión | Para qué la usamos |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 4.0.6 | Framework del microservicio |
| Spring Data JPA | - | Acceso a la base de datos |
| Spring AMQP | - | Comunicación con RabbitMQ |
| PostgreSQL | 16 | Base de datos relacional |
| RabbitMQ | 3.13 | Cola de mensajes entre microservicios |
| Docker | - | Contenerización |
| Docker Compose | - | Orquestación local |
| GitHub Actions | - | Pipeline CI/CD |
| Snyk | - | Análisis de seguridad |
| Lombok | - | Reducción de código repetitivo |

---

## Estructura del proyecto

```
MS-Coincidencias/
├── .github/
│   ├── dependabot.yml                         # Actualización automática de dependencias
│   └── workflows/
│       ├── ci-cd.yml                          # Pipeline principal CI/CD (5 etapas)
│       └── security.yml                       # Análisis de seguridad con Snyk y CodeQL
├── src/
│   ├── main/java/com/sanosysalvos/coincidencias/
│   │   ├── CoincidenciasApplication.java      # Punto de entrada
│   │   ├── config/RabbitConfig.java           # Configuración de RabbitMQ
│   │   ├── controller/CoincidenciaController.java  # Endpoints REST
│   │   ├── messaging/NotificacionPublisher.java    # Publica notificaciones
│   │   ├── model/                             # Entidades y DTOs
│   │   ├── repository/                        # Acceso a datos
│   │   └── services/coincidenciaService.java  # Lógica de negocio y algoritmo
│   └── test/                                  # Pruebas unitarias
├── Dockerfile                                 # Imagen Docker multi-stage
├── docker-compose.yml                         # Orquestación de contenedores
└── pom.xml                                    # Dependencias Maven
```

---

## Uso de Contenedores

Contenerizamos el microservicio usando una imagen Docker de construcción en dos etapas (multi-stage build) para optimizar el tamaño final de la imagen.

### Dockerfile

La imagen se construye en dos etapas:
- **Etapa 1 (build):** usa Maven para compilar y empaquetar el proyecto
- **Etapa 2 (runtime):** usa solo el JRE de Java (imagen Alpine, más liviana) y copia el JAR generado

También configuramos un usuario sin privilegios de root para mayor seguridad, y agregamos un health check para que Docker sepa cuándo el servicio está listo.

```bash
# Construir la imagen manualmente
docker build -t ms-coincidencias:latest .

# Verificar que la imagen se creó
docker images | grep ms-coincidencias
```

---

## Pruebas Automatizadas

Implementamos pruebas unitarias con JUnit 5 y Mockito. Las pruebas no necesitan base de datos ni RabbitMQ porque usamos mocks que simulan esas dependencias.

### Clases de prueba

| Clase | Pruebas | Qué valida |
|---|---|---|
| `CoincidenciaServiceTest` | 8 | Algoritmo de scoring, umbral 0.60, asignación correcta de IDs PERDIDO/VISTO, casos borde como color parcial o sin candidatos |
| `CoincidenciaControllerTest` | 10 | Todos los endpoints REST: GET all, GET by ID, GET by reporte, GET perdido, GET visto, DELETE — códigos HTTP y cuerpo JSON |
| `CoincidenciasApplicationTests` | 1 | Que la aplicación carga sin errores |

### Ejecutar pruebas localmente

```bash
./mvnw test
```

Los reportes se guardan en `target/surefire-reports/` y también se suben como Artifact en cada ejecución del pipeline.

---

## Seguridad en el Pipeline

Configuramos dos herramientas de análisis de seguridad y Dependabot para mantener las dependencias actualizadas.

### Dependabot (`.github/dependabot.yml`)

Dependabot revisa automáticamente cada lunes si hay actualizaciones de seguridad disponibles para las dependencias de Maven y para las GitHub Actions que usamos. Si encuentra algo, crea un Pull Request automático.

### Snyk (en el pipeline)

Snyk analiza todas las dependencias del `pom.xml` en busca de vulnerabilidades conocidas (CVEs). Si encuentra una vulnerabilidad de severidad **HIGH** o **CRITICAL**, el pipeline se detiene y no permite continuar al despliegue. Esto lo configuramos con `continue-on-error: false` y `--severity-threshold=high`.

### CodeQL (`.github/workflows/security.yml`)

CodeQL analiza el código Java que escribimos para detectar patrones de código inseguro (inyecciones, manejo incorrecto de excepciones, etc.).

Los resultados de ambas herramientas se pueden ver en GitHub → pestaña **Security → Code scanning alerts**.

### Configurar SNYK_TOKEN

Para que Snyk funcione, hay que agregar el token en el repositorio:
1. En GitHub: **Settings → Secrets and variables → Actions → New repository secret**
2. Nombre: `SNYK_TOKEN`, valor: el token copiado

---

## Pipeline CI/CD y Trazabilidad

Implementamos un pipeline completo en GitHub Actions con cinco etapas que se ejecutan en orden. Si alguna etapa falla, las siguientes no se ejecutan.

### Flujo del pipeline

```
Commit / Pull Request
        │
        ▼
┌─────────────────────┐
│  1. Compilación     │  Verifica que el código compila sin errores
└────────┬────────────┘
         │
    ┌────┴─────┐
    ▼          ▼
┌───────┐  ┌──────────────────┐
│ 2.    │  │ 3. Análisis de   │  Snyk bloquea si hay vulnerabilidades
│Pruebas│  │    Seguridad     │  HIGH o CRITICAL
└───┬───┘  └────────┬─────────┘
    │               │
    └───────┬───────┘
            ▼
┌───────────────────────────┐
│ 4. Construcción imagen    │  Build multi-stage + push a ghcr.io
│    Docker                 │  Tags: latest, branch, sha-{commit}
└────────────┬──────────────┘
             │  (solo en push, no en PRs)
             ▼
┌───────────────────────────┐
│ 5. Despliegue simulado    │  docker compose up → health check →
│                           │  smoke test → docker compose down
└───────────────────────────┘
```

##  — Orquestación con Docker Compose

Usamos Docker Compose para orquestar los tres servicios que necesita el microservicio: la base de datos, la cola de mensajes y la aplicación misma.

### Características de la orquestación

- **Health checks:** cada servicio tiene un health check configurado. Docker Compose no inicia el siguiente servicio hasta que el anterior esté saludable
- **Orden de inicio garantizado:** la aplicación espera que PostgreSQL y RabbitMQ estén `healthy` antes de arrancar (`depends_on` con `condition: service_healthy`)
- **Reinicio automático:** todos los servicios tienen `restart: unless-stopped` para recuperarse ante fallos
- **Límites de recursos:** cada contenedor tiene límites de CPU y memoria para evitar que un servicio consuma todos los recursos del host
- **Red aislada:** los tres servicios se comunican a través de una red interna (`ms-coincidencias-network`) sin exposición innecesaria
- **Persistencia:** los datos de PostgreSQL y RabbitMQ se guardan en volúmenes nombrados

### Levantar el entorno completo

```bash
# Construir y levantar todos los servicios
docker compose up --build -d

# Ver el estado de los contenedores
docker compose ps

# Ver los logs de la aplicación
docker compose logs -f coincidencias-service

# Verificar que la API responde
curl http://localhost:8082/coincidencias

# Detener todo
docker compose down -v
```

### URLs de los servicios

| Servicio | URL |
|---|---|
| API REST | http://localhost:8082/coincidencias |
| Swagger UI | http://localhost:8082/swagger-ui.html |
| Health check | http://localhost:8082/actuator/health |
| RabbitMQ UI | http://localhost:15672 (guest / guest) |

---

## Endpoints de la API

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/coincidencias` | Obtener todas las coincidencias |
| GET | `/coincidencias/{id}` | Obtener una coincidencia por ID |
| GET | `/coincidencias/reporte/{idReporte}` | Coincidencias de un reporte |
| GET | `/coincidencias/perdido/{id}` | Coincidencia para un reporte perdido |
| GET | `/coincidencias/visto/{id}` | Coincidencia para un reporte encontrado |
| DELETE | `/coincidencias/{id}` | Eliminar una coincidencia |

---
