# SGA - Sistema de Gestión de Alojamiento · Siete Raíces

Proyecto de **Programación Avanzada** (Ingeniería de Sistemas y Computación, Universidad del Quindío). Backend del SGA para *Siete Raíces – Apartamentos Turísticos* (Armenia, Quindío), construido con **Domain-Driven Design** y **arquitectura hexagonal**, con el lenguaje ubicuo en español.

| Documento | Contenido |
|---|---|
| [docs/ENTREGA_2.md](docs/ENTREGA_2.md) | Lo implementado en la Entrega 2: correcciones de la sustentación, invariantes por agregado, casos de uso, matriz de trazabilidad ampliada con endpoints, matriz de roles y recorrido completo de una reserva |
| [docs/api/openapi.yaml](docs/api/openapi.yaml) | Contrato de la API (lo verifica `ContratoOpenApiTest`) |
| [docs/pruebas/SGA-Entrega2.postman_collection.json](docs/pruebas/SGA-Entrega2.postman_collection.json) | Colección de pruebas de la sustentación |
| [docs/diagramas/DiagramaDeClases.md](docs/diagramas/DiagramaDeClases.md) | Diagrama de clases actualizado |
| [IMPLEMENTACION_ENTREGA_1.md](IMPLEMENTACION_ENTREGA_1.md) | Estado al cierre de la Entrega 1 (Guías 05 y 06) |

## Integrantes

- Jose Manuel Rodriguez Ospina
- Daniela Muñoz Arboleda
- Adrian David Navarro

## Tecnologías

Java 25 · Spring Boot 4.1 (Web MVC, Validation, Data JPA, Security, OAuth2 Resource Server) · H2 · MapStruct 1.6 · Lombok (solo en infraestructura) · springdoc-openapi (Swagger UI) · JUnit 5 y Mockito · Gradle.

## Ejecución

Requisitos: JDK 25 y Git (Gradle se usa con el wrapper).

```bash
./gradlew test
```

```bash
./gradlew bootRun
```

La clave del JWT **no está en el repositorio**. Se entrega con la variable de entorno `SGA_JWT_SECRETO` (al menos 32 caracteres). Si no se define, la aplicación genera una clave aleatoria al arrancar y los tokens dejan de servir al reiniciar:

```bash
SGA_JWT_SECRETO='una-clave-larga-de-al-menos-32-caracteres' ./gradlew bootRun
```

| Recurso | URL |
|---|---|
| API | `http://localhost:8080/api` |
| Swagger UI (botón **Authorize** con el token de `/api/auth/login`) | `http://localhost:8080/swagger-ui.html` |
| Consola H2 (`jdbc:h2:mem:sgadb`, usuario `sa`, sin contraseña) | `http://localhost:8080/h2-console` |

Swagger UI muestra dos definiciones: el contrato `docs/api/openapi.yaml` (seleccionado por defecto) y el documento generado desde el código.

### Usuarios de prueba (cargados por `data.sql`)

| Correo | Contraseña | Rol |
|---|---|---|
| admin@sieteraices.co | Admin123* | ADMINISTRADOR |
| recepcion@sieteraices.co | Recepcion123* | RECEPCIONISTA |
| ana.ramirez@correo.com | Huesped123* | HUESPED |
| carlos.gomez@correo.com | Huesped123* | HUESPED |

Además se cargan los 7 apartamentos de la Ficha con sus tarifas, las temporadas Alta y Especial, la política de cancelación versión 1, un bloqueo y 5 reservas (RES-2025-00001 a RES-2025-00005) con sus folios. Las fechas de las reservas son relativas al día de arranque.

### Colección de pruebas

Con la aplicación **recién arrancada**, importar la colección en Postman y ejecutarla completa con el Runner, o desde la terminal:

```bash
npx newman run docs/pruebas/SGA-Entrega2.postman_collection.json
```

Cubre el flujo completo de una reserva, un 409 por cada regla que expone la API y los 400, 401, 403 y 404 (122 solicitudes, 225 aserciones). Para repetirla, reinicie la aplicación: la base H2 vive en memoria.

## Arquitectura

```text
co.edu.uniquindio.sga
├── domain                      ← Java puro: sin Spring, JPA ni Lombok
│   ├── entity                  Reserva, Apartamento, Folio, Bloqueo, PoliticaCancelacion, Usuario, Ocupante, Pago
│   ├── valueobject             Estancia, Capacidad, Temporada, CalendarioTemporadas, Tarifa, Dinero, ...
│   ├── service                 Servicios del catálogo (disponibilidad, cotización, retención, parqueadero, ...)
│   ├── repository              Puertos (uno por agregado) y Pagina, PeticionPagina, FiltroReservas
│   └── exception               ReglaDominioException (→ 409)
├── application
│   ├── usecase                 Casos de uso con @Service y @Transactional
│   ├── dto/request, response   DTOs record con Bean Validation y ErrorResponse
│   ├── exception               Recursos inexistentes (→ 404), acceso denegado (→ 403), credenciales (→ 401)
│   ├── port                    CifradorContrasenas, EmisorTokens
│   └── seguridad               Solicitante: verificación por propietario
└── infrastructure
    ├── rest                    Controllers, mappers MapStruct y GlobalExceptionHandler
    ├── persistence/jpa         Entidades JPA, embebibles, mappers y adaptadores de cada puerto
    ├── persistence/inmemory    Adaptadores en memoria para pruebas (no son beans)
    ├── security                JWT, BCrypt y matriz de roles
    ├── config                  DomainServiceConfig, parámetros de la Ficha, reloj, Swagger
    └── scheduling              Vencimiento automático de reservas PENDIENTE (RN-21)
```

## Principios de diseño

- Las reglas del negocio viven en el dominio; los casos de uso orquestan y los controllers solo validan la forma, traducen y delegan.
- Entidades con constructor controlado, sin setters y con igualdad por identidad; objetos de valor inmutables (`record`/`enum`) que se validan al construirse.
- Las transiciones de estado usan métodos con intención de negocio (`confirmar`, `cancelar`, `registrarLlegada`, `declararNoShow`), nunca un `cambiarEstado` genérico.
- Los parámetros del alojamiento (umbral de edad, horarios, preparación, plazo, anticipo, no-show a las **20:00**, descuento, mascotas, vehículos) están en `application.properties` y se cambian sin recompilar.
- Ningún registro de negocio se borra físicamente: retiro de apartamentos, levantamiento de bloqueos y desactivación de usuarios son eliminaciones lógicas.
