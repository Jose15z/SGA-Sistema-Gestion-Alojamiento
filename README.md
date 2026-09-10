# SGA - Sistema de Gestión de Alojamiento

Proyecto desarrollado para la asignatura **Programación Avanzada** del programa de Ingeniería de Sistemas y Computación de la **Universidad del Quindío**.

El SGA tiene como propósito administrar un alojamiento turístico compuesto por apartamentos independientes, permitiendo gestionar apartamentos, reservas, estancias, ocupantes, disponibilidad, temporadas, tarifas, folios, pagos y bloqueos.

El proyecto se desarrolla aplicando principios de **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal**, manteniendo el lenguaje ubicuo del dominio en español.

---

## Integrantes

- Jose Manuel Rodriguez Ospina
- Daniela Muñoz Arboleda
- Adrian David Navarro

---

## Tecnologías

- Java 25
- Spring Boot 4.1.1
- Gradle - Groovy DSL
- Spring Web MVC
- Spring Validation
- Spring Data JPA
- H2 Database
- JUnit
- Git y GitHub

---

## Arquitectura

El proyecto sigue una estructura basada en Arquitectura Hexagonal:

```text
co.edu.uniquindio.sga
├── domain
│   ├── entity
│   ├── valueobject
│   └── exception
├── application
└── infrastructure
```

### Domain

Contiene los conceptos y reglas propias del negocio.

El dominio se mantiene independiente de frameworks y tecnologías externas. Las clases ubicadas en este paquete no dependen de Spring, JPA ni Lombok.

### Application

Contendrá los casos de uso de la aplicación y coordinará las operaciones del dominio.

### Infrastructure

Contendrá los adaptadores técnicos necesarios para interactuar con elementos externos, como la API REST y la persistencia.

---

## Modelo de dominio actual

Actualmente se encuentra implementada la primera parte del modelo de dominio.

### Entidades

- `Apartamento`
- `Ocupante`
- `Reserva`

Las entidades poseen identidad propia, identificadores inmutables y su igualdad se determina únicamente mediante dicha identidad.

### Objetos de valor

- `Capacidad`
- `Estancia`
- `EstadoReserva`
- `EstadoOperativo`
- `CanalOrigen`
- `CodigoReserva`
- `IdentificacionApartamento`
- `DocumentoIdentidad`
- `UmbralEdadFacturable`
- `FechaCreacion`

Los objetos de valor son inmutables y representan conceptos del lenguaje ubicuo del SGA.

### Excepción del dominio

- `ReglaDominioException`

Se utiliza para representar violaciones de reglas propias del negocio y diferenciarlas de errores técnicos.

---

## Reglas de negocio implementadas

### RN-02 - Capacidad del apartamento

El número total de ocupantes de una reserva no puede superar la capacidad definida para el apartamento.

Todos los ocupantes cuentan para la capacidad, sean facturables o no.

```text
totalOcupantes <= capacidad
```

### RN-03 - Estancia mínima

Toda estancia debe tener como mínimo una noche.

La fecha de salida debe ser posterior a la fecha de entrada.

```text
fechaSalida > fechaEntrada
```

El intervalo de una estancia se interpreta como:

```text
[fechaEntrada, fechaSalida)
```

Por ejemplo, una estancia del 10 al 12 corresponde a dos noches.

### RN-04 - Reservas hacia el pasado

No es posible crear una nueva reserva cuya fecha de entrada sea anterior a la fecha actual.

### RN-06 - Ocupante facturable

Un ocupante genera cargo cuando su edad en la fecha de entrada alcanza o supera el umbral de edad facturable configurado por el alojamiento.

La edad no se almacena. Se calcula a partir de la fecha de nacimiento.

---

## Lenguaje ubicuo

Los conceptos propios del negocio se mantienen en español tanto en el modelo como en el código.

Ejemplos:

