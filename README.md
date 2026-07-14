# EcoMarket SPA — Sistema de Microservicios

Proyecto académico — Duoc UC
Repositorio: https://github.com/PedroSuarez91/EcoMarketVersionFinal

---

## 1. Descripción del contexto / dominio

**EcoMarket SPA** es una plataforma de comercio electrónico dedicada a la venta de
productos ecológicos y sustentables. La empresa opera con múltiples sucursales,
bodegas de almacenamiento y una red de proveedores, y necesita un sistema capaz de
cubrir el ciclo completo del negocio:

- **Gestión de clientes y acceso**: registro de usuarios, autenticación y atención de
  tickets de soporte.
- **Catálogo y stock**: publicación de productos por categoría, reseñas de clientes,
  control de inventario por sucursal, administración de bodegas, proveedores y
  solicitudes de reabastecimiento.
- **Flujo de compra**: carro de compras, aplicación de cupones de descuento,
  generación de pedidos con dirección de despacho, emisión de facturas y gestión de
  envíos con sus rutas asociadas.

Para soportar este dominio se optó por una **arquitectura de microservicios**: cada
capacidad de negocio se implementa como una aplicación **Spring Boot** independiente,
con su propio puerto, su propia base de datos **MySQL** y la misma estructura por capas
(`Controller → Service → Repository → Model`). La comunicación entre servicios se
realiza vía **REST** usando `RestTemplate`, y todo el tráfico externo entra por un
**API Gateway** (Spring Cloud Gateway) que actúa como único punto de entrada.

### Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4.1.0 (Gateway: Spring Boot 4.0.7 + Spring Cloud Gateway WebFlux) |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8 (H2 en pruebas) |
| Validación | Bean Validation (`@Valid`, `@NotBlank`, `@Positive`, …) |
| Utilidades | Lombok |
| Comunicación | REST / `RestTemplate` |
| Build | Maven (wrapper `mvnw` incluido en cada módulo) |
| Pruebas | JUnit 5, Mockito, JaCoCo |
| Documentación API | Springdoc OpenAPI / Swagger UI |

---

## 2. Estudiantes

| Nombre | 
|---|
| Pedro Suárez | 
| Nicolas Castillo |
|  Benjamin Carrillo |

---

## 3. Microservicios implementados

El sistema está compuesto por **14 microservicios** más el **API Gateway**.

| # | Microservicio | Carpeta | Puerto | Path base | Base de datos | Responsabilidad |
|---|---|---|---|---|---|---|
| 1 | usuario-ms | `usuario-ms-master` | 8081 | `/api/v1/usuarios` | `usuariosdb` | Registro y administración de usuarios |
| 2 | proveedor-ms | `proveedor-ms-master` | 8082 | `/api/v1/proveedores` | `proveedoresdb` | Administración de proveedores |
| 3 | carro-ms | `carro-ms-master` | 8083 | `/api/v1/carros` | `dbcarro` | Carro de compras y sus ítems |
| 4 | sucursal-ms | `sucursal-ms-master` | 8084 | `/api/v1/sucursales` | `sucursaldb` | Sucursales de la empresa |
| 5 | reabastecimiento-ms | `reabastecimiento-ms-master` | 8085 | `/api/v1/reabastecimientos` | `dbreabastecimiento` | Solicitudes de reposición de stock |
| 6 | factura-ms | `factura-master` | 8087 | `/api/v1/facturas` | `dbfactura` | Emisión de facturas (consume pedido-ms) |
| 7 | catalogo (producto) | `catalogo-main` | 8090 | `/api/v1/productos`, `/api/v1/categorias`, `/api/v1/catalogos`, `/api/v1/resenias` | `catalogodb` | Catálogo, categorías y reseñas |
| 8 | cupon | `cupon-master` | 8091 | `/api/v1/cupones` | `cupondb` | Cupones y descuentos |
| 9 | pedido-ms | `pedido-master` | 8093 | `/api/v1/pedidos`, `/api/v1/direcciones` | `dbpedido` | Pedidos y direcciones de despacho (consume carro-ms e inventario) |
| 10 | soporte | `soporte_service-main` | 9091 | `/api/v1/soporte` | `soportedb` | Tickets de soporte / postventa |
| 11 | autentificacion | `autentificacion_service-main` | 9092 | `/api/v1/autentificacion` | `autentificaciondb` | Credenciales y autenticación |
| 12 | inventario | `inventario_service-main` | 9093 | `/api/v1/inventario` | `inventariodb` | Control de stock |
| 13 | bodega | `bodega_service-main` | 9094 | `/api/v1/bodega` | `bodegadb` | Bodegas de almacenamiento |
| 14 | envio_service | `envio_service-main` | 9095 | `/api/v1/envios`, `/api/v1/rutas` | `enviosdb` | Envíos y rutas de despacho |
| — | **ecomarket-gateway** | `ecomarket-gateway` | **8200** | — | — | API Gateway / punto único de entrada |

