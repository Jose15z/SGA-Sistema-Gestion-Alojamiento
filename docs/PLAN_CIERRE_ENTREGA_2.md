# Plan de cierre de la Entrega 2

Revisión del 30 de octubre de 2026, actualizada el 31 de octubre, sobre `entrega-2-jose`, creada desde
`origin/entrega-2-daniela-munoz` en `0ab9049`. El backend ya tiene una implementación
amplia. El inventario local de la Entrega 1 ya fue contrastado. Falta cerrar los datos
pendientes, confirmar la versión definitiva y preparar la demostración del equipo.
Una compilación correcta no equivale por sí sola a cumplir toda la rúbrica.

## 1. Alcance y fuentes

- Rúbrica de Evaluación de la Entrega 2 proporcionada por José.
- Retroalimentación del profesor compartida por José en esta revisión.
- Ficha del Alojamiento, especialmente A.2 a A.5.
- Documentos locales de la Entrega 1: casos de uso, diagramas y glosario.
- Contrato inicial de la API en el commit `c5dd520` y contrato actual.
- Código, pruebas y documentación de la rama de Daniela.

El corte exige usuarios y seguridad, alojamiento y apartamentos, temporadas,
tarifas y política, disponibilidad y reservas. La operación diaria y el folio ya
implementados se conservan y se verifican, pero no se añaden integraciones de
correo, IA, imágenes o canales externos para cerrar este corte.

## 2. Orden de trabajo y condiciones de cierre

| Paso | Trabajo | Estado y condición de cierre |
|---|---|---|
| 1 | Aislar el trabajo y verificar la base de Daniela | Hecho. Rama `entrega-2-jose`; la base pasó `./gradlew clean test`. |
| 2 | Añadir las seis suites con Mockito | Hecho. 26 pruebas nuevas de crear, cancelar, modificar, salida, no-show y vencimiento. Verifican resultados, persistencia y ausencia de guardados ante rechazos. |
| 3 | Corregir datos iniciales del alojamiento | Parcial. Las cinco normas se tomaron de la Ficha A.4. Falta que el equipo defina dirección exacta y coordenadas; siguen identificadas como ejemplos. |
| 4 | Contrastar API con Entrega 1 y código actual | Rutas verificadas: se conservan las 14 operaciones iniciales y hay 57 en el contrato actual. La prueba `ContratoOpenApiTest` compara métodos/rutas, campos de los DTOs, enums, presencia de obligatorios y que el solicitante JWT no sea un parámetro del cliente; no certifica toda la semántica ni todas las restricciones. Las diferencias principales de mensajes se explican abajo. |
| 5 | Contrastar inventario de conceptos y catálogo completo | Revisión estructural hecha: Los 31 conceptos y los 12 servicios del Excel local tienen correspondencia con el código. Correspondencias y firmas documentadas en `REVISION_ENTREGA_1.md`. Confirmar la versión entregada: el profesor menciona trece servicios. |
| 6 | Verificar la entrega ejecutable | Colección completa verificada: 179 solicitudes y 334 aserciones sin fallos sobre la aplicación recién arrancada. Repetir después de incorporar cambios posteriores. |
| 7 | Preparar sustentación e integrar | Pruebas y normas ya guardadas y publicadas en la rama de José (`ee1f35f` y `f0b5ed4`). Pendientes este commit de documentación, ensayo entre los integrantes, revisión cruzada y PR. |

## 3. Revisión por criterio de la rúbrica

“Evidencia” significa que se localizó código o se ejecutó una comprobación;
no es una estimación de la nota ni una certificación de cada invariante.