```text
Apartamento
Reserva
Estancia
Ocupante
Titular
Capacidad
Tarifa
Folio
Pago
Bloqueo
```

Se evitan términos genéricos o traducciones que no pertenecen al lenguaje ubicuo:

```text
Room
Booking
Guest
CheckIn
Invoice
```

Los paquetes estructurales utilizan vocabulario técnico en inglés:

```text
domain
application
infrastructure
```

---

## Estructura actual del dominio

```text
domain/
├── entity/
│   ├── Apartamento.java
│   ├── Ocupante.java
│   └── Reserva.java
│
├── valueobject/
│   ├── CanalOrigen.java
│   ├── Capacidad.java
│   ├── CodigoReserva.java
│   ├── DocumentoIdentidad.java
│   ├── Estancia.java
│   ├── EstadoOperativo.java
│   ├── EstadoReserva.java
│   ├── FechaCreacion.java
│   ├── IdentificacionApartamento.java
│   └── UmbralEdadFacturable.java
│
└── exception/
    └── ReglaDominioException.java
```

---

## Requisitos

Para ejecutar el proyecto es necesario tener instalado:

- JDK 25
- Git

No es necesario instalar Gradle manualmente, ya que el proyecto utiliza **Gradle Wrapper**.

---

## Ejecución

Clonar el repositorio:

```bash
git clone https://github.com/Jose15z/SGA-Sistema-Gestion-Alojamiento.git
```

Entrar al proyecto:

```bash
cd SGA-Sistema-Gestion-Alojamiento
```

Compilar y ejecutar las pruebas:

```bash
./gradlew build
```

Ejecutar la aplicación:

```bash
./gradlew bootRun
```

La aplicación se ejecuta por defecto en:

```text
http://localhost:8080
```

---

## Base de datos H2

Durante el desarrollo se utiliza una base de datos H2 en memoria.

La consola se encuentra disponible en:

```text
http://localhost:8080/h2-console
```

Configuración:

```text
JDBC URL: jdbc:h2:mem:sgadb
Usuario: sa
Contraseña: vacía
```

Los datos almacenados en H2 se eliminan cuando se detiene la aplicación.

---

## Estado actual del proyecto

### Completado

- Configuración inicial de Spring Boot y Gradle.
- Configuración de Java 25.
- Configuración de H2.
- Estructura inicial de Arquitectura Hexagonal.
- Modelado inicial del dominio.
- Implementación de `Apartamento`.
- Implementación de `Ocupante`.
- Implementación de `Reserva`.
- Implementación de objetos de valor iniciales.
- Implementación de `FechaCreacion` como objeto de valor.
- Implementación de RN-02.
- Implementación de RN-03.
- Implementación de RN-04.
- Implementación de RN-06.

### Próximos pasos

- Implementar pruebas unitarias del dominio.
- Completar el modelo de dominio.
- Implementar `Temporada`.
- Implementar `Tarifa`.
- Implementar `Folio`.
- Implementar `Cargo`.
- Implementar `Pago`.
- Implementar `Bloqueo`.
- Incorporar agregados y servicios de dominio.
- Implementar casos de uso.
- Implementar adaptadores de persistencia.
- Implementar API REST.
- Desarrollar el frontend del SGA.

---

## Principios de diseño

El desarrollo del SGA busca mantener un modelo de dominio rico.

Por esta razón:

- Las entidades no utilizan setters genéricos.
- Las reglas del negocio viven dentro del dominio.
- Los objetos de valor son inmutables.
- Las entidades se comparan por identidad.
- El dominio no depende de Spring, JPA ni otros frameworks.
- Se utiliza el lenguaje ubicuo definido para el SGA.
- Los valores configurables del alojamiento no se encuentran quemados en el código.

---

## Información académica

**Universidad del Quindío**

Programa de Ingeniería de Sistemas y Computación

Asignatura: **Programación Avanzada**

Proyecto: **SGA - Sistema de Gestión de Alojamiento**