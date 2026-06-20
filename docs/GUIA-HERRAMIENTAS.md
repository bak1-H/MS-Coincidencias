# Guía de herramientas del proyecto

Esta guía es una referencia personal para entender qué hace cada herramienta que usamos en MS-Coincidencias, para qué sirve y dónde está configurada. No es parte de la entrega: es para tener claro el "por qué" de cada cosa.

La idea general: tenemos un microservicio (el código), y alrededor de él un montón de herramientas que se encargan de **construirlo, probarlo, revisar su calidad y seguridad, desplegarlo y vigilarlo mientras corre**. Cada herramienta cubre una de esas etapas.

---

## 1. Construcción y ejecución

### Maven (`mvnw`)
**Qué es:** la herramienta que compila el proyecto, descarga las dependencias y arma el `.jar` final.

**Para qué lo usamos:** todo lo que tenga que ver con compilar, probar y empaquetar pasa por Maven. El archivo `pom.xml` es donde se declara todo: las dependencias (Spring, PostgreSQL, etc.) y los plugins (JaCoCo, Sonar).

**`mvn` vs `mvnw`:** `mvnw` es el "wrapper", una versión que viene con el proyecto y garantiza que todos usen la misma versión de Maven. Siempre conviene usar `mvnw`.

Comandos típicos:
- `mvnw test` → compila y corre las pruebas
- `mvnw package` → genera el `.jar`
- `mvnw verify` → pruebas + reporte de cobertura

### Docker
**Qué es:** empaqueta la aplicación junto con todo lo que necesita para correr (Java, el `.jar`, configuración) en una "imagen". Esa imagen corre igual en cualquier máquina.

**Para qué lo usamos:** el `Dockerfile` define cómo se construye la imagen del microservicio. Usamos un build de **dos etapas**: una compila con Maven y la otra se queda solo con lo justo para ejecutar (más liviano).

**Analogía:** es como meter la app en una caja sellada con todo adentro, para que no dependa de "en mi máquina funciona".

### Docker Compose
**Qué es:** levanta varios contenedores juntos y los conecta entre sí con un solo comando.

**Para qué lo usamos:** el microservicio no vive solo, necesita base de datos (PostgreSQL), cola (RabbitMQ) y, en la parte 2, el stack de monitoreo (Prometheus, Grafana, Pushgateway). El `docker-compose.yml` los define todos y se encarga de que arranquen en el orden correcto.

---

## 2. Pruebas y calidad del código

### JUnit 5 + Mockito
**Qué es:** JUnit es el framework para escribir pruebas unitarias. Mockito sirve para "simular" dependencias (mocks), así una prueba no necesita una base de datos real para correr.

**Para qué lo usamos:** probar que el algoritmo de coincidencias y los endpoints funcionan, de forma rápida y aislada.

### JaCoCo
**Qué es:** mide la **cobertura de las pruebas**, o sea, qué porcentaje del código fue ejecutado por las pruebas.

**Para qué lo usamos:** saber qué tan bien probado está el proyecto. Si un porcentaje es bajo, significa que hay código que ningún test toca y que podría fallar sin que nos enteremos.

**Cómo se ve:** después de `mvnw test`, genera un reporte en `target/site/jacoco/index.html`. Ahí se ve el porcentaje total y, por clase, qué líneas están cubiertas (verde) y cuáles no (rojo).

**Dato:** que una clase tenga 0% no siempre es malo. Las clases de arranque o de pura configuración normalmente no se testean.

### SonarCloud
**Qué es:** un servicio en la nube que analiza el código y lo evalúa en tres aspectos: **bugs** (errores probables), **vulnerabilidades** (riesgos de seguridad) y **code smells** (código que funciona pero está mal escrito). También muestra la cobertura (que recibe de JaCoCo) y el código duplicado.

**Para qué lo usamos:** tener una nota objetiva de la salud del código. SonarCloud le pone una calificación de A a E a cada aspecto.

**Quality Gate:** es la parte clave. Es un conjunto de condiciones que el código tiene que cumplir para "pasar". Si no las cumple, el Quality Gate queda en *Failed*. Lo conectamos al pipeline para que, si falla, frene la entrega.

**Diferencia con JaCoCo:** JaCoCo solo mide cobertura. SonarCloud mira la calidad completa (y usa la cobertura de JaCoCo como un dato más).

---

## 3. Seguridad

### Snyk
**Qué es:** revisa las **dependencias** del proyecto (las librerías que usamos, declaradas en el `pom.xml`) buscando vulnerabilidades conocidas (CVEs).

**Para qué lo usamos:** una app puede estar perfecta, pero si usa una librería con un agujero de seguridad conocido, hereda ese problema. Snyk avisa de eso.

**Cómo lo configuramos:** corre en el pipeline con `--severity-threshold=high`, lo que significa que si encuentra una vulnerabilidad **alta o crítica**, ese paso falla y frena el pipeline.

