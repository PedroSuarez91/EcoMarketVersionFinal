# Microservicio Pedido — EcoMarket

Microservicio encargado de la gestión de **pedidos** del sistema EcoMarket. Construye pedidos a partir del carro de compras de un usuario, administra las direcciones de envío y controla el ciclo de vida del pedido (pendiente, pagado, enviado, etc.). Forma parte de una arquitectura de microservicios y se comunica con otros servicios vía REST.

---

## Tecnologías

| Componente | Versión / Detalle |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Web MVC | `spring-boot-starter-webmvc` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| RestTemplate | Comunicación síncrona con el microservicio de Carro |
| Base de datos | MySQL (producción) / H2 en memoria (tests) |
| Lombok | Para reducir código repetitivo (getters, setters, constructores) |
| Bean Validation | `spring-boot-starter-validation` — valida el cuerpo de las peticiones (`@NotBlank`, `@Size`, `@Positive`, etc.) |
| springdoc-openapi | `springdoc-openapi-starter-webmvc-ui` (2.7.0) — documentación interactiva de la API (Swagger UI) |
| Maven | Gestión de dependencias y build (incluye wrapper `mvnw`) |

---

## Dominios del modelo

El microservicio gestiona dos entidades propias y consume datos del carro mediante DTOs:

- **Pedido**: entidad central. Contiene el usuario, el nombre del cliente, la fecha, el tipo de entrega, el cupón aplicado, subtotal, total, estado, un resumen de la compra y la lista de ítems. Referencia una dirección de envío.
- **ItemPedido**: cada línea del pedido (producto, cantidad, precio unitario, subtotal). Pertenece a un pedido.
- **Direccion**: dirección de envío (calle, número, región, ciudad, comuna, código postal). Se gestiona **dentro** de este microservicio con su propio CRUD.
- **EstadoPedido** (enum): `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.

DTOs de consumo (no son entidades, representan datos que llegan del carro):

- **CarroDTO** / **ItemCarroDTO**: estructura del carro que este servicio lee para armar el pedido.

### Relaciones

- `Pedido` 1 — N `ItemPedido` (un pedido tiene muchos ítems; cascada y `orphanRemoval`).
- `Pedido` N — 1 `Direccion` (muchos pedidos pueden apuntar a una dirección).

> Nota: la dirección se administra como entidad propia del pedido. El `idDireccion` que pudiera venir en el carro **se ignora**; la dirección se asigna por su id desde este servicio.

---

## Requisitos previos

- **JDK 25** instalado y configurado.
- **MySQL** corriendo en `localhost:3306` (por ejemplo vía XAMPP/MariaDB). La base `dbpedido` se crea sola al arrancar (`createDatabaseIfNotExist=true`).
- El microservicio de **Carro** disponible en `localhost:8083` para poder crear pedidos a partir de un carro.
- (Opcional) El microservicio de **Inventario**: el descuento de stock es *best-effort*, así que el pedido se crea aunque inventario no esté disponible.

---

## Configuración

La configuración principal está en `src/main/resources/application.properties`:

```properties
spring.application.name=pedido-ms
server.port=8093
spring.datasource.url=jdbc:mysql://localhost:3306/dbpedido?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Microservicios que consume el pedido
servicios.carro.url=http://localhost:8083
servicios.inventario.url=http://localhost:9093
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8093` | Puerto en el que corre el servicio |
| Base de datos | `dbpedido` | Se crea automáticamente si no existe |
| `servicios.carro.url` | `http://localhost:8083` | URL del microservicio de Carro |
| `servicios.inventario.url` | `http://localhost:9093` | URL del microservicio de Inventario (best-effort) |

---

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

El servicio queda disponible en `http://localhost:8093`.

Para empaquetar un `.jar` ejecutable:

```bash
./mvnw clean package
java -jar target/pedido-0.0.1-SNAPSHOT.jar
```

---

## API REST

Base URL: `http://localhost:8093`

### Documentación interactiva (Swagger UI)

El servicio expone la documentación OpenAPI de forma interactiva. Con la aplicación corriendo:

- **Swagger UI**: `http://localhost:8093/doc/swagger-ui.html`
- **Especificación OpenAPI (JSON)**: `http://localhost:8093/v3/api-docs`

Desde ahí se pueden explorar y probar todos los endpoints sin necesidad de un cliente externo.

### Pedidos — `/api/v1/pedidos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pedidos` | Lista todos los pedidos | 200 / 204 |
| GET | `/api/v1/pedidos/{id}` | Obtiene un pedido por id | 200 / 204 |
| GET | `/api/v1/pedidos/usuario/{idUsuario}` | Pedidos de un usuario | 200 / 204 |
| POST | `/api/v1/pedidos/carro/{idCarro}?idDireccion={id}` | Crea un pedido a partir de un carro | 201 / 404 |
| PUT | `/api/v1/pedidos/{id}/direccion/{idDireccion}` | Asigna/cambia la dirección del pedido | 200 / 404 |
| PUT | `/api/v1/pedidos/{id}/estado?estado={ESTADO}` | Cambia el estado del pedido | 200 / 404 |
| DELETE | `/api/v1/pedidos/{id}` | Elimina un pedido | 204 / 404 |