---

## 4. Rutas principales del Gateway

El Gateway (`ecomarket-gateway`, Spring Cloud Gateway sobre WebFlux) se levanta en el
puerto **8200** y enruta las peticiones al microservicio correspondiente según el path.
Su configuración está en `ecomarket-gateway/src/main/resources/application.yml`.

**URL base:** `http://localhost:8200`

### Usuarios y acceso

| Route ID | Predicado (Path) | Destino |
|---|---|---|
| `ms-usuario` | `/api/v1/usuarios`, `/api/v1/usuarios/**` | `http://localhost:8081` |
| `ms-autentificacion` | `/api/v1/autentificacion`, `/api/v1/autentificacion/**` | `http://localhost:9092` |
| `ms-soporte` | `/api/v1/soporte`, `/api/v1/soporte/**` | `http://localhost:9091` |

### Catálogo y stock

| Route ID | Predicado (Path) | Destino |
|---|---|---|
| `ms-catalogo` | `/api/v1/productos/**`, `/api/v1/categorias/**`, `/api/v1/catalogos/**`, `/api/v1/resenias/**` | `http://localhost:8090` |
| `ms-inventario` | `/api/v1/inventario`, `/api/v1/inventario/**` | `http://localhost:9093` |
| `ms-bodega` | `/api/v1/bodega`, `/api/v1/bodega/**` | `http://localhost:9094` |
| `ms-sucursal` | `/api/v1/sucursales`, `/api/v1/sucursales/**` | `http://localhost:8084` |
| `ms-proveedor` | `/api/v1/proveedores`, `/api/v1/proveedores/**` | `http://localhost:8082` |
| `ms-reabastecimiento` | `/api/v1/reabastecimientos`, `/api/v1/reabastecimientos/**` | `http://localhost:8085` |

### Flujo de compra

| Route ID | Predicado (Path) | Destino |
|---|---|---|
| `ms-cupon` | `/api/v1/cupones`, `/api/v1/cupones/**` | `http://localhost:8091` |
| `ms-carro` | `/api/v1/carros`, `/api/v1/carros/**` | `http://localhost:8083` |
| `ms-pedido` | `/api/v1/pedidos/**`, `/api/v1/direcciones/**` | `http://localhost:8093` |
| `ms-factura` | `/api/v1/facturas`, `/api/v1/facturas/**` | `http://localhost:8087` |
| `ms-envio` | `/api/v1/envios/**`, `/api/v1/rutas/**` | `http://localhost:9095` |

> El Gateway **no reescribe** los paths: la ruta pública es idéntica a la del
> microservicio. Por ejemplo, `GET http://localhost:8200/api/v1/productos` equivale a
> `GET http://localhost:8090/api/v1/productos`.

### Ejemplos de uso

```bash
# Listar productos a través del Gateway
curl http://localhost:8200/api/v1/productos

# Crear un usuario a través del Gateway
curl -X POST http://localhost:8200/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombreUsuario":"Pedro","emailUsuario":"pedro@ecomarket.cl","passwordUsuario":"1234"}'

# Consultar un envío
curl http://localhost:8200/api/v1/envios/1
```

---

## 5. Documentación Swagger / OpenAPI

Cada microservicio expone su documentación con **Springdoc OpenAPI**. Una vez levantado
el servicio, la interfaz queda disponible en su propio puerto:

