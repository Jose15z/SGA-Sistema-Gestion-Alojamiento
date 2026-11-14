# Verificación de la colección Postman de la Entrega 2

El 14 de noviembre de 2026 se ejecutaron las siete carpetas de la colección, en orden y en una iteración, sobre una instancia local del SGA recién iniciada con H2. Newman 6.2.1 completó **185 solicitudes y 349 aserciones, con cero fallos**.

La primera corrida, con la colección anterior, completó 179 solicitudes y tuvo cuatro aserciones fallidas de 334. Dos casos aún esperaban que Ana y Carlos pudieran usar el listado general de reservas. La política vigente de la Guía 12 reserva ese listado a recepción y administración y devuelve HTTP 403 a huéspedes. Se actualizaron esos dos casos y se conservaron las comprobaciones de acceso a reservas propias. El filtro de precio también se reforzó porque su aserción anterior aceptaba una respuesta vacía.

## Reproducción

Con Java 25 y Node.js, levante una instancia de prueba desde la raíz del repositorio. La clave se genera localmente y H2 se inicializa al arrancar.

```bash
SGA_JWT_SECRET="$(openssl rand -hex 32)" ./gradlew bootRun --args='--server.port=18082 --server.address=127.0.0.1'
```

En otra terminal, ejecute la colección completa sin seleccionar carpetas. Reinicie la API entre corridas, porque la colección crea y modifica datos en H2.

```bash
npx --yes newman@6.2.1 run docs/pruebas/SGA-Entrega2.postman_collection.json \
  --env-var baseUrl=http://127.0.0.1:18082/api
```

## Resultados observados

| Consulta | Resultado HTTP 200 |
| --- | --- |
| Reservas con `estado=CANCELADA` | 3 resultados. Por fecha de creación descendente: `RES-2026-00003`, `RES-2026-00001`, `RES-2025-00004`. |
| Canceladas con `size=1` y `page=0,1,2` en solicitudes separadas | Un resultado por página, en el mismo orden que la consulta completa y sin duplicados. Cada respuesta informa 3 elementos totales y 3 páginas. |
| Canceladas con `size=1&page=3` | Contenido vacío y el mismo total de 3 elementos y 3 páginas. |
| Reservas con `canal=EXTERNO` | `RES-2026-00002` y `RES-2025-00007`. Ambas son del canal externo. |
| Reservas con `titular=1094555666` | `RES-2025-00004`, `RES-2025-00002` y `RES-2025-00006`. Todas son de Carlos Gómez. |
| Reservas con `apartamento=APT-301` | Solo `RES-2025-00001`. |
| Reservas con ambas fechas de entrada en el día +20 | Solo `RES-2025-00001`, con entrada el 4 de diciembre de 2026. La consulta incluye ambos límites. |
| Apartamentos activos | 7 resultados, de `APT-101` a `APT-303` por identificación ascendente. El listado posterior al retiro omite `APT-401`. |
| Temporadas | 6 resultados por fecha de inicio ascendente, del 15 de junio de 2026 al 1 de diciembre de 2027. |
| Disponibilidad con precio máximo | La consulta sin límite devolvió 8 apartamentos. Al aplicar un máximo igual al menor valor observado (480 000 COP), quedó solo `APT-101`. |
| Disponibilidad con capacidad mínima 6 y balcón panorámico | Solo `APT-303`. |
| Disponibilidad con al menos 3 dormitorios | `APT-302` y `APT-303` por identificación ascendente. |

Los códigos de las reservas nuevas y las fechas concretas corresponden a esta corrida. La colección usa fechas relativas para repetir las comprobaciones.

## Resumen de Newman

| Métrica | Ejecutadas | Fallidas |
| --- | ---: | ---: |
| Iteraciones | 1 | 0 |
| Solicitudes | 185 | 0 |
| Scripts previos a la solicitud | 185 | 0 |
| Scripts de pruebas | 185 | 0 |
| Aserciones | 349 | 0 |