Crear un pedido desde el carro `7` usando la dirección `1`:

```
POST http://localhost:8093/api/v1/pedidos/carro/7?idDireccion=1
```

El servicio consulta el carro en el microservicio de Carro, copia sus ítems y totales, asigna la dirección indicada, intenta descontar stock (best-effort) y guarda el pedido en estado `PENDIENTE`. Si el carro no existe o está vacío, responde `404`.

Cambiar el estado:

```
PUT http://localhost:8093/api/v1/pedidos/1/estado?estado=ENVIADO
```

### Direcciones — `/api/v1/direcciones`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/direcciones` | Lista todas las direcciones | 200 / 204 |
| GET | `/api/v1/direcciones/{id}` | Obtiene una dirección por id | 200 / 204 |
| POST | `/api/v1/direcciones` | Crea una dirección | 201 / 400 / 409 |
| PUT | `/api/v1/direcciones/{id}` | Actualiza una dirección | 200 / 400 / 404 |
| DELETE | `/api/v1/direcciones/{id}` | Elimina una dirección | 204 |

```json
{
  "calle": "Av. Siempre Viva",
  "numero": "742",
  "region": "Metropolitana",
  "ciudad": "Santiago",
  "comuna": "Providencia",
  "codigoPostal": 8320000
}
```

El cuerpo se valida antes de guardar: `calle`, `numero`, `region`, `ciudad` y `comuna` son obligatorios (`calle` máx. 100 caracteres, `numero` máx. 20) y `codigoPostal` debe ser un entero positivo. Si algún campo no cumple, el servicio responde **400 (Bad Request)** con el mensaje de validación correspondiente.

---

## Flujo típico

1. Crear una dirección: `POST /api/v1/direcciones` → devuelve `idDireccion`.
2. Tener un carro listo en el microservicio de Carro (con sus ítems y totales).
3. Crear el pedido: `POST /api/v1/pedidos/carro/{idCarro}?idDireccion={idDireccion}`.
4. El pedido nace en estado `PENDIENTE`. Luego se puede avanzar su estado con `PUT /api/v1/pedidos/{id}/estado`.

---

## Pruebas

El proyecto incluye pruebas en **tres niveles**:

1. **Unitarias de servicio** (`*ServiceTest`): prueban `PedidoService` y `DireccionService` de forma aislada, mockeando los repositorios y el `RestTemplate` con Mockito.
2. **De capa web** (`*ControllerTest`): usan `@WebMvcTest` + `MockMvc`, con el service mockeado.
3. **De integración** (`*ControllerIT`): usan `@SpringBootTest` y levantan la aplicación completa contra **H2 en memoria**. El `RestTemplate` se reemplaza con `@MockitoBean` para simular el carro sin depender del servicio real.

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`), que reemplaza MySQL por H2, de modo que **no necesitas MySQL ni el resto de microservicios corriendo para ejecutarlos**.

Ejecutar toda la batería de pruebas:

```bash
./mvnw test
```

Ejecutar solo un grupo (ejemplo, el service de pedido):

```bash
./mvnw test -Dtest=PedidoServiceTest
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/ecomarket/pedido/
│   │   ├── PedidoApplication.java          # Clase principal
│   │   ├── config/                         # Configuración del RestTemplate
│   │   ├── controller/                     # PedidoController, DireccionController
│   │   ├── model/                          # Pedido, ItemPedido, Direccion, EstadoPedido, DTOs del carro
│   │   ├── repository/                     # PedidoRepository, DireccionRepository
│   │   └── service/                        # PedidoService, DireccionService
│   └── resources/
│       └── application.properties          # Configuración (MySQL, puerto 8093, URLs de servicios)
└── test/
    ├── java/ecomarket/pedido/              # Tests (unitarios, web, integración)
    └── resources/
        └── application-test.properties     # Configuración de test (H2)
```

---

## Lugar en el ecosistema EcoMarket

El pedido es un servicio "orquestador": toma el carro ya calculado y lo convierte en un pedido formal, gestionando además sus propias direcciones de envío.

| Servicio | Puerto | Relación con Pedido |
|---|---|---|
| Pedido | 8093 | Este servicio |
| Carro | 8083 | Es consumido para construir el pedido |
| Inventario | 9093 | Descuento de stock (best-effort) |
| Factura | 8087 | Consume el pedido para emitir la factura |