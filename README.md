# 🦝 RACCCASH 💰

<div align="center">

### Aplicación móvil para la gestión de finanzas personales

Proyecto académico desarrollado para la asignatura **Programación Orientada a Objetos II**.

![Kotlin](https://img.shields.io/badge/Kotlin-Android-purple?logo=kotlin)
![Android](https://img.shields.io/badge/Android-Studio-green?logo=android)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-blue?logo=jetpackcompose)
![Material 3](https://img.shields.io/badge/Material-3-orange?logo=materialdesign)
![Spring Boot](https://img.shields.io/badge/Spring-Boot-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue?logo=postgresql)
![Architecture](https://img.shields.io/badge/Architecture-Client--Server-red)
![License](https://img.shields.io/badge/License-Educational-lightgrey)

</div>

---

# 📖 Descripción

**RACCCASH** es una aplicación móvil desarrollada para ayudar a los usuarios a gestionar sus finanzas personales de forma sencilla, visual y organizada.

La aplicación permite registrar cuentas, ingresos, gastos, transferencias, deudas, metas de ahorro y presupuestos. Además, cuenta con una pantalla principal donde el usuario puede visualizar un resumen general de su situación financiera.

El proyecto está dividido en dos partes principales:

* **Frontend Android:** aplicación móvil desarrollada con Kotlin y Jetpack Compose.
* **Backend API REST:** servidor desarrollado con Java y Spring Boot, conectado a una base de datos PostgreSQL.

Esta separación permite que la app móvil se comunique con una API central, donde se procesan los datos, se aplica la lógica financiera y se almacena la información de cada usuario.

---

# 🎯 Objetivo del proyecto

El objetivo de **RACCCASH** es brindar una herramienta digital que permita al usuario llevar un mejor control de su dinero personal desde su teléfono móvil.

La aplicación busca facilitar tareas como:

* Registrar ingresos y gastos.
* Administrar cuentas financieras.
* Controlar deudas personales.
* Dar seguimiento a metas de ahorro.
* Crear presupuestos por periodo o categoría.
* Consultar un resumen financiero general.
* Mantener separada la información de cada usuario.

Desde el punto de vista académico, el proyecto aplica conceptos de **Programación Orientada a Objetos II**, consumo de APIs, arquitectura por capas, manejo de estados, modelos, repositorios, servicios y conexión entre frontend, backend y base de datos.

---

# 🧩 Arquitectura general

El sistema utiliza una arquitectura **cliente-servidor**.

```text
Aplicación Android
       |
       | Solicitudes HTTP con Retrofit
       v
Backend Spring Boot
       |
       | Repositorios JPA / Hibernate
       v
Base de datos PostgreSQL
```

La aplicación Android envía solicitudes al backend mediante Retrofit. El backend recibe los datos, valida la información, aplica la lógica correspondiente y guarda o consulta la información en PostgreSQL.

Para separar la información de cada usuario, el backend utiliza el identificador del usuario y lo recibe en las solicitudes privadas mediante el header:

```text
X-Usuario-Id
```

De esta forma, cada usuario visualiza únicamente sus propias cuentas, transacciones, deudas, metas de ahorro y presupuestos.

---

# ✨ Funcionalidades principales

## 🔐 Registro e inicio de sesión

La aplicación permite crear una cuenta de usuario e iniciar sesión con correo y contraseña.

Después del inicio de sesión, la app guarda localmente los datos básicos del usuario para permitir el acceso a las pantallas principales.

---

## 👤 Separación de datos por usuario

Cada usuario tiene su propia información financiera.
El sistema evita que un usuario visualice cuentas, movimientos, deudas, ahorros o presupuestos pertenecientes a otra persona.

Las categorías principales pueden funcionar como datos globales del sistema, mientras que la información financiera privada se filtra por usuario.

---

## 💳 Gestión de cuentas

El usuario puede administrar diferentes cuentas financieras.

Funciones principales:

* Crear cuentas.
* Consultar cuentas existentes.
* Editar información de una cuenta.
* Eliminar o desactivar cuentas.
* Manejar saldos iniciales y saldos actuales.
* Personalizar información como tipo, moneda y color.

Ejemplos de cuentas:

* Efectivo.
* Banco.
* Tarjeta.
* Cuenta personal.
* Cuenta de ahorro.

---

## 💸 Gestión de transacciones

La app permite registrar movimientos financieros asociados a las cuentas del usuario.

Tipos principales:

* Ingresos.
* Gastos.
* Transferencias.

Cada transacción puede modificar automáticamente el saldo de las cuentas relacionadas. Esto permite mantener actualizado el balance general del usuario.

---

## 🔎 Filtros y organización de transacciones

El usuario puede consultar y organizar sus movimientos de forma más clara mediante filtros y criterios de búsqueda.

Opciones utilizadas:

* Tipo de transacción.
* Cuenta.
* Categoría.
* Subcategoría.
* Fecha.
* Monto.
* Nombre o descripción del movimiento.

Esto facilita encontrar movimientos específicos y analizar mejor el comportamiento financiero.

---

## 🏷️ Categorías y subcategorías

RACCCASH permite clasificar los movimientos financieros mediante categorías.

Las categorías ayudan a ordenar los ingresos y gastos, mientras que las subcategorías permiten un control más personalizado según las necesidades del usuario.

Ejemplos:

* Comida.
* Transporte.
* Entretenimiento.
* Servicios.
* Compras.
* Educación.
* Salud.
* Otros.

---

## 🤝 Gestión de deudas

La aplicación incluye un módulo para administrar deudas personales.

Tipos de deuda:

* **Debo:** cuando el usuario debe dinero a otra persona.
* **Me deben:** cuando otra persona le debe dinero al usuario.

Funciones principales:

* Crear deudas.
* Consultar deudas registradas.
* Editar información de una deuda.
* Registrar pagos.
* Eliminar pagos.
* Revisar el historial de pagos.
* Consultar estado de la deuda.
* Controlar monto pagado y monto pendiente.

Estados posibles:

* Pendiente.
* Parcialmente pagada.
* Pagada.
* Cancelada.

---

## 🎯 Metas de ahorro

El módulo de ahorro permite crear metas financieras y registrar avances asociados a cada objetivo.

Funciones principales:

* Crear metas de ahorro.
* Definir monto objetivo.
* Registrar aportes.
* Consultar progreso.
* Visualizar porcentaje completado.
* Calcular monto restante.
* Revisar movimientos asociados a la meta.

Este módulo está separado de las categorías de transacciones para que el ahorro funcione como una sección principal dentro de la app.

---

## 📊 Presupuestos

La app permite crear presupuestos para controlar mejor los gastos durante un periodo determinado.

Funciones principales:

* Crear presupuestos.
* Definir monto límite.
* Asociar categorías.
* Consultar monto utilizado.
* Consultar monto restante.
* Visualizar avance del presupuesto.
* Editar o eliminar presupuestos.

Este módulo ayuda al usuario a evitar gastos excesivos y a mantener un mejor control financiero.

---

## 🏠 Dashboard o pantalla principal

La pantalla principal muestra un resumen general de la información financiera del usuario.

Puede incluir:

* Balance total.
* Ingresos del mes.
* Gastos del mes.
* Flujo neto.
* Movimientos recientes.
* Tendencia del balance.
* Resumen de cuentas.
* Información relevante de deudas, ahorros o presupuestos.

El objetivo del dashboard es permitir que el usuario tenga una vista rápida de su situación financiera.

---

## 🔒 Seguridad local con PIN

La aplicación permite configurar un PIN local de cuatro dígitos para proteger el acceso a la app.

Esta función sirve como una capa adicional de seguridad dentro del dispositivo, especialmente cuando el usuario desea proteger su información financiera personal.

---

## 🚪 Cierre de sesión

El usuario puede cerrar sesión desde la aplicación.
Al hacerlo, se eliminan los datos locales de sesión y la app vuelve a la pantalla de autenticación.

---

# 🛠️ Tecnologías utilizadas

## Frontend Android

| Tecnología        | Uso dentro del proyecto                        |
| ----------------- | ---------------------------------------------- |
| Kotlin            | Lenguaje principal de desarrollo Android       |
| Android Studio    | Entorno de desarrollo del frontend             |
| Jetpack Compose   | Creación de interfaces modernas                |
| Material 3        | Diseño visual de la aplicación                 |
| ViewModel         | Manejo de lógica y estado de pantallas         |
| StateFlow         | Manejo reactivo de estados                     |
| Coroutines        | Operaciones asíncronas                         |
| Retrofit          | Comunicación con la API REST                   |
| OkHttp            | Cliente HTTP                                   |
| Gson Converter    | Conversión de JSON a objetos Kotlin            |
| SharedPreferences | Almacenamiento local de sesión y configuración |

---

## Backend

| Tecnología         | Uso dentro del proyecto              |
| ------------------ | ------------------------------------ |
| Java 17            | Lenguaje principal del backend       |
| Spring Boot 3.3.5  | Framework principal para la API      |
| Spring Web         | Creación de endpoints REST           |
| Spring Data JPA    | Acceso a datos mediante repositorios |
| Hibernate          | Mapeo objeto-relacional              |
| Jakarta Validation | Validación de datos                  |
| Maven              | Gestión de dependencias del backend  |
| PostgreSQL         | Base de datos principal              |
| Dockerfile         | Preparación para despliegue          |
| Spring Boot Test   | Apoyo para pruebas del backend       |

---

## Despliegue y conexión

| Elemento            | Descripción                                                       |
| ------------------- | ----------------------------------------------------------------- |
| Render              | Plataforma utilizada para mantener el backend disponible en línea |
| API REST            | Comunicación entre app móvil y backend                            |
| JSON                | Formato utilizado para enviar y recibir datos                     |
| Header X-Usuario-Id | Identificador usado para separar los datos de cada usuario        |

---

# 🔁 Flujo general de uso

1. El usuario abre la aplicación.
2. Si no existe una sesión guardada, se muestran las pantallas de login o registro.
3. El usuario se registra o inicia sesión.
4. El backend devuelve los datos básicos del usuario.
5. La app guarda localmente el identificador del usuario.
6. Las solicitudes privadas se envían al backend usando el header `X-Usuario-Id`.
7. El backend filtra la información correspondiente al usuario autenticado.
8. El usuario puede gestionar cuentas, transacciones, deudas, metas de ahorro y presupuestos.
9. La app muestra los datos actualizados en las pantallas correspondientes.
10. El usuario puede cerrar sesión o proteger el acceso local mediante PIN.

---

# 📌 Requisitos funcionales principales

* El sistema debe permitir registrar usuarios.
* El sistema debe permitir iniciar sesión con correo y contraseña.
* El sistema debe mantener la sesión local del usuario.
* El sistema debe separar la información financiera por usuario.
* El usuario debe poder crear, consultar, editar y eliminar cuentas.
* El usuario debe poder registrar ingresos, gastos y transferencias.
* El sistema debe actualizar los saldos de las cuentas al modificar transacciones.
* El usuario debe poder filtrar y ordenar transacciones.
* El usuario debe poder utilizar categorías y subcategorías.
* El usuario debe poder crear, consultar, editar y eliminar deudas.
* El usuario debe poder registrar pagos de deudas.
* El sistema debe actualizar el estado de una deuda según sus pagos.
* El usuario debe poder crear, editar y eliminar metas de ahorro.
* El usuario debe poder registrar movimientos asociados a metas de ahorro.
* El usuario debe poder crear, editar y eliminar presupuestos.
* El usuario debe poder consultar el avance de sus presupuestos.
* El usuario debe poder visualizar un resumen financiero general.
* El usuario debe poder configurar un PIN de seguridad local.
* El usuario debe poder cerrar sesión.

---

# 🧠 Relación con Programación Orientada a Objetos

El proyecto aplica conceptos importantes de Programación Orientada a Objetos y desarrollo por capas.

## Encapsulamiento

Se utilizan clases y modelos para representar entidades como usuarios, cuentas, transacciones, deudas, metas de ahorro y presupuestos.

## Abstracción

La lógica del sistema se separa en componentes específicos como modelos, servicios, repositorios, controladores y ViewModels.

## Reutilización

Los componentes de interfaz, modelos de datos y repositorios permiten reutilizar código dentro del proyecto.

## Separación de responsabilidades

Cada parte del sistema cumple una función específica:

* Los controladores reciben solicitudes en el backend.
* Los servicios aplican la lógica de negocio.
* Los repositorios acceden a la base de datos.
* Los modelos representan la información.
* Los ViewModels manejan el estado de la interfaz.
* Los Composables muestran las pantallas al usuario.

---

# 📡 Endpoints principales del backend

Algunos endpoints principales disponibles en la API son:

## Autenticación

```text
POST /api/auth/registro
POST /api/auth/login
```

## Cuentas

```text
GET /api/accounts
POST /api/accounts
GET /api/accounts/{id}
PUT /api/accounts/{id}
DELETE /api/accounts/{id}
```

## Transacciones

```text
GET /api/transactions
POST /api/transactions
GET /api/transactions/{id}
PUT /api/transactions/{id}
DELETE /api/transactions/{id}
```

## Categorías

```text
GET /api/categories
POST /api/categories
GET /api/categories/{id}
PUT /api/categories/{id}
DELETE /api/categories/{id}
```

## Deudas

```text
GET /api/debts
POST /api/debts
GET /api/debts/{id}
PUT /api/debts/{id}
DELETE /api/debts/{id}
GET /api/debts/reminders
POST /api/debts/{id}/payments
GET /api/debts/{id}/payments
DELETE /api/debts/{id}/payments/{paymentId}
```

## Metas de ahorro

```text
GET /api/saving-goals
POST /api/saving-goals
GET /api/saving-goals/{id}
PUT /api/saving-goals/{id}
DELETE /api/saving-goals/{id}
GET /api/saving-goals/{id}/transactions
```

## Presupuestos

```text
GET /api/budgets
POST /api/budgets
GET /api/budgets/{id}/summary
PUT /api/budgets/{id}
DELETE /api/budgets/{id}
```

## Dashboard y reportes

```text
GET /api/dashboard/summary
GET /api/reports/monthly
GET /api/reports/by-category
GET /api/reports/by-account
GET /api/reports/cash-flow
```

---

# 📂 Estructura general del proyecto

El proyecto se organiza en dos partes principales:

```text
RACCCASH/
│
├── RaccoonCash/
│   └── Aplicación Android desarrollada con Kotlin y Jetpack Compose
│
└── raccoon-cash-api/
    └── Backend desarrollado con Java, Spring Boot y PostgreSQL
```

---

# 🚀 Estado del proyecto

✅ Proyecto funcional para presentación académica.

El sistema cuenta con frontend Android, backend API REST y conexión con base de datos.
Actualmente se encuentra en una etapa funcional con posibilidades de mejora y ampliación.

---

# 🔮 Mejoras futuras

Algunas mejoras que podrían implementarse en versiones posteriores son:

* Incorporar autenticación con JWT y Spring Security.
* Agregar cifrado robusto de contraseñas en el backend.
* Implementar almacenamiento local con Room o DataStore.
* Agregar notificaciones para recordatorios de deudas y metas.
* Añadir autenticación biométrica.
* Mejorar el modo offline.
* Ampliar los reportes financieros.
* Optimizar gráficos y estadísticas dentro del dashboard.

---

# 👨‍💻 Equipo de desarrollo

## RACCCASH Team

Proyecto académico desarrollado para la **Universidad Americana (UAM)**.

**Asignatura:** Programación Orientada a Objetos II
**Docente:** Jose Duran Garcia
**Año:** 2026

## Integrantes

* Diego Alejandro Palacios Parada
* Julio Cesar Mendez Sandigo
* KENNETH ALEXANDER ACUÑA ESTRADA

---

# 📄 Licencia

Este proyecto fue desarrollado únicamente con fines educativos y académicos.

---

<div align="center">

### 🦝 RACCCASH

**Organiza tu dinero, controla tus gastos y mejora tus finanzas.**

© 2026 RACCCASH Team

</div>
