# Rack - Pro

Aplicación Android para el **levantamiento digital de racks en Data Centers**, desarrollada para el caso **CMC360 SpA** (Proyecto académico DSY1105).

Un **Inspector** registra levantamientos de racks en terreno (foto de evidencia, componentes del catálogo, fecha) y un **Supervisor** los revisa, valida cada componente y aprueba el levantamiento. Todo funciona en modo local, sin conexión a internet.

## Pantallas

| Pantalla | Descripción |
|---|---|
| Login | Ingreso con correo, contraseña y rol (Inspector / Supervisor) |
| Registro | Creación de cuenta con validación de campos |
| Levantamientos | Listado con filtros Todos / Pendiente / Aprobado y botón “Nueva inspección” |
| Detalle de rack | Foto de evidencia (cámara o galería), ID, fecha, componentes y trazabilidad |
| Mi perfil | Datos del usuario y estadísticas de sus levantamientos |
| Editar perfil | Formulario con foto de perfil tomada con la cámara |
| Acerca de | Información del proyecto |

## Requisitos cumplidos

- **Formularios:** Login, Registro, Nueva inspección, Detalle de rack y Editar perfil.
- **Validación de campos:** `utilidades/Validaciones.kt` (correo, contraseña, confirmación, teléfono, ID de rack `A-01`, fecha `dd/mm/aaaa`). Los mensajes de error aparecen bajo cada campo.
- **Imagen:** logo vectorial `res/drawable/ic_rack.xml` y fotos tomadas con la cámara.
- **Animaciones:** entrada del logo en Login, colores animados en botones de rol, filtros y estados, aparición de errores, contadores del perfil, logo pulsante en “Acerca de” y reordenamiento animado de la lista.
- **Modales:** Nueva inspección, Agregar componente, Aprobar levantamiento y Cerrar sesión (`AlertDialog`).
- **Persistencia con Room:** usuarios (incluida la sesión activa), levantamientos y componentes (`data/`).
- **Navegación:** Navigation Compose (`navegacion/NavegacionApp.kt`) con barra inferior.
- **Recurso nativo:** cámara (con permiso en tiempo de ejecución y `FileProvider`) y galería/archivos (`camara/CamaraFotos.kt`).

## Roles

| Acción | Inspector | Supervisor |
|---|---|---|
| Crear inspección | ✅ | ❌ |
| Editar ID, fecha y foto | ✅ | ✅ |
| Agregar componentes | ✅ | ✅ |
| Cambiar estado de componentes (tocar la fila) | ❌ | ✅ |
| Aprobar levantamiento | ❌ | ✅ |

Al aprobar un levantamiento queda bloqueado para edición.

## Estructura del proyecto

```
app/src/main/java/com/example/proyectov2/
├── MainActivity.kt
├── RackProViewModel.kt          Lógica de la app y acceso a Room
├── camara/CamaraFotos.kt        Cámara y galería
├── data/                        Entidades, DAOs y base de datos Room
├── navegacion/                  Rutas y NavHost
├── ui/componentes/              Botones, campos, encabezado, etiquetas
├── ui/pantallas/                Una pantalla por archivo
├── ui/theme/                    Paleta de colores y tema
└── utilidades/                  Validaciones y fechas
```

## Paleta de colores

| Color | Uso |
|---|---|
| `#0B3C49` Azul petróleo | Encabezados y filtros activos |
| `#0E6B78` Verde azulado | Botones principales |
| `#F3F5F7` Gris claro | Fondo |
| `#DDF3E4` / `#1E7A3C` | Aprobado / Validado |
| `#FCEBC8` / `#8A5A00` | Pendiente |
| `#FADBD8` / `#B42318` | Observado |

## Cómo ejecutar

1. Abrir el proyecto en Android Studio (versión reciente, compatible con AGP 9).
2. Sincronizar Gradle.
3. Ejecutar en un emulador o dispositivo con Android 7.0 (API 24) o superior.

La primera vez se cargan 4 levantamientos de ejemplo. Para probar, crea una cuenta de Inspector y otra de Supervisor desde “Regístrate”.

## Pruebas

```bash
./gradlew test
```

Ejecuta las pruebas unitarias de `ValidacionesTest`.

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · Room · KSP · Navigation Compose · ViewModel · Activity Result API