| Criterio | Peso | Evidencia localizada o ejecutada | Cierre pendiente |
|---|---:|---|---|
| Dominio | 15 % | Entidades, valores, servicios y puertos en `domain`; búsqueda sin imports de Spring, JPA o Lombok y sin setters en entidades. Hay pruebas de dominio y matriz RN/L/RP. | Contraste estructural completo en `REVISION_ENTREGA_1.md`. Validar que sea el Excel definitivo y explicar las invariantes de cada raíz con la sección 2 de `ENTREGA_2.md`. |
| Casos de uso | 15 % | Casos en `application.usecase`, configuración del negocio externa y seis suites Mockito añadidas. Existen pruebas de flujos con repositorios en memoria. | Nombres y firmas evolucionadas documentados en `REVISION_ENTREGA_1.md`; comprobar las decisiones entre agregados durante el ensayo. |
| API y DTOs | 10 % | Contrato de 57 operaciones; DTOs request/response; colección prueba creación, consultas y operaciones de negocio. | Revisar los cuerpos y códigos al incorporar cualquier cambio; la prueba de contrato cubre operaciones, campos, enums, presencia de obligatorios y el solicitante JWT oculto. No prueba todas las restricciones ni todos los códigos HTTP. |
| Controller, mapper y validación | 10 % | Controllers, MapStruct y `@Valid`; colección incluye errores 400 y 409. | En el contraste final, comprobar que ninguna regla documentada esté únicamente en un DTO, controller o consulta. |
| Excepciones | 10 % | `GlobalExceptionHandler`, errores de seguridad y `ErrorResponse`; pruebas HTTP de 400, 401, 403, 404 y 409. | Mantener el formato único al modificar endpoints. |
| Persistencia H2 | 15 % | Adaptadores JPA, entidades separadas, embebibles y carga automática de `data.sql`; flujos API ejecutados con H2. | Definir dirección/coordenadas y revisar todos los datos frente a la sección 12.3 del enunciado original. |
| Consultas y paginación | 5 % | Disponibilidad con filtros; listados con tamaño por defecto 10 y máximo 10; reservas ordenadas por creación descendente y desempate por id. | Demostrar filtros y segunda página durante el ensayo. |
| JWT | 10 % | Login, BCrypt, secreto por variable o clave aleatoria, roles y comprobación de propietario; colección prueba acceso cruzado entre dos huéspedes. | Revisar que el historial no contenga secretos reales y repetir la demostración de propietario. |
| Documentación | 5 % | OpenAPI, Swagger, Postman, diagramas y matriz ampliada en `ENTREGA_2.md`. | Confirmar la versión definitiva de E1 y consolidar los documentos que se entregarán con no-show a las 20:00. |
| Git | 5 % | Trabajo de Daniela conservado como base y rama propia de José. | Contribución verificable de cada integrante, revisión cruzada y PR sin subir directo a `main`. |

## 4. Correcciones del profesor

| Corrección solicitada | Situación encontrada y comprobación de cierre |
|---|---|
| Unificar no-show | `application.properties` define las 20:00; el caso de uso consulta la configuración. Las pruebas nuevas comprueban 19:59:59 y 20:00. La decisión ya figura en E1; la mención de 19:00 del PDF queda sustituida por ella. |
| Tres invariantes por raíz y coordinación | `ENTREGA_2.md`, sección 2, separa invariantes del agregado y condiciones entre agregados. El inventario local se contrastó en `REVISION_ENTREGA_1.md`; confirmar que sea la versión definitiva. |
| Casos adicionales del catálogo | Sección 4 con caso de uso y endpoint; incluye cotización, modificación, externo, vehículo, bloqueo y retiro. Correspondencia de todas las filas del catálogo local en `REVISION_ENTREGA_1.md`. |
| Identidad, reemplazo y ciclo de vida | Sección 3 con ejemplos de reserva, estancia, capacidad, ocupante, pago y política. |
| Razones y consecuencias de reglas propias | Sección 5 para RP-01, RP-02 y RP-03. |
| Recorrido hasta endpoint, validación y respuesta | Sección 8, ejemplo completo de reserva y posteriores errores de negocio. Ensayar ejecutándolo en Swagger o Postman. |

## 5. Decisiones y diferencias de contrato

### Medios de pago

Se conservan `EFECTIVO`, `TARJETA_DEBITO`, `TARJETA_CREDITO` y
`TRANSFERENCIA_BANCARIA`. La Ficha A.5 admite débito y crédito en una misma fila;
separarlos conserva ambos medios admitidos y permite distinguir su registro.
El ejemplo original de E1 ya usaba `TARJETA_CREDITO`. El enum y OpenAPI actuales
coinciden. No se introduce `TARJETA` como quinto medio ni se cambia el contrato.
El equipo puede revisar esta decisión sin que bloquee las pruebas actuales.