**Diferencia con SonarCloud:** SonarCloud mira **nuestro** código; Snyk mira el código de **terceros** (las dependencias).

### CodeQL
**Qué es:** una herramienta de GitHub que analiza **nuestro** código fuente buscando patrones inseguros (inyecciones, mal manejo de datos, etc.).

**Para qué lo usamos:** una segunda capa de seguridad, enfocada en el código propio. Los resultados aparecen en GitHub, en la pestaña *Security → Code scanning*.

### Dependabot
**Qué es:** un bot de GitHub que revisa periódicamente si hay versiones más nuevas (y más seguras) de las dependencias.

**Para qué lo usamos:** cuando encuentra una actualización importante, abre un Pull Request automático con el cambio. Así las dependencias no quedan viejas y vulnerables con el tiempo.

---

## 4. CI/CD (automatización)

### GitHub Actions
**Qué es:** el sistema que ejecuta el **pipeline** automáticamente cada vez que hacemos push o un Pull Request.

**Para qué lo usamos:** encadenar todo lo anterior sin tener que correrlo a mano. El archivo `.github/workflows/ci-cd.yml` define las etapas: compilar → probar → seguridad (Snyk) → calidad (SonarCloud) → construir imagen → desplegar.

**La idea clave:** las etapas van encadenadas. Si una falla, las siguientes no corren. Así nunca se despliega algo que no pasó las pruebas o los chequeos de seguridad/calidad.

---

## 5. Observabilidad (vigilar la app mientras corre)

Las herramientas anteriores actúan **antes** de desplegar. Estas actúan **después**, cuando la app ya está corriendo, para ver cómo se comporta.

### Spring Boot Actuator + Micrometer
**Qué es:** Actuator es un módulo de Spring que expone información interna de la app (estado de salud, métricas) en endpoints HTTP. Micrometer traduce esas métricas al formato que entiende Prometheus.

**Para qué lo usamos:** que la aplicación publique sus propias métricas (uso de CPU, memoria, peticiones HTTP, errores) en `/actuator/prometheus`, para que otra herramienta las pueda leer.

### Prometheus
**Qué es:** una base de datos especializada en métricas. Cada cierto tiempo va a "buscar" (scrape) las métricas de la app y las guarda con su marca de tiempo.

**Para qué lo usamos:** recolectar y almacenar las métricas del microservicio a lo largo del tiempo, para poder consultarlas o graficarlas.

**Analogía:** es el que toma notas constantemente de cómo está la app.

### Grafana
**Qué es:** la herramienta de **dashboards**. Toma los datos de Prometheus y los muestra en gráficos.

**Para qué lo usamos:** ver de un vistazo el estado del sistema: disponibilidad, CPU, memoria, errores, cobertura y tiempo de despliegue, todo en una sola pantalla. Lo dejamos configurado para que el dashboard se cargue solo al levantar el entorno.

**Analogía:** si Prometheus toma las notas, Grafana es el tablero que las convierte en gráficos entendibles.

### Pushgateway
**Qué es:** un intermediario para métricas que **no vienen de la app**, sino de procesos puntuales (como el pipeline de CI/CD).

**Para qué lo usamos:** el tiempo de despliegue y la cobertura de pruebas no son cosas que el microservicio genere mientras corre. Esos valores se los "empujamos" al Pushgateway con un script, y desde ahí Prometheus los lee como cualquier otra métrica. Así terminan apareciendo en el dashboard de Grafana junto al resto.

---

## Resumen rápido

| Herramienta | En una frase |
|---|---|
| Maven | Compila y arma el proyecto |
| Docker | Empaqueta la app en una imagen portable |
| Docker Compose | Levanta todos los servicios juntos |
| JUnit + Mockito | Escribe y corre las pruebas |
| JaCoCo | Mide cuánto código cubren las pruebas |
| SonarCloud | Evalúa la calidad y salud del código propio |
| Snyk | Busca vulnerabilidades en las dependencias |
| CodeQL | Busca patrones inseguros en el código propio |
| Dependabot | Mantiene las dependencias actualizadas |
| GitHub Actions | Ejecuta el pipeline automáticamente |
| Actuator + Micrometer | Hacen que la app publique sus métricas |
| Prometheus | Recolecta y guarda las métricas |
| Grafana | Muestra las métricas en dashboards |
| Pushgateway | Recibe métricas del pipeline (deploy, cobertura) |

**Cómo se relacionan, en orden:**
escribo código → Maven lo compila → JUnit lo prueba → JaCoCo mide la cobertura → SonarCloud y Snyk revisan calidad y seguridad → GitHub Actions encadena todo → Docker arma la imagen → Docker Compose la despliega → Actuator/Prometheus/Grafana la vigilan mientras corre.