| Microservicio | Swagger UI | OpenAPI JSON |
|---|---|---|
| usuario-ms | http://localhost:8081/doc/swagger-ui.html | http://localhost:8081/v3/api-docs |
| proveedor-ms | http://localhost:8082/doc/swagger-ui.html | http://localhost:8082/v3/api-docs |
| carro-ms | http://localhost:8083/doc/swagger-ui.html | http://localhost:8083/v3/api-docs |
| sucursal-ms | http://localhost:8084/doc/swagger-ui.html | http://localhost:8084/v3/api-docs |
| reabastecimiento-ms | http://localhost:8085/doc/swagger-ui.html | http://localhost:8085/v3/api-docs |
| factura-ms | http://localhost:8087/doc/swagger-ui.html | http://localhost:8087/v3/api-docs |
| catalogo | http://localhost:8090/doc/swagger-ui.html | http://localhost:8090/v3/api-docs |
| cupon | http://localhost:8091/doc/swagger-ui.html | http://localhost:8091/v3/api-docs |
| pedido-ms | http://localhost:8093/doc/swagger-ui.html | http://localhost:8093/v3/api-docs |
| soporte | http://localhost:9091/doc/swagger-ui.html | http://localhost:9091/v3/api-docs |
| autentificacion | http://localhost:9092/doc/swagger-ui.html | http://localhost:9092/v3/api-docs |
| inventario | http://localhost:9093/doc/swagger-ui.html | http://localhost:9093/v3/api-docs |
| bodega | http://localhost:9094/doc/swagger-ui.html | http://localhost:9094/v3/api-docs |
| envio_service | http://localhost:9095/doc/swagger-ui.html | http://localhost:9095/v3/api-docs |

**Configuración requerida en cada microservicio.** Los servicios `bodega` y
`envio_service` ya traen las propiedades de Swagger en su `application.properties`.
Para replicarlo en el resto, agregar en el `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>
```

y en `src/main/resources/application.properties`:

```properties
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
```

> **Nota:** si se omite la propiedad `springdoc.swagger-ui.path`, la ruta por defecto es
> `http://localhost:{puerto}/swagger-ui/index.html`.

### Documentación remota

Al desplegar en un servidor, las URLs mantienen la misma estructura reemplazando
`localhost:{puerto}` por el host público del servicio, por ejemplo:

```
http://<host-o-ip-del-servidor>:8090/doc/swagger-ui.html
```

---

## 6. Instrucciones de ejecución

### 6.1 Requisitos previos

- **JDK 25** (`java -version`)
- **Maven 3.9+** — opcional, cada módulo incluye el wrapper `./mvnw`
- **MySQL 8** corriendo en `localhost:3306` con usuario `root` y contraseña vacía
  (o ajustar `spring.datasource.username` / `password` en cada `application.properties`)
- **Git**
- Opcional: **Postman** para probar los endpoints

### 6.2 Clonar el repositorio

```bash
git clone https://github.com/PedroSuarez91/EcoMarketVersionFinal.git
cd EcoMarketVersionFinal
```

### 6.3 Preparar las bases de datos

Las tablas se crean solas (`spring.jpa.hibernate.ddl-auto=update`), pero los esquemas
deben existir previamente. Ejecutar en MySQL:

```sql
CREATE DATABASE IF NOT EXISTS usuariosdb;
CREATE DATABASE IF NOT EXISTS proveedoresdb;
CREATE DATABASE IF NOT EXISTS dbcarro;
CREATE DATABASE IF NOT EXISTS sucursaldb;
CREATE DATABASE IF NOT EXISTS dbreabastecimiento;
CREATE DATABASE IF NOT EXISTS dbfactura;
CREATE DATABASE IF NOT EXISTS catalogodb;
CREATE DATABASE IF NOT EXISTS cupondb;
CREATE DATABASE IF NOT EXISTS dbpedido;
CREATE DATABASE IF NOT EXISTS soportedb;
CREATE DATABASE IF NOT EXISTS autentificaciondb;
CREATE DATABASE IF NOT EXISTS inventariodb;
CREATE DATABASE IF NOT EXISTS bodegadb;
CREATE DATABASE IF NOT EXISTS enviosdb;
```

### 6.4 Ejecución local (modo desarrollo)

Cada microservicio es un proyecto Maven independiente y se levanta en su **propia
terminal**:

