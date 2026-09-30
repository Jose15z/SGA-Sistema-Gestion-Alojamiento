# Implementación de reservas — Guías 05 y 06

Actualización: 30 de septiembre de 2026. Cambios sin commit.

## Funcionalidad implementada

- Los ocho casos de uso: crear, confirmar, cancelar, registrar llegada, registrar salida, declarar no-show, vencer pendientes y consultar por estado.
- Creación: disponibilidad y capacidad de todo el grupo; bloqueos; tiempo de preparación en ambos sentidos; cotización por noche; mínimo de dos noches en Temporada Especial; descuento configurado; congelamiento del valor y política; apertura de folio.
- Confirmación: folio abierto, anticipo neto del 30 %, hora estimada y plazo de 24 horas. A las 24 horas exactas aún se permite; después se rechaza.
- Cancelación y no-show: política histórica, reverso del alojamiento más penalidad, cargos y pagos previos intactos. Los extras se conservan. El saldo a favor representa devolución pendiente; una devolución efectuada se registra como pago negativo con identidad, medio, fecha, concepto y autor.
- Llegada/salida: coordinación de reserva y apartamento. La salida requiere folio cerrado; el cierre con saldo positivo o negativo exige autorización registrada.
- Vencimiento: solo pendientes cuyo plazo se superó; reverso de alojamiento sin penalidad por cancelación voluntaria; ejecución repetida sin duplicar movimientos.
- Reserva: salida, no-show, novedades de llegada y modificación de estancia con ajuste, validación del nuevo desglose y conservación de la política.
- `Pago` se mantiene como entidad con identidad propia; `Cargo` es objeto de valor. Ambos son inmutables.

## Configuración y decisiones

`infrastructure/configuration/ParametrosAlojamiento.java` implementa el puerto de configuración. Su fábrica `sieteRaices()` contiene los valores de la ficha fuera del dominio: edad 10 años, entrada 15:00, salida 11:00, preparación 3 horas, confirmación 24 horas, anticipo 30 %, descuento 10 % desde 7 noches y no-show **20:00**.

El no-show se permite exactamente a las 20:00 del día de entrada, únicamente desde CONFIRMADA y sin novedad de llegada pendiente. Las novedades se registran y resuelven con autor, motivo y fecha en el historial.

La política inicial usa antelación exacta desde las 15:00 de la entrada: al menos 168 horas → 0 %; al menos 48 horas → 30 %; menos de 48 horas → 50 %. No-show → 50 %. Los tramos y la hora de referencia pertenecen a la versión histórica inmutable. Publicar otra versión no cambia las reservas existentes.

## Repositorios y alcance técnico

Los puertos de Folio, Bloqueo y Política están en `domain.repository`. En `infrastructure.repository.memory` existen adaptadores para reservas, apartamentos, folios, bloqueos y políticas, además de un generador secuencial de códigos por año. La versión anterior de política se conserva y no puede sobrescribirse.

Estos adaptadores son adecuados para ejercicios y pruebas de un solo hilo; no ofrecen durabilidad ni transacciones de base de datos. Los casos que escriben varios agregados deben ejecutarse dentro de una transacción y con control de concurrencia al conectar persistencia real. La verificación de disponibilidad por sí sola no evita dos ventas simultáneas. No se añadieron controladores REST, entidades JPA, scheduler ni frontend.

`TarifarioRepository` sigue siendo el puerto existente: el adaptador definitivo de tarifas/temporadas debe aportarse al ensamblar la aplicación. Las pruebas de flujos utilizan un tarifario determinista. No se inventaron fechas de Temporada Especial.

La modificación de estancia del agregado devuelve el ajuste; su orquestación con disponibilidad y folio es una operación posterior, distinta de los ocho casos de la Guía 06. También quedan fuera los otros servicios del catálogo general (parqueadero, conciliación externa, administración de bloqueos/retiro) y las capas posteriores del proyecto.

## Validación

Desde `sga/sga`:

```sh
./gradlew build --offline
```

Las pruebas nuevas verifican recorridos completos y rechazos sin mutaciones: anticipo insuficiente, horario y plazo exactos, política histórica, disponibilidad bidireccional, bloqueos, preservación de movimientos, cierre con autorización y secuencias de códigos. El ensamblaje de ejemplo está en `src/test/java/co/edu/uniquindio/sga/EscenarioReservas.java`.

Resultado verificado: `./gradlew build --offline` completó correctamente, con **392 pruebas, 75 nuevas, cero fallos, cero errores y cero omitidas**. `git diff --check` sin incidencias. El dominio continúa sin imports de Spring, JPA ni Lombok.
