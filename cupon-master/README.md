# Microservicio Cupón — EcoMarket

Microservicio encargado de la gestión de **cupones de descuento** del sistema EcoMarket. Administra el ciclo de vida de los cupones (creación, consulta, actualización, eliminación) y valida si un cupón puede aplicarse. Forma parte de una arquitectura de microservicios y es consumido por el servicio de Carro al momento de aplicar descuentos.

---

## Tecnologías

| Componente | Versión / Detalle |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Web MVC | `spring-boot-starter-webmvc` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| Base de datos | MySQL (producción) / H2 en memoria (tests) |
| Lombok | Para reducir código repetitivo (getters, setters, constructores) |
| Bean Validation | `spring-boot-starter-validation` — valida el cuerpo de las peticiones (`@NotBlank`, `@Size`, `@NotNull`, `@DecimalMin`, `@DecimalMax`) |
| springdoc-openapi | `springdoc-openapi-starter-webmvc-ui` (2.7.0) — documentación interactiva de la API (Swagger UI) |
| Maven | Gestión de dependencias y build (incluye wrapper `mvnw`) |

---

## Dominio del modelo

- **Cupon**: entidad central. Representa un cupón de descuento con los siguientes campos:

| Campo | Tipo | Descripción |
|---|---|---|
| `idCupon` | Long | Identificador autogenerado |
| `codigo` | String | Código del cupón. **Único**: no pueden existir dos con el mismo código |
| `porcentajeDescuento` | Double | Porcentaje de descuento que aplica el cupón |
| `activo` | Boolean | Si el cupón está habilitado |
| `fechaExpiracion` | LocalDate | Fecha de vencimiento (puede ser nula = sin vencimiento) |

### Reglas de negocio

- Al **guardar** un cupón, si no se indica `activo` nace **activo** por defecto.
- El `codigo` es **único** a nivel de base de datos; intentar crear uno repetido devuelve `409`.
- La **validación** de un cupón (`/validar/{codigo}`) considera que es válido solo si está **activo** y **no ha expirado**.

---

## Requisitos previos

- **JDK 25** instalado y configurado.
- **MySQL** corriendo en `localhost:3306` (por ejemplo vía XAMPP/MariaDB). La base `cupondb` se crea sola al arrancar (`createDatabaseIfNotExist=true`).

Este microservicio es **autónomo**: no consume a ningún otro servicio.

---

## Configuración

La configuración principal está en `src/main/resources/application.properties`:

```properties
spring.application.name=cupon
server.port=8091
spring.datasource.url=jdbc:mysql://localhost:3306/cupondb?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8091` | Puerto en el que corre el servicio |
| Base de datos | `cupondb` | Se crea automáticamente si no existe |
| `ddl-auto` | `update` | Hibernate actualiza el esquema según las entidades |

---

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

El servicio queda disponible en `http://localhost:8091`.

Para empaquetar un `.jar` ejecutable:

```bash
./mvnw clean package
java -jar target/cupon-0.0.1-SNAPSHOT.jar
```

---

## API REST

Base URL: `http://localhost:8091`

### Documentación interactiva (Swagger UI)

El servicio expone la documentación OpenAPI de forma interactiva. Con la aplicación corriendo:

- **Swagger UI**: `http://localhost:8091/doc/swagger-ui.html`
- **Especificación OpenAPI (JSON)**: `http://localhost:8091/v3/api-docs`

Desde ahí se pueden explorar y probar todos los endpoints sin necesidad de un cliente externo.

### Validación de peticiones

Las operaciones `POST` y `PUT` validan el cuerpo antes de guardar: el `codigo` es obligatorio y de máximo 50 caracteres, y `porcentajeDescuento` es obligatorio y debe estar entre 0 y 100. Si algún campo no cumple, el servicio responde **400 (Bad Request)** con el mensaje de validación correspondiente (por eso ambos métodos incluyen `400` en la tabla).

