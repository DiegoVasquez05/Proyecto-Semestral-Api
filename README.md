# Rack - Pro

Aplicación Android para el **levantamiento digital de racks en Data Centers**, desarrollada para el caso **CMC360 SpA** (proyecto académico DSY1105).

Un **Inspector** registra levantamientos de racks en terreno (foto de evidencia, componentes del catálogo y fecha) y un **Supervisor** los revisa, valida cada componente y aprueba el levantamiento. Los datos se guardan en una **API REST propia** (Node.js + PostgreSQL) y la app mantiene una copia local en Room para mostrarlos sin esperar la red.

[![Android CI](https://github.com/DiegoVasquez05/Proyecto-Semestral-Api/actions/workflows/android.yml/badge.svg)](https://github.com/DiegoVasquez05/Proyecto-Semestral-Api/actions/workflows/android.yml)

## Descargar el APK

El APK **release firmado** está en la página de [Releases](https://github.com/DiegoVasquez05/Proyecto-Semestral-Api/releases/latest), en la sección **Assets**. Se instala en Android 7.0 (API 24) o superior.

Cuentas de prueba (se crean automáticamente en la API):

| Rol | Correo | Contraseña |
|---|---|---|
| Inspector | `inspector@cmc360.cl` | `rackpro123` |
| Supervisor | `supervisor@cmc360.cl` | `rackpro123` |

> La API está en el plan gratuito de Render, que la "duerme" tras 15 minutos sin uso. La primera petición puede tardar hasta un minuto mientras despierta.

## Requisitos de la evaluación

| Requisito | Dónde se cumple |
|---|---|
| Consumo de API externa (mínimo 7 endpoints, GET y POST) | API propia en [`api/`](api) con 13 endpoints; la app consume 12 en [`RackProApi.kt`](app/src/main/java/com/example/proyectov2/repository/remoto/RackProApi.kt) |
| Pruebas unitarias (mínimo 2) | 16 pruebas de la app en [`app/src/test`](app/src/test/java/com/example/proyectov2) y 12 pruebas de la API en [`api/test`](api/test) |
| APK firmado (modo release) | Firma configurada en [`app/build.gradle.kts`](app/build.gradle.kts) y generada por [`release.yml`](.github/workflows/release.yml) |
| Ícono personalizado | Ícono adaptativo en [`res/drawable/ic_launcher_foreground.xml`](app/src/main/res/drawable/ic_launcher_foreground.xml) |
| 2 recursos nativos | **Cámara y galería** ([`camara/CamaraFotos.kt`](app/src/main/java/com/example/proyectov2/camara/CamaraFotos.kt)) y **notificaciones** ([`notificaciones/Notificador.kt`](app/src/main/java/com/example/proyectov2/notificaciones/Notificador.kt)) |
| Arquitectura MVVM | Carpetas `model`, `repository` y `viewmodel`, más las pantallas en `ui` |

## Arquitectura

La app sigue el patrón **MVVM**. Cada capa tiene una sola responsabilidad y solo conoce a la capa de abajo:

```
Pantallas (Jetpack Compose)        ui/pantallas, ui/componentes
        │  observan el estado y llaman acciones
        ▼
ViewModels                         viewmodel/
        │  SesionViewModel · LevantamientosViewModel
        ▼
Repositorios                       repository/
        │  UsuarioRepositorio · LevantamientoRepositorio
        ├──────────────────────────────┐
        ▼                              ▼
API REST (Retrofit)                Room (caché local)
repository/remoto                  repository/local

Model (model/): entidades, DTO de la API, roles y estados que usan todas las capas
```

- **Pantallas:** solo dibujan la interfaz y validan los formularios. No acceden a datos directamente.
- **ViewModels:** guardan el estado de la pantalla (usuario actual, listado, mensajes, carga) y lanzan las operaciones en corrutinas.
- **Repositorios:** deciden de dónde vienen los datos. Cada cambio se envía primero a la API y, si responde bien, la respuesta se guarda en Room; las pantallas observan Room con `Flow` y se actualizan solas.
- **Remoto (`repository/remoto`):** interfaz Retrofit, cliente HTTP, conversión de DTO a entidades y manejo de errores con mensajes en español.
- **Local (`repository/local`):** DAO y base de datos Room. Las fotos de evidencia y de perfil solo se guardan en el teléfono.
- **Model (`model`):** las clases de datos: entidades de Room, DTO que viajan en JSON, roles y estados.

### Estructura del proyecto

```
app/src/main/java/com/example/proyectov2/
├── MainActivity.kt
├── RackProApplication.kt          Crea la base de datos, el cliente de la API y los repositorios
├── camara/CamaraFotos.kt          Recurso nativo 1: cámara y galería
├── notificaciones/Notificador.kt  Recurso nativo 2: notificaciones y permiso
├── model/                         Entidades (Usuario, Levantamiento, Componente), DTO, roles y estados
├── repository/                    UsuarioRepositorio y LevantamientoRepositorio
│   ├── local/                     DAO y base de datos Room
│   └── remoto/                    Retrofit: endpoints, cliente, mapeadores y errores
├── viewmodel/                     SesionViewModel y LevantamientosViewModel
├── navegacion/                    Rutas y NavHost
├── ui/
│   ├── pantallas/                 Una pantalla por archivo
│   ├── componentes/               Botones, campos, encabezado, etiquetas
│   └── theme/                     Paleta de colores y tema
└── utilidades/                    Validaciones y fechas

api/
├── src/
│   ├── servidor.js                Punto de entrada (conexión a PostgreSQL)
│   ├── app.js                     Rutas de Express
│   ├── baseDeDatos.js             Tablas y datos iniciales
│   ├── rutas/                     usuarios, levantamientos, componentes, catálogo
│   └── ...                        validaciones, errores, seguridad y mapeadores
└── test/api.test.js               Pruebas de los endpoints
```

## API REST

Backend propio hecho con **Node.js + Express + PostgreSQL**, desplegado en Render. URL base: `https://rack-pro-api.onrender.com/api/`.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/salud` | Comprueba que la API esté en línea |
| POST | `/usuarios/registro` | Crea una cuenta (la contraseña se guarda cifrada con scrypt) |
| POST | `/usuarios/login` | Inicia sesión validando correo, contraseña y rol |
| PUT | `/usuarios/{id}` | Actualiza nombre, correo, cargo y teléfono |
| GET | `/levantamientos` | Lista los levantamientos con sus componentes (filtro opcional `?estado=`) |
| GET | `/levantamientos/{id}` | Obtiene un levantamiento con sus componentes |
| POST | `/levantamientos` | Crea una inspección en estado Pendiente |
| PUT | `/levantamientos/{id}` | Edita el ID del rack y la fecha |
| PATCH | `/levantamientos/{id}/aprobacion` | El supervisor aprueba el levantamiento |
| DELETE | `/levantamientos/{id}` | Elimina un levantamiento pendiente |
| POST | `/levantamientos/{id}/componentes` | Agrega un componente del catálogo |
| PATCH | `/componentes/{id}` | Cambia el estado de un componente |
| GET | `/catalogo/componentes` | Catálogo de componentes disponibles |

Todas las respuestas de error tienen la forma `{ "mensaje": "..." }`. Un levantamiento aprobado queda bloqueado: editarlo, eliminarlo o cambiar sus componentes responde `409`.

Ejemplo de petición:

```http
POST /api/levantamientos
Content-Type: application/json

{
  "codigoRack": "A-03",
  "dataCenter": "DC Principal",
  "sala": "Sala 2",
  "fecha": "20/11/2026",
  "inspectorId": 1,
  "inspectorNombre": "Inspector Demo"
}
```

### Ejecutar la API en local

Requiere Node.js 20 o superior y PostgreSQL.

```bash
cd api
npm install
DATABASE_URL=postgres://usuario:clave@localhost:5432/rack_pro npm start
```

Las pruebas no necesitan PostgreSQL (usan una base en memoria):

```bash
cd api
npm test
```

### Desplegar en Render

1. Crear una cuenta en [render.com](https://render.com) e iniciar sesión con GitHub.
2. **New → Blueprint** y elegir este repositorio. Render lee [`render.yaml`](render.yaml) y crea la base PostgreSQL y el servicio web.
3. Cuando termine, la API queda en `https://rack-pro-api.onrender.com/api/`. Si Render asigna otra dirección, actualizar `rackpro.apiUrl` en [`gradle.properties`](gradle.properties).

## Recursos nativos

1. **Cámara y galería:** foto de evidencia del rack y foto de perfil, con permiso en tiempo de ejecución y `FileProvider`.
2. **Notificaciones:** avisan cuando se registra una inspección y cuando se aprueba un levantamiento. En Android 13 o superior la app pide el permiso `POST_NOTIFICATIONS`.

## Pantallas

| Pantalla | Descripción |
|---|---|
| Login | Ingreso con correo, contraseña y rol (Inspector / Supervisor) |
| Registro | Creación de cuenta con validación de campos |
| Levantamientos | Listado con filtros Todos / Pendiente / Aprobado, deslizar para actualizar y botón "Nueva inspección" |
| Detalle de rack | Foto de evidencia, ID, fecha, componentes, trazabilidad, aprobación y eliminación |
| Mi perfil | Datos del usuario y estadísticas de sus levantamientos |
| Editar perfil | Formulario con foto de perfil tomada con la cámara |
| Acerca de | Información del proyecto |

## Roles

| Acción | Inspector | Supervisor |
|---|---|---|
| Crear inspección | ✅ | ❌ |
| Editar ID, fecha y foto | ✅ | ✅ |
| Agregar componentes | ✅ | ✅ |
| Cambiar estado de componentes (tocar la fila) | ❌ | ✅ |
| Aprobar levantamiento | ❌ | ✅ |
| Eliminar levantamiento pendiente | ✅ | ❌ |

Al aprobar un levantamiento queda bloqueado para edición.

## Pruebas

Pruebas unitarias de la app (validaciones, estados, mapeadores y manejo de errores):

```bash
./gradlew testDebugUnitTest
```

Pruebas de la API (los 13 endpoints):

```bash
cd api && npm test
```

GitHub Actions ejecuta ambas en cada push ([`android.yml`](.github/workflows/android.yml)).

## APK firmado

El APK release se firma con una llave que **no** está en el repositorio. `app/build.gradle.kts` la lee de estas variables de entorno, que en GitHub Actions vienen de los *secrets* del repositorio (**Settings → Secrets and variables → Actions**):

| Secret | Contenido |
|---|---|
| `RACKPRO_KEYSTORE_BASE64` | Archivo `.jks` de la llave, codificado en base64 |
| `RACKPRO_KEYSTORE_PASSWORD` | Contraseña del archivo de la llave |
| `RACKPRO_KEY_ALIAS` | Alias de la llave |
| `RACKPRO_KEY_PASSWORD` | Contraseña del alias |

Para publicar una versión nueva se ejecuta el workflow [`release.yml`](.github/workflows/release.yml) desde **Actions → APK firmado (release) → Run workflow** indicando la versión, o se sube una etiqueta. El workflow compila, firma, verifica la firma y publica el APK en Releases:

```bash
git tag v2.0.0
```

```bash
git push origin v2.0.0
```

## Paleta de colores

| Color | Uso |
|---|---|
| `#0B3C49` Azul petróleo | Encabezados, filtros activos y fondo del ícono |
| `#0E6B78` Verde azulado | Botones principales |
| `#F3F5F7` Gris claro | Fondo |
| `#DDF3E4` / `#1E7A3C` | Aprobado / Validado |
| `#FCEBC8` / `#8A5A00` | Pendiente |
| `#FADBD8` / `#B42318` | Observado |

## Tecnologías

**App:** Kotlin · Jetpack Compose · Material 3 · MVVM · Retrofit · Gson · OkHttp · Room · KSP · Navigation Compose · Activity Result API · Notificaciones

**API:** Node.js · Express · PostgreSQL · node:test · Supertest · pg-mem · Render

**CI/CD:** GitHub Actions (pruebas, compilación y APK firmado)
