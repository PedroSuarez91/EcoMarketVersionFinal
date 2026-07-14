# Microservicio Factura — EcoMarket

Microservicio encargado de la **emisión de facturas** del sistema EcoMarket. A partir de un pedido existente genera la factura electrónica, calculando el desglose de neto e IVA. Forma parte de una arquitectura de microservicios y se comunica con el servicio de Pedido vía REST.

---

## Tecnologías

| Componente | Versión / Detalle |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Web MVC | `spring-boot-starter-webmvc` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| RestTemplate | Comunicación síncrona con el microservicio de Pedido |
| Base de datos | MySQL (producción) / H2 en memoria (tests) |
| Lombok | Para reducir código repetitivo (getters, setters, constructores) |
| Bean Validation | `spring-boot-starter-validation` — restricciones a nivel de entidad (`@NotNull`, `@PositiveOrZero`) que actúan como resguardo antes de persistir |
| springdoc-openapi | `springdoc-openapi-starter-webmvc-ui` (2.7.0) — documentación interactiva de la API (Swagger UI) |
| Maven | Gestión de dependencias y build (incluye wrapper `mvnw`) |

---

## Dominio del modelo

- **Factura**: entidad central. Contiene el id del pedido facturado, el nombre del cliente, la fecha de emisión y el desglose monetario: **neto**, **IVA** y **total** (donde `total = neto + IVA`).

DTO de consumo (no es entidad, representa lo que llega del pedido):

- **PedidoDTO**: estructura reducida del pedido que este servicio lee para facturar (`idPedido`, `nombreCliente`, `total`).

### Cálculo del IVA

La factura se emite a partir del **total** del pedido (que ya incluye IVA). El servicio descompone ese total en:

- **Neto** = total / 1.19
- **IVA** = total − neto

Con la tasa de IVA chilena del 19%. Por ejemplo, un total de `11900` produce neto `10000` e IVA `1900`.

---

## Requisitos previos

- **JDK 25** instalado y configurado.
- **MySQL** corriendo en `localhost:3306` (por ejemplo vía XAMPP/MariaDB). La base `dbfactura` se crea sola al arrancar (`createDatabaseIfNotExist=true`).
- El microservicio de **Pedido** disponible en `localhost:8093`, ya que la factura se emite a partir de un pedido existente.

---

## Configuración

La configuración principal está en `src/main/resources/application.properties`:

```properties
spring.application.name=factura-ms
server.port=8087
spring.datasource.url=jdbc:mysql://localhost:3306/dbfactura?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Microservicio que consume
servicios.pedido.url=http://localhost:8093
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8087` | Puerto en el que corre el servicio |
| Base de datos | `dbfactura` | Se crea automáticamente si no existe |
| `servicios.pedido.url` | `http://localhost:8093` | URL del microservicio de Pedido |

---

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

El servicio queda disponible en `http://localhost:8087`.

Para empaquetar un `.jar` ejecutable:

```bash
./mvnw clean package
java -jar target/factura-ms-0.0.1-SNAPSHOT.jar
```

---

## API REST

Base URL: `http://localhost:8087`

### Documentación interactiva (Swagger UI)

El servicio expone la documentación OpenAPI de forma interactiva. Con la aplicación corriendo:

- **Swagger UI**: `http://localhost:8087/doc/swagger-ui.html`
- **Especificación OpenAPI (JSON)**: `http://localhost:8087/v3/api-docs`

Desde ahí se pueden explorar y probar todos los endpoints sin necesidad de un cliente externo.

### Facturas — `/api/v1/facturas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/facturas` | Lista todas las facturas | 200 / 204 |
| GET | `/api/v1/facturas/{id}` | Obtiene una factura por id | 200 / 204 |
| GET | `/api/v1/facturas/pedido/{idPedido}` | Obtiene la factura de un pedido | 200 / 204 |
| POST | `/api/v1/facturas/emitir/{idPedido}` | Emite la factura de un pedido | 201 / 409 / 404 |

Emitir la factura del pedido `1`:

```
POST http://localhost:8087/api/v1/facturas/emitir/1
```

Comportamiento del endpoint de emisión:

- **201 Created**: la factura se generó correctamente.
- **409 Conflict**: ese pedido **ya fue facturado** (no se factura dos veces).
- **404 Not Found**: el pedido no existe en el microservicio de Pedido.

Ejemplo de factura emitida (respuesta):

```json
{
  "idFactura": 1,
  "idPedido": 1,
  "nombreCliente": "Camila Soto",
  "fechaEmision": "2026-06-30",
  "neto": 10000,
  "iva": 1900,
  "total": 11900
}
```

---

## Flujo típico

1. Existe un pedido en el microservicio de Pedido (por ejemplo, `idPedido = 1`).
2. Se emite la factura: `POST /api/v1/facturas/emitir/1`.
3. El servicio consulta el pedido en `servicios.pedido.url`, toma su total, calcula neto e IVA, y guarda la factura.
4. Un segundo intento sobre el mismo pedido devuelve `409` (ya facturado).

---

## Pruebas

El proyecto incluye pruebas en **tres niveles**:

1. **Unitarias de servicio** (`FacturaServiceTest`): prueban `FacturaService` de forma aislada, mockeando el repositorio y el `RestTemplate` con Mockito. Verifican el cálculo de neto/IVA y los casos en que el pedido no existe.
2. **De capa web** (`FacturaControllerTest`): usan `@WebMvcTest` + `MockMvc`, con el service mockeado. Cubren los códigos 201/409/404 de la emisión y los 200/204 de las consultas.
3. **De integración** (`FacturaControllerIT`): usan `@SpringBootTest` y levantan la aplicación completa contra **H2 en memoria**. El `RestTemplate` se reemplaza con `@MockitoBean` para simular el pedido sin depender del servicio real.

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`), que reemplaza MySQL por H2, de modo que **no necesitas MySQL ni el microservicio de Pedido corriendo para ejecutarlos**.

Ejecutar toda la batería de pruebas:

```bash
./mvnw test
```

Ejecutar solo el service:

```bash
./mvnw test -Dtest=FacturaServiceTest
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/ecomarket/factura_ms/
│   │   ├── FacturaMsApplication.java       # Clase principal
│   │   ├── config/                         # Configuración del RestTemplate
│   │   ├── controller/                     # FacturaController
│   │   ├── model/                          # Factura, PedidoDTO
│   │   ├── repository/                     # FacturaRepository
│   │   └── service/                        # FacturaService
│   └── resources/
│       └── application.properties          # Configuración (MySQL, puerto 8087, URL de pedido)
└── test/
    ├── java/ecomarket/factura_ms/          # Tests (unitarios, web, integración)
    └── resources/
        └── application-test.properties     # Configuración de test (H2)
```

---

## Lugar en el ecosistema EcoMarket

La factura es el último eslabón del flujo de compra: toma un pedido ya generado y produce el documento tributario con su desglose de neto e IVA.

| Servicio | Puerto | Relación con Factura |
|---|---|---|
| Factura | 8087 | Este servicio |
| Pedido | 8093 | Es consumido para emitir la factura |
| Carro | 8083 | Origen del pedido (indirecto) |