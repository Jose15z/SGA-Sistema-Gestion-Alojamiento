# Siete Raíces · Diseño de la aplicación

SGA separa la búsqueda de alojamiento de la operación diaria. Las pantallas internas usan español de Colombia y muestran las opciones disponibles para el rol de la sesión. El backend aplica los permisos.

- **Navegación:** `frontend/src/datos/navegacion.ts` define las opciones y los inicios por rol. Los enlaces existentes a módulos retirados dirigen a una tarea disponible del mismo rol.
- **Tipografía:** DM Sans para el cuerpo y Manrope para los encabezados. Los estilos viven en `frontend/src/index.css`.
- **Color:** `--ink` #18251f para texto, `--green` #164d3a para acciones, `--cream` #f6f5ef para fondo, `--line` #e2e7e3 para separadores. Los estados incluyen su nombre, además de un color.
- **Pantallas internas:** reutilizan `EncabezadoPagina`, `Etiqueta` y `Cargando`. Las acciones por fila muestran su estado de guardado y las listas de operación se apilan en pantallas estrechas.
