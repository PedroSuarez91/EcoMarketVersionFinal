# Microservicio Catálogo — EcoMarket

Microservicio encargado de la gestión del **catálogo de productos** del sistema EcoMarket. Administra los productos, sus categorías, las reseñas de los clientes y los catálogos que los agrupan. Forma parte de una arquitectura de microservicios y expone una API REST versionada.

---

## Tecnologías

| Componente | Versión / Detalle |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Web MVC | `spring-boot-starter-webmvc` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| Base de datos | MySQL (producción) / H2 en memoria (tests) |
| Actuator | `spring-boot-starter-actuator` |
| Lombok | Para reducir código repetitivo (getters, setters, constructores) |
| Bean Validation | `spring-boot-starter-validation` — valida el cuerpo de las peticiones (`@NotBlank`, `@Size`, `@Min`, `@Max`, `@Positive`, etc.) |
| springdoc-openapi | `springdoc-openapi-starter-webmvc-ui` (2.7.0) — documentación interactiva de la API (Swagger UI) |
| Maven | Gestión de dependencias y build (incluye wrapper `mvnw`) |

---

## Dominios del modelo

El microservicio gestiona cuatro entidades relacionadas entre sí:

- **Producto**: entidad central. Tiene nombre, marca, descripción, tipo, precio unitario, estado y una referencia (`idInventario`) al microservicio de Inventario. Pertenece a un catálogo, puede tener varias reseñas y varias categorías.
- **Catalogo**: agrupa muchos productos. Tiene nombre y fecha de actualización.
- **Categoria**: clasifica productos mediante una relación muchos-a-muchos.
- **Resenia**: comentario y calificación que un cliente deja sobre un producto.

### Relaciones

- `Catalogo` 1 — N `Producto` (un catálogo agrupa muchos productos).
- `Producto` N — N `Categoria` (un producto puede estar en varias categorías y viceversa).
- `Producto` 1 — N `Resenia` (un producto tiene muchas reseñas).

Las relaciones bidireccionales usan `@JsonManagedReference` / `@JsonBackReference` (y `@JsonIgnore` en Categoria) para evitar la recursión infinita al serializar a JSON.

> Nota: `Producto.idInventario` **no** es una relación JPA, sino un puntero lógico hacia el microservicio de Inventario.

---

## Requisitos previos

- **JDK 25** instalado y configurado.
- **MySQL** corriendo en `localhost:3306` (por ejemplo vía XAMPP/MariaDB).
- No es necesario crear la base manualmente: la URL incluye `createDatabaseIfNotExist=true`, así que la base `catalogodb` se crea sola al arrancar.

---

## Configuración

La configuración principal está en `src/main/resources/application.properties`:

```properties
spring.application.name=producto
server.port=8090
spring.datasource.url=jdbc:mysql://localhost:3306/catalogodb?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8090` | Puerto en el que corre el servicio |
| Base de datos | `catalogodb` | Se crea automáticamente si no existe |
| `ddl-auto` | `update` | Hibernate actualiza el esquema según las entidades |

Ajusta `spring.datasource.username` y `spring.datasource.password` según tu instalación de MySQL.

---

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

El servicio queda disponible en `http://localhost:8090`.

Para empaquetar un `.jar` ejecutable:

```bash
./mvnw clean package
java -jar target/catalogo-0.0.1-SNAPSHOT.jar
```

---

## API REST

Base URL: `http://localhost:8090`

### Documentación interactiva (Swagger UI)

El servicio expone la documentación OpenAPI de forma interactiva. Con la aplicación corriendo:

- **Swagger UI**: `http://localhost:8090/doc/swagger-ui.html`
- **Especificación OpenAPI (JSON)**: `http://localhost:8090/v3/api-docs`

Desde ahí se pueden explorar y probar todos los endpoints sin necesidad de un cliente externo.

### Estado del servicio (Actuator)

Gracias a `spring-boot-starter-actuator`, el estado del servicio queda disponible en `http://localhost:8090/actuator/health` (expuesto por defecto), útil para health checks.

### Validación de peticiones

Todas las operaciones `POST` y `PUT` validan el cuerpo antes de guardar. Si algún campo no cumple las reglas (por ejemplo, nombre en blanco, texto que supera el largo máximo, precio no positivo o una calificación de reseña fuera del rango 1–5), el servicio responde **400 (Bad Request)** con el mensaje de validación correspondiente. Por eso las tablas siguientes incluyen `400` en esos métodos.

### Productos — `/api/v1/productos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/productos` | Lista todos los productos | 200 / 204 |
| GET | `/api/v1/productos/{id}` | Obtiene un producto por id | 200 / 204 |
| POST | `/api/v1/productos` | Registra un producto | 201 / 400 / 409 |
| PUT | `/api/v1/productos/{id}` | Actualiza un producto | 200 / 400 / 404 |
| DELETE | `/api/v1/productos/{id}` | Elimina un producto | 204 / 404 |
| GET | `/api/v1/productos/categoria/{idCategoria}` | Productos de una categoría | 200 / 204 |
| GET | `/api/v1/productos/buscar?nombre=` | Busca por nombre (contiene, ignora mayúsculas) | 200 / 204 |
| GET | `/api/v1/productos/marca/{marca}` | Productos de una marca | 200 / 204 |
| GET | `/api/v1/productos/precio/rango?min=&max=` | Productos en un rango de precio | 200 / 204 |
| GET | `/api/v1/productos/precio/maximo?max=` | Productos con precio ≤ máximo | 200 / 204 |
| GET | `/api/v1/productos/precio/minimo?min=` | Productos con precio ≥ mínimo | 200 / 204 |

