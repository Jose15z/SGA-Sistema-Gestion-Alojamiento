# Guía 12 Seguridad con JWT en Siete Raíces

Universidad del Quindío · Ingeniería de Sistemas y Computación · Programación Avanzada. Fecha de verificación: 5 de noviembre de 2026.

La implementación completa la actividad 7.5 sobre el SGA existente. Incluye autenticación con credenciales persistidas en H2, JWT HS256 de 30 minutos, autorización por rol y titular, registro público de huéspedes y evidencia reproducible. El dominio se conserva sin cambios. El documento de entrega es `evidencias/guia12/Seguimiento_SGA_Solo_Paginas_Intermedias.docx`.

## Implementación y decisiones

| Requisito | Implementación |
|---|---|
| Spring Security 7 y OAuth2 Resource Server | Starter de Spring Boot 4 `spring-boot-starter-security-oauth2-resource-server`; `SeguridadConfig` configura una cadena stateless, sin sesión, CSRF deshabilitado para Bearer y rechazo por defecto de rutas no declaradas. |
| Clave externa obligatoria | `ClaveJwt` lee primero SGA_JWT_SECRET y luego sga.jwt.secret. Sin clave, con plantilla sin reemplazar o con menos de 32 bytes el arranque falla. No se genera una clave implícita. |
| Configuración local | application.properties importa opcionalmente application-secrets.properties; la plantilla .example se versiona y el archivo real está ignorado. |
| Firma y validación | Nimbus firma y valida únicamente HS256. Se validan emisor, vigencia y presencia de exp, sub, documento y un rol admitido. Se mantiene la tolerancia de reloj estándar de Spring Security; la prueba de expiración usa 120 segundos de vencimiento. |
| Claims | iss, sub, iat, exp, documento, roles. Se conserva además rol para compatibilidad con el Solicitante existente; el adaptador lo convierte a autoridades sin prefijo ROLE_. No viajan contraseñas ni fecha de nacimiento. |
| Usuarios iniciales | `DefaultUserProperties` valida los cinco usuarios configurados de Siete Raíces; `DefaultUserInitializer` los persiste mediante JPA y PasswordEncoder. Es idempotente y no sobrescribe contraseñas de cuentas existentes. |
| BCrypt | DelegatingPasswordEncoder almacena {bcrypt} seguido del hash. El login usa el puerto CifradorContrasenas, implementado con PasswordEncoder.matches. |
| Login | `IniciarSesionUseCase` valida usuario activo y contraseña; `AutenticacionController` responde token, tipo Bearer, expiraEn, correo, rol y roles. Credenciales erradas, cuenta inexistente e inactiva responden 401 con mensaje genérico. |
| Propietario | `PropietarioReservaVerificador` consulta por el puerto ReservaRepository y compara el documento con el titular. Se usa en @PreAuthorize para consulta, cancelación y otras acciones del huésped, y también para consulta de folios. |
| Errores | `GlobalExceptionHandler` conserva 400, 401, 403, 404 y 409; `RespuestaErrorSeguridad` implementa el desafío opcional y devuelve ErrorResponse desde los filtros, con WWW-Authenticate en los 401. |
| Documentación | Contrato OpenAPI con login y registro públicos, bearerAuth, respuestas 401 y 403 en toda operación protegida y esquema roles. Swagger muestra Authorize. |

Se reutilizan `Usuario`, `Rol`, los puertos y casos de uso de la capa application que ya existían. `UsuarioEntity` y los adaptadores JWT/JPA permanecen en infrastructure. No se añade otro modelo de credenciales ni un segundo login paralelo con UserDetails: el caso de uso existente ya delega la comparación segura a PasswordEncoder a través de su puerto. Esta adaptación conserva la arquitectura hexagonal y los contratos del SGA, con los comportamientos exigidos por la guía.

## Registro público y canales

POST /api/auth/registro exige nombreCompleto, correo, contrasena, telefono, documento y fechaNacimiento. Bean Validation exige correo con formato válido, fecha pasada y contraseña de 8 a 72 caracteres con mayúscula y número. El caso de uso fuerza HUESPED, aunque el cliente intente enviar ADMINISTRADOR, y responde 409 por correo o documento existente. El DTO conserva el nombre `nombreCompleto` del contrato del proyecto.

La relación entre usuario, canal y titular se valida en `Solicitante.verificarPuedeReservar`, en application: es una regla de acceso, porque depende de la identidad autenticada. Un huésped crea solo PORTAL y a su nombre; recepción solo DIRECTO; administración conserva también EXTERNO para el flujo preexistente. Disponibilidad, capacidad, anticipo y transiciones siguen en el dominio. El controlador y los casos de uso obtienen al solicitante del JWT, nunca del JSON enviado por el cliente.

