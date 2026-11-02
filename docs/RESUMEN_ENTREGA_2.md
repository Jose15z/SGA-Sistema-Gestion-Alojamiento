# Resumen de la Entrega 2 y del aporte de José

Revisión del 31 de octubre de 2026. La Entrega 2 convierte el modelo de dominio
de la primera entrega en un backend ejecutable: una API REST con Spring Boot,
persistencia en H2, seguridad JWT y documentación para probar sus operaciones.

## Estado de las ramas

Se ejecutó `git fetch origin` y se consultaron las ramas remotas. No se encontraron
commits nuevos publicados desde la revisión del 30 de octubre:

| Rama | Último commit remoto | Estado |
|---|---|---|
| `main` | `e6f708c` | Integra el trabajo de Daniela mediante el PR #1, del 28 de octubre. |
| `entrega-2-daniela-munoz` | `0ab9049` | Base del backend revisado. |
| `entrega-2-jose` | `f0b5ed4` | Incluye las pruebas Mockito y las normas de la Ficha. |

Esta comprobación solo abarca los commits publicados en este repositorio. Los
cambios que un compañero tenga únicamente en su computador no aparecen en GitHub.

## Qué implementa la Entrega 2

| Área | Implementación |
|---|---|
| Dominio | Entidades, objetos de valor, agregados y servicios que protegen capacidad, disponibilidad, precios, estados y políticas. El dominio sigue separado de Spring y JPA. |
| Casos de uso | Coordinan creación, consulta, modificación, confirmación y cancelación de reservas, además de las operaciones de administración y seguridad. |
| API REST | Controllers por recurso, DTOs record, mappers MapStruct, Bean Validation y manejo uniforme de errores. Hay 57 operaciones documentadas. |
| Persistencia | Adaptadores JPA, entidades separadas y H2 con carga automática de usuarios, siete apartamentos, tarifas, temporadas, políticas y reservas de prueba. |
| Seguridad | Registro público de huéspedes, inicio de sesión con JWT, contraseñas BCrypt, permisos por rol y verificación de propietario de reservas y folios. |
| Consultas | Disponibilidad por fechas y grupo, filtros, valor estimado y listados paginados. Reservas ordenadas de la más reciente a la más antigua. |
| Operación adicional | Llegada, salida, no-show, preparación de apartamentos, vencimiento automático, folios, cargos, pagos y cierre. Parte de esta operación diaria supera el mínimo del corte. |
| Documentación | OpenAPI, Swagger con Authorize, colección Postman, diagramas y matriz de trazabilidad con endpoints. |

La base del backend de esta entrega está en los commits de Daniela del 28 de
octubre. El diseño y código de la Entrega 1 tienen contribuciones anteriores del
equipo. Esta revisión no atribuye a José todo el backend existente.

## Qué aportó José

### Guardado y publicado en su rama

1. `ee1f35f`: añadió 26 pruebas Mockito para crear, cancelar y modificar reservas,
   registrar salida, declarar no-show y vencer reservas pendientes. Comprueban
   resultados, guardados, rechazo de operaciones e idempotencia del vencimiento.
2. `f0b5ed4`: sustituyó las normas iniciales de ejemplo por las cinco de la Ficha:
   capacidad, ruido, prohibición de fumar, cuidado de instalaciones y mascotas.

### Preparado localmente y pendiente de commit

- Revisión de la rúbrica y plan de cierre.
- Correspondencia de los 31 conceptos y 12 servicios del Excel disponible con el
  código, con explicación de los nombres y firmas que cambiaron.
- Revisión de la API: se conservan las 14 operaciones originales y se explican
  cambios como obtener el autor del JWT y recibir titular más acompañantes.
- Corrección del Swagger generado: los endpoints del perfil ya no piden un
  `solicitante` enviado por el cliente, pues se obtiene del JWT.
- Validación explícita de campos obligatorios en seis DTOs: evita que su ausencia
  se interprete como `0` o `false`, particularmente en servicios y tramos de política.
- Seis pruebas adicionales: cuatro del contrato (campos, enums, presencia de
  obligatorios y solicitante oculto) y dos HTTP para solicitudes incompletas.

Las pruebas del contrato cubren esos aspectos concretos; no certifican por sí solas
todas las restricciones, todos los códigos HTTP ni la semántica completa de la API.

## Cómo explicar el flujo en la sustentación

Una persona de recepción inicia sesión y obtiene un JWT. Envía una reserva con
el token, apartamento, fechas, titular y acompañantes. El controller valida la
forma y traduce los DTOs. El caso de uso consulta disponibilidad, cotización,
estancia mínima y política. El dominio crea la reserva PENDIENTE con su valor y
política congelados. La persistencia guarda la reserva y su folio en una misma
transacción. La API responde 201 con Location.

Después se registra el anticipo, se indica la hora y se confirma la reserva. Una
infracción de negocio responde 409. Un token ausente produce 401 y el acceso de
otro huésped a la reserva produce 403. El no-show usa las 20:00 como valor único.

## Verificación del 31 de octubre

- `./gradlew test`: BUILD SUCCESSFUL, 489 pruebas, sin fallos, errores ni omitidas.
- Swagger generado: ya no pide solicitante y sus campos obligatorios de requests
  coinciden con los del contrato.
- Newman: 179 solicitudes y 334 aserciones sin fallos.
- La aplicación usada para repetir la colección se inició limpia en el puerto
  8081, separada de la instancia abierta en 8080.

## Pendientes para cerrar la entrega

- El equipo debe definir la dirección exacta y las coordenadas: la Ficha solo
  indica Armenia, Quindío y `data.sql` todavía las identifica como ejemplos.
- Confirmar que el Excel revisado es la versión entregada: contiene 12 servicios,
  mientras la retroalimentación menciona trece. El código actual tiene 14.
- Guardar y publicar la documentación y las correcciones locales, revisar el PR
  hacia `main` y ensayar entre los tres el flujo, errores, roles y paginación.

Débito y crédito se conservan separados y documentados como medios admitidos.
Las integraciones externas, reportes e IA no se añaden para completar este corte.