Ejemplo de cuerpo para crear un producto:

```json
{
  "idInventario": 1,
  "tipoProducto": "ALIMENTO",
  "nombre": "Manzana Fuji",
  "marca": "DelHuerto",
  "descripcion": "Manzanas frescas por kilo",
  "precioUnitario": 1990,
  "estado": true
}
```

### Categorías — `/api/v1/categorias`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/categorias` | Lista todas las categorías | 200 / 204 |
| GET | `/api/v1/categorias/{id}` | Obtiene una categoría por id | 200 / 204 |
| POST | `/api/v1/categorias` | Crea una categoría | 201 / 400 / 409 |
| DELETE | `/api/v1/categorias/{id}` | Elimina una categoría | 204 |

```json
{
  "nombreCategoria": "Frutas",
  "tipoProducto": "ALIMENTO"
}
```

### Reseñas — `/api/v1/resenias`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/resenias` | Lista todas las reseñas | 200 / 204 |
| GET | `/api/v1/resenias/producto/{idProducto}` | Reseñas de un producto | 200 / 204 |
| POST | `/api/v1/resenias` | Registra una reseña | 201 / 400 / 404 |
| PUT | `/api/v1/resenias/{id}` | Actualiza una reseña | 200 / 400 / 404 |
| DELETE | `/api/v1/resenias/{id}` | Elimina una reseña | 204 |

Al registrar una reseña, el producto referenciado **debe existir**; si no, responde `404`. El cuerpo debe incluir el producto con su id:

```json
{
  "comentario": "Excelente calidad",
  "calificacion": 5,
  "fechaResenia": "2026-06-30",
  "producto": { "idProducto": 1 }
}
```

### Catálogos — `/api/v1/catalogos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/catalogos` | Lista todos los catálogos | 200 / 204 |
| GET | `/api/v1/catalogos/{id}` | Obtiene un catálogo por id | 200 / 204 |
| POST | `/api/v1/catalogos` | Crea un catálogo | 201 / 400 / 409 |
| PUT | `/api/v1/catalogos/{id}` | Actualiza un catálogo | 200 / 400 / 404 |
| DELETE | `/api/v1/catalogos/{id}` | Elimina un catálogo | 204 |

```json
{
  "nombreCatalogo": "Catálogo Verano 2026",
  "fechaActualizacion": "2026-06-30"
}
```

---

## Pruebas

El proyecto incluye pruebas en **tres niveles**, siguiendo el estándar de testing del ecosistema:

1. **Unitarias de servicio** (`*ServiceTest`): prueban la lógica de cada service de forma aislada, mockeando los repositorios con Mockito.
2. **De capa web** (`*ControllerTest`): usan `@WebMvcTest` + `MockMvc` para probar los controllers, con el service mockeado.
3. **De integración** (`*ControllerIT`): usan `@SpringBootTest` y levantan la aplicación completa contra una base **H2 en memoria**.

Los tests de integración usan el perfil `test` (`src/test/resources/application-test.properties`), que reemplaza MySQL por H2, de modo que **no necesitas MySQL corriendo para ejecutarlos**.

Ejecutar toda la batería de pruebas:

```bash
./mvnw test
```

Ejecutar solo un dominio (ejemplo, Producto):

```bash
./mvnw test -Dtest=ProductoServiceTest,ProductoControllerTest,ProductoControllerIT
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/ecomarket/catalogo/
│   │   ├── CatalogoApplication.java        # Clase principal
│   │   ├── controller/                     # Controladores REST (4)
│   │   ├── model/                          # Entidades JPA (4)
│   │   ├── repository/                     # Repositorios Spring Data (4)
│   │   └── service/                        # Lógica de negocio (4)
│   └── resources/
│       └── application.properties          # Configuración (MySQL, puerto 8090)
└── test/
    ├── java/ecomarket/catalogo/            # Tests (unitarios, web, integración)
    └── resources/
        └── application-test.properties     # Configuración de test (H2)
```

---

## Lugar en el ecosistema EcoMarket

Este microservicio publica la información de productos que otros servicios consumen (por ejemplo, el **Carro** y el **Pedido** consultan productos por su id). A su vez, referencia al microservicio de **Inventario** mediante `idInventario`, sin acoplarse a él por base de datos.

| Servicio | Puerto | Relación con Catálogo |
|---|---|---|
| Catálogo | 8090 | Este servicio |
| Carro | 8083 | Consume productos |
| Pedido | 8093 | Consume productos (vía carro) |
| Inventario | — | Referenciado por `idInventario` |