### Evolución desde el OpenAPI inicial

- Las 14 combinaciones de método y ruta de E1 permanecen en el contrato actual.
- Se añadieron operaciones de usuarios, seguridad y administración hasta llegar
  a 57 operaciones. Swagger generado también expone 57.
- El autor de una operación procede ahora del usuario autenticado por JWT;
  ya no se acepta que el cliente lo establezca en el cuerpo.
- Crear reserva recibe `titular` y `acompanantes`; E1 recibía `titular` y
  `ocupantes`. El mapper forma el grupo completo con el titular incluido.
- Los pagos usan un enum cerrado y mantienen los medios admitidos por la Ficha.
- El servidor documentado es local; el contrato inicial incluía además una URL
  de producción que no corresponde a un despliegue verificado.

Estos cambios se documentan como evolución del diseño. Para una revisión completa
de compatibilidad deben contrastarse también campos, restricciones y respuestas;
la comprobación de rutas, campos, enums y presencia de obligatorios no prueba por sí sola toda esa compatibilidad.

## 6. Reparto sugerido para acordar entre los tres

- José: revisar y explicar las nuevas pruebas Mockito, explicar la revisión API
  y preparar sus cambios en `entrega-2-jose`.
- Adrián: validar la revisión del inventario y catálogo frente a la versión
  entregada (12 servicios locales frente a 13 mencionados por el profesor); revisar
  los diagramas y los datos iniciales, y definir con el equipo la dirección y las coordenadas.
  Crear su rama desde la base acordada.
- Daniela: revisar los cambios propuestos y participar en el ensayo de H2,
  seguridad y endpoints. Integrar solo después de la revisión cruzada.

Este reparto es una propuesta; no representa asignaciones ya aceptadas ni
mensajes enviados a los compañeros.

## 7. Verificación reproducible y entrega

Resultado de esta revisión: `./gradlew clean test` finalizó con `BUILD SUCCESSFUL`;
51 suites, 489 pruebas, 0 fallos, 0 errores y 0 omitidas. La colección completa
terminó con 179 solicitudes y 334 aserciones sin fallos.

Desde la raíz del repositorio:

```bash
./gradlew clean test
./gradlew bootRun
```

Con esa aplicación recién iniciada, ejecutar en otra terminal:

```bash
npx newman run docs/pruebas/SGA-Entrega2.postman_collection.json
```

Reiniciar la aplicación antes de repetir la colección: modifica los datos de H2,
incluido el alojamiento. Para comprobar las cinco normas iniciales, consultar
`GET /api/alojamiento` antes de ejecutar la colección.

Revisar Swagger en `http://localhost:8080/swagger-ui.html`, iniciar sesión y usar
Authorize. Demostrar reserva válida, un 409 de negocio, un 401 sin token, un 403
por rol y otro por propietario, más filtros y paginación.

Antes de publicar cambios, revisar `git diff`, incluir solo archivos del trabajo
propio y crear un commit descriptivo. Publicar explícitamente la rama propia con
`git push -u origin entrega-2-jose`. El PR debe usar la base acordada por el equipo:
si el trabajo de Daniela aún no está en `main`, usar su rama para revisar solo el
aporte de José, o coordinar primero el PR de Daniela hacia `main`.

No dar por cerrada la entrega hasta resolver los datos pendientes, completar el
contraste con la versión definitiva de E1, verificar los cambios integrados y realizar el ensayo entre todos.

## 8. Actualización del 31 de octubre

`git fetch origin` y la consulta de ramas remotas no mostraron commits nuevos de
los compañeros respecto a la revisión anterior. `main` ya contiene la integración
de Daniela (`e6f708c`); la rama de José contiene `ee1f35f` y `f0b5ed4`.
La documentación del tercer commit seguía local y preparada, sin commit.

Se corrigieron el parámetro solicitante en Swagger del perfil y la presencia de
campos obligatorios en seis DTOs. Se añadieron cuatro pruebas de contrato y dos
HTTP. El [resumen de la entrega y los aportes](RESUMEN_ENTREGA_2.md) distingue
lo publicado de lo pendiente de commit. Las correcciones no cambian las rutas
ni las reglas del dominio.