```bash
cd usuario-ms-master
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Repetir para cada carpeta. **Orden recomendado** (primero los servicios base, luego los
que consumen a otros, y el Gateway al final):

```
1. usuario-ms-master        (8081)
2. autentificacion_service-main (9092)
3. catalogo-main            (8090)
4. proveedor-ms-master      (8082)
5. sucursal-ms-master       (8084)
6. bodega_service-main      (9094)
7. inventario_service-main  (9093)
8. reabastecimiento-ms-master (8085)
9. cupon-master             (8091)
10. carro-ms-master         (8083)
11. pedido-master           (8093)   -> consume carro-ms e inventario
12. factura-master          (8087)   -> consume pedido-ms
13. envio_service-main      (9095)
14. soporte_service-main    (9091)
15. ecomarket-gateway       (8200)   -> siempre al final
```

Verificación rápida:

```bash
curl http://localhost:8081/api/v1/usuarios     # directo al microservicio
curl http://localhost:8200/api/v1/usuarios     # a través del Gateway
```

### 6.5 Compilar y ejecutar el JAR

```bash
cd usuario-ms-master
./mvnw clean package                 # genera target/usuario-ms-0.0.1-SNAPSHOT.jar
java -jar target/usuario-ms-0.0.1-SNAPSHOT.jar
```

Para saltarse los tests durante el empaquetado: `./mvnw clean package -DskipTests`

### 6.6 Ejecutar los tests

```bash
cd bodega_service-main
./mvnw test                          # JUnit 5 + Mockito (H2 en memoria)
./mvnw verify                        # incluye reporte JaCoCo
```

El reporte de cobertura queda en `target/site/jacoco/index.html`.

### 6.7 Ejecución remota

Al desplegar en un servidor (VM, contenedor o servicio cloud):

1. **Puertos**: cada servicio se puede sobrescribir sin tocar el código:

   ```bash
   java -jar usuario-ms-0.0.1-SNAPSHOT.jar --server.port=8081
   ```

2. **Base de datos**: apuntar a la instancia remota mediante variables de entorno o
   argumentos:

   ```bash
   java -jar usuario-ms-0.0.1-SNAPSHOT.jar \
     --spring.datasource.url=jdbc:mysql://<host-bd>:3306/usuariosdb \
     --spring.datasource.username=<usuario> \
     --spring.datasource.password=<clave>
   ```

3. **Gateway**: reemplazar los `uri: http://localhost:{puerto}` de
   `ecomarket-gateway/src/main/resources/application.yml` por los host reales de cada
   microservicio, por ejemplo `uri: http://ms-usuario:8081` (nombre de servicio en
   Docker/Kubernetes) o `uri: http://10.0.0.15:8081`.

4. **Acceso**: publicar únicamente el puerto **8200** del Gateway hacia el exterior y
   mantener los microservicios en la red interna. El consumo queda entonces como:

   ```
   http://<host-publico>:8200/api/v1/productos
   ```

5. **Producción**: usar `spring.jpa.hibernate.ddl-auto=validate` (en lugar de `update`)
   y `spring.jpa.show-sql=false`.

---

## 7. Estructura del repositorio

```
EcoMarketVersionFinal/
├── ecomarket-gateway/            # API Gateway (8200)
├── usuario-ms-master/            # 8081
├── proveedor-ms-master/          # 8082
├── carro-ms-master/              # 8083
├── sucursal-ms-master/           # 8084
├── reabastecimiento-ms-master/   # 8085
├── factura-master/               # 8087
├── catalogo-main/                # 8090
├── cupon-master/                 # 8091
├── pedido-master/                # 8093
├── soporte_service-main/         # 9091
├── autentificacion_service-main/ # 9092
├── inventario_service-main/      # 9093
├── bodega_service-main/          # 9094
├── envio_service-main/           # 9095
└── README.md
```

Estructura interna de cada microservicio:

```
<microservicio>/
├── src/main/java/ecomarket/<servicio>/
│   ├── controller/     # Endpoints REST (@RestController)
│   ├── service/        # Lógica de negocio (@Service)
│   ├── repository/     # Acceso a datos (JpaRepository)
│   ├── model/          # Entidades JPA
│   └── dto/            # Objetos de transferencia
├── src/main/resources/
│   └── application.properties
├── src/test/java/      # Pruebas unitarias e integración
└── pom.xml
```

---

## 8. Flujo de compra de referencia

```
Usuario → autentificacion → catalogo → carro → cupon → pedido → factura → envio
                                          ↓
                                     inventario ← bodega ← reabastecimiento ← proveedor
```

1. El cliente se registra (`usuario-ms`) y se autentica (`autentificacion`).
2. Explora el catálogo (`catalogo`) y agrega productos al carro (`carro-ms`).
3. Aplica un cupón de descuento (`cupon`).
4. Genera el pedido (`pedido-ms`), que descuenta stock en `inventario`.
5. Se emite la factura (`factura-ms`) a partir del pedido.
6. Se crea el envío con su ruta (`envio_service`).
7. Cualquier incidencia posterior se atiende vía `soporte`.
