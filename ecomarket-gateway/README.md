# EcoMarket 2.0 — API Gateway

API Gateway construido con **Spring Cloud Gateway** (variante reactiva WebFlux) que actúa como punto de entrada único a los 15 microservicios de EcoMarket 2.0. Los clientes solo conocen el gateway (puerto **8080**) y este enruta cada petición al microservicio correspondiente.

## Stack

- Spring Boot 4.0.7
- Java 25
- Spring Cloud 2025.1.2
- `spring-cloud-starter-gateway-server-webflux`

## Cómo ejecutar

```bash
./mvnw spring-boot:run
```

El gateway queda escuchando en `http://localhost:8080`. Cada microservicio debe estar levantado en su propio puerto para que el enrutamiento funcione.

## Tabla de rutas

Todas las peticiones entran por `http://localhost:8080` y se redirigen según el path:

| Ruta (id)            | Path                                                                 | Destino |
|----------------------|----------------------------------------------------------------------|---------|
| ms-usuario           | `/api/v1/usuarios/**`                                                | 8081    |
| ms-proveedor         | `/api/v1/proveedores/**`                                             | 8082    |
| ms-carro             | `/api/v1/carros/**`                                                  | 8083    |
| ms-sucursal          | `/api/v1/sucursales/**`                                              | 8084    |
| ms-reabastecimiento  | `/api/v1/reabastecimientos/**`                                       | 8085    |
| ms-factura           | `/api/v1/facturas/**`                                                | 8087    |
| ms-catalogo          | `/api/v1/productos/**`, `/categorias/**`, `/catalogos/**`, `/resenias/**` | 8090    |
| ms-cupon             | `/api/v1/cupones/**`                                                 | 8091    |
| ms-pedido            | `/api/v1/pedidos/**`, `/api/v1/direcciones/**`                       | 8093    |
| ms-soporte           | `/api/v1/soporte/**`                                                 | 9091    |
| ms-autentificacion   | `/api/v1/autentificacion/**`                                         | 9092    |
| ms-inventario        | `/api/v1/inventario/**`                                              | 9093    |
| ms-bodega            | `/api/v1/bodega/**`                                                  | 9094    |
| ms-envio             | `/api/v1/envios/**`, `/api/v1/rutas/**`                              | 9095    |

> El catálogo agrupa cuatro recursos (productos, categorías, catálogos y reseñas) que comparten el mismo servicio en el puerto 8090. Pedido y envío también agrupan más de un recurso.

## Ejemplo

```
Cliente → GET  http://localhost:8080/api/v1/productos/5
Gateway →     reenvía a → http://localhost:8090/api/v1/productos/5
```

## Notas y posibles mejoras

- Las URIs están definidas con `http://localhost:PUERTO`. Para un despliegue real (Docker / Kubernetes) conviene reemplazarlas por el nombre del servicio o integrar **Eureka** + `lb://` para balanceo de carga y descubrimiento dinámico.
- No se conserva ningún prefijo extra: el path llega al microservicio tal cual, por lo que no se usa el filtro `StripPrefix`.