La matriz completa, con una fila por cada operación, está en [api/MATRIZ_SEGURIDAD_GUIA_12.md](api/MATRIZ_SEGURIDAD_GUIA_12.md). Incluye todos los endpoints administrativos y operativos, ya implementados en este proyecto. El listado de reservas ahora está reservado a recepción y administración, conforme a la tabla 7.1 de la guía.

## Corrección detectada al verificar

La prueba de crear y cancelar una reserva propia dentro de una transacción detectó que el mapper del folio producía listas inmutables. Hibernate necesita modificar esas colecciones durante merge y lanzaba UnsupportedOperationException. `FolioPersistenceMapper` ahora crea ArrayList para cargos y pagos. La prueba comprueba creación 201 y cancelación 200 con estado CANCELADA. No cambia reglas del dominio.

También se ajustó la comprobación del personal en el calendario al formato de autoridades sin ROLE_, para conservar que solo recepción y administración vean los códigos de reserva. Una prueba verifica que el huésped y el visitante anónimo reciban únicamente disponibilidad.

## Ejecutar y reproducir

Requisitos: JDK 25 y Python 3 para el recorrido HTTP. El wrapper descarga Gradle y las dependencias. No se necesita instalar librerías de Python para verificar la API.

1. Generar la clave local según el README o configurar SGA_JWT_SECRET. No versionar el archivo real ni pegar la clave en evidencias.
2. Ejecutar `./gradlew build`. Las pruebas usan una clave ficticia exclusiva de pruebas definida en build.gradle; el arranque normal sigue exigiendo una clave externa.
3. Ejecutar `./gradlew bootRun`. Para usar otro puerto: `./gradlew bootRun --args='--server.port=8081'`.
4. Ejecutar `python3 scripts/verificar_guia12.py` o añadir `--base-url http://localhost:8081`. El script verifica los casos HTTP, guarda respuestas sanitizadas y termina con código 1 si algún estado no coincide.
5. Abrir `docs/pruebas/guia12.http` en VS Code REST Client. Ejecutar los cuatro logins y luego los casos 3 a 14. Para el token vencido, copiar el valor auxiliar de `build/guia12/tokens.json`, que Git ignora.
6. En Swagger, pulsar Authorize y usar un token local del login. No enviar el token a servicios externos. En H2 consultar `SELECT correo, rol, contrasena FROM usuarios`; la tabla del proyecto se llama usuarios.
7. Revisar `git status --ignored` y `git check-ignore src/main/resources/application-secrets.properties` antes de realizar el commit.

Los cinco usuarios y sus contraseñas de demostración están en el README. Ana (1094123456) es titular de RES-2025-00001 y Carlos (1094555666) permite probar acceso cruzado. Se usan los datos de Siete Raíces del repositorio en lugar de crear usuarios sga.local desconectados de las reservas existentes.

## Evidencias y criterios de verificación

- `evidencias/guia12/build.txt`: salida de Gradle y conteo agregado de resultados JUnit, sin tokens.
- `evidencias/guia12/pruebas-resumen.json`: número de pruebas, fallos, errores y suites obtenido de los XML de JUnit.
- `evidencias/guia12/arranque.txt`: arranque real y mensaje de cinco usuarios cargados en H2.
- `evidencias/guia12/http-resultados.json`: peticiones reales a localhost y respuestas esperadas/obtenidas. Los tokens de login se sustituyen por OMITIDO.
- `evidencias/guia12/swagger-bearer.png`: captura real del esquema Bearer de Swagger, sin credenciales.
- `Guia12SeguridadTest`: login real, usuarios BCrypt e inicializador idempotente, registro, roles, titular, canal, folio, calendario, token vencido firmado, emisor incorrecto firmado, token alterado y algoritmo none.
- `ClaveJwtTest`: rechazo de clave ausente, corta o plantilla sin reemplazar.
- `HerramientasDeshabilitadasTest`: cierre de Swagger, documentación OpenAPI y H2 al deshabilitar herramientas de desarrollo.
- `ContratoOpenApiTest`: correspondencia de rutas, DTOs y enums con el código; respuestas 401 y 403 en todas las operaciones protegidas.

Las pruebas de integración usan SpringBootTest, MockMvc, los filtros reales, H2 y JWT firmados; no sustituyen el decodificador con mocks. Los casos que modifican datos hacen rollback. La prueba de credenciales inactivas verifica el login; los JWT ya emitidos siguen siendo válidos hasta expirar, ya que no se implementa revocación anticipada.

## Alcance de despliegue

El entorno entregado es de desarrollo con H2 en memoria y datos de demostración; al reiniciar se recrean los datos. Para despliegue se requiere HTTPS, una clave propia, deshabilitar las herramientas de desarrollo y reemplazar los usuarios de prueba. No se implementan refresh tokens ni limitación de intentos, que quedan fuera de las partes obligatorias de esta guía.

Referencia técnica: [Spring Security OAuth2 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html), especialmente JwtValidators.createDefaultWithIssuer, y la guía 12 suministrada por el curso.