### Cupones — `/api/v1/cupones`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/cupones` | Lista todos los cupones | 200 / 204 |
| GET | `/api/v1/cupones/{id}` | Obtiene un cupón por id | 200 / 204 |
| GET | `/api/v1/cupones/codigo/{codigo}` | Busca un cupón por su código | 200 / 404 |
| GET | `/api/v1/cupones/validar/{codigo}` | Indica si el cupón es válido (activo y no vencido) | 200 (`true`/`false`) |
| POST | `/api/v1/cupones` | Crea un cupón | 201 / 400 / 409 |
| PUT | `/api/v1/cupones/{id}` | Actualiza un cupón | 200 / 400 / 404 |
| DELETE | `/api/v1/cupones/{id}` | Elimina un cupón | 204 |

Ejemplo de cuerpo para crear un cupón:

```json
{
  "codigo": "VERANO10",
  "porcentajeDescuento": 10.0,
  "activo": true,
  "fechaExpiracion": "2026-12-31"
}
```

Validar un cupón (devuelve `true` o `false`):

```
GET http://localhost:8091/api/v1/cupones/validar/VERANO10
```

Notas sobre los códigos de respuesta:

- **POST** devuelve `409` si el `codigo` ya existe (restricción de unicidad).
- **POST** y **PUT** devuelven `400` si el cuerpo no pasa la validación (código vacío o mayor a 50 caracteres, o descuento fuera del rango 0–100).
- **GET `/{id}`** devuelve `204` si el cupón no existe, mientras que **GET `/codigo/{codigo}`** devuelve `404` en ese caso.
- **GET `/validar/{codigo}`** siempre responde `200` con un booleano: `true` si está activo y vigente, `false` en cualquier otro caso (inactivo, vencido o inexistente).

---

## Pruebas

El proyecto incluye pruebas en **tres niveles**:

1. **Unitarias de servicio** (`CuponServiceTest`): prueban `CuponService` de forma aislada, mockeando el repositorio con Mockito. Cubren la regla de "nace activo", la búsqueda por código y los cuatro caminos de la validación (válido, vencido, inactivo, inexistente).
2. **De capa web** (`CuponControllerTest`): usan `@WebMvcTest` + `MockMvc`, con el service mockeado. Cubren los códigos 201/409 de la creación, los 200/204/404 de las consultas y el booleano de validación.
3. **De integración** (`CuponControllerIT`): usan `@SpringBootTest` y levantan la aplicación completa contra **H2 en memoria**, incluyendo la verificación de la restricción de código único de punta a punta.

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`), que reemplaza MySQL por H2, de modo que **no necesitas MySQL corriendo para ejecutarlos**.

Ejecutar toda la batería de pruebas:

```bash
./mvnw test
```

Ejecutar solo el service:

```bash
./mvnw test -Dtest=CuponServiceTest
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/ecomarket/cupon/
│   │   ├── CuponApplication.java           # Clase principal
│   │   ├── controller/                     # CuponController
│   │   ├── model/                          # Cupon
│   │   ├── repository/                     # CuponRepository
│   │   └── service/                        # CuponService
│   └── resources/
│       └── application.properties          # Configuración (MySQL, puerto 8091)
└── test/
    ├── java/ecomarket/cupon/               # Tests (unitarios, web, integración)
    └── resources/
        └── application-test.properties     # Configuración de test (H2)
```

---

## Lugar en el ecosistema EcoMarket

El cupón es un servicio autónomo que provee descuentos al resto del sistema. El microservicio de **Carro** lo consulta (por código) para validar y aplicar el descuento al total de la compra antes de generar el pedido.

| Servicio | Puerto | Relación con Cupón |
|---|---|---|
| Cupón | 8091 | Este servicio |
| Carro | 8083 | Consume el cupón para aplicar descuentos |
| Pedido | 8093 | Recibe el código de cupón ya aplicado (vía carro) |