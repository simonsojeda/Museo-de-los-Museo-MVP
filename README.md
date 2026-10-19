[Repositorio GitHub](https://github.com/simonsojeda/Museo-de-los-Museo-MVP) · [Tablero Trello](https://trello.com/b/WLWXs7FH/museoappmovil)

# Museo de los Museos · Android Kotlin (Duoc UC)

**Estado:** MVP académico en desarrollo. Se ha mostrado en emulador inicio de sesión, dashboard, catálogo y carrito; otras pantallas requieren pruebas de funcionamiento. No es una versión de producción.

## Contexto y objetivos

Caso Museo de los Museos: unificar cursos, artículos museológicos y el mapa de museos virtuales de Iberoamérica, con solicitudes de compra/inscripción y cotización **ficticia** de envíos. Los diseños de referencia son las 15 pantallas del PDF «061 SEC003D Pantallas Caso Museo de los Museos». La pauta es «Ev. Parcial 2 Estudiante Encargo DSY1105».

## Preparar el entorno

1. Instalar **Android Studio** con SDK Android API **35** y emulador Android **8.0+** (recomendado API 34/35 con Google APIs), JDK 17 compatible con Gradle.
2. Descomprimir la carpeta `MuseoDeLosMuseos` completa y abrirla desde **File > Open** en Android Studio (seleccionar la carpeta raíz que contiene `settings.gradle.kts`).
3. Con Internet, permitir sincronizar Gradle y descargar las dependencias declaradas. Gradle esperado: **8.7** y plugin Android **8.5.2**. El proyecto contiene `gradle/wrapper/gradle-wrapper.properties`, **pero no incluye `gradle-wrapper.jar`**, debido a que el entorno de generación no pudo descargarlo; si el IDE solicita wrapper JAR, usa la instalación de Gradle integrada/configurada por Android Studio para `Sync Project with Gradle Files`, o genera el wrapper desde un Gradle 8.7 instalado mediante `gradle wrapper --gradle-version 8.7`.
4. Crear un emulador o conectar un Android físico con depuración USB. Ejecutar **Run > app**.
5. La app usa datos de prueba locales al primer inicio; no exige levantar ningún servidor para sus funciones principales.
6. Para validar reglas independientes del teléfono: **Run Tests** desde la carpeta `app/src/test`.

**Si Gradle Sync presenta un error:** comprueba Internet, proxies, SDK 35, JDK 17 y que estás en la **carpeta raíz**; no abras exclusivamente `app/src` o los archivos `.kt` como proyecto. No afirmamos que el proyecto compile sin una comprobación en Android Studio.

## Cuentas ficticias

| Rol | Correo de laboratorio | Contraseña de laboratorio |
| --- | --- | --- |
| Cliente | `cliente@demo.cl` | `museo123` |
| Investigador | `investigador@demo.cl` | `museo123` |
| Administrador | `admin@demo.cl` | `museo123` |

Para registrarte como cliente usa exclusivamente identidad y correo inventados. Las credenciales se generan localmente para pruebas. **No reutilices contraseñas reales.** La autenticación no es apta para producción (hash local de laboratorio).

## Las 15 pantallas

| N° | Pantalla | Estado en el código |
| --- | --- | --- |
| 1 | Bienvenida | Interfaz, botones y animación |
| 2 | Registro | Campos con mensajes de validación; alta Room |
| 3 | Login de roles | Perfiles de prueba y separación por rol |
| 4 | Dashboard | Resumen adaptado al rol; navegación inferior |
| 5 | Mapa de museos | OpenStreetMap/osmdroid con marcadores ficticios y filtros |
| 6 | Detalle de museo | Ficha, favoritos y edición por investigador/admin |
| 7 | Escáner QR | Cámara mediante ZXing; entrada manual alternativa |
| 8 | Resultado QR | Validación local, éxito/error, animación y navegación |
| 9 | Catálogo | Búsqueda, categorías, alta admin y sincronización Retrofit manual |
| 10 | Detalle/solicitud | Precio, cantidad, carrito o inscripción a curso |
| 11 | Favoritos | Colección Room por usuario |
| 12 | Carrito/envío | Cantidades, total y cotizador ficticio |
| 13 | Historial/solicitudes | Seguimiento por cliente; cambio de estado admin |
| 14 | Notificaciones | Avisos Room y notificación local (NO push remoto) |
| 15 | Perfil | Nombre editable, preferencias Room, sesión e historial QR |

**Importante:** se trata de una implementación académica amplia del flujo; una pantalla definida en el código no constituye evidencia de ejecución hasta comprobarla en el emulador. Visitas virtuales, pagos y couriers externos reales **no están implementados**; sus enlaces y datos son ejemplos. Idioma alternativo, aplicación efectiva del modo de movimiento reducido, edición posterior del avatar y notificaciones push remotas quedan pendientes para una EFT más completa.

## Arquitectura y tecnologías

- `ui/MuseoApp.kt`: componentes Jetpack Compose, formularios y navegación por estado.
- `ui/Theme.kt`: colores y tema coherente con prototipos.
- `domain/Rules.kt`: validación desacoplada y reglas simuladas de envío; es testeable sin Android.
- `data/Models.kt`: Room (SQLite), modelos, DAO, fixtures ficticios y aislamiento por `userId`.
- `data/MuseoViewModel.kt`: gestión de estado y flujos, solicitudes, favoritos, login de prueba y comunicación con Room.
- `data/Api.kt`: Retrofit REST; GET datos públicos ficticios.
- `MainActivity.kt`: notificaciones locales, ubicación del equipo y configuración del mapa.
- `mock_api/server.py`: servidor REST académico opcional sin dependencias externas.
- `trello/`: tarjetas/planificación editable y tablero Kanban local (NO es una cuenta Trello).

**Estructura:** interfaz Compose → ViewModel → Room/API; lógica independiente en `domain`. Se utiliza navegación básica controlada por estado Compose para el MVP, no el grafo de Navigation Compose (librería disponible para una futura migración).

## REST opcional y caché

En consola desde la raíz ejecuta `python mock_api/server.py`. En el emulador, la URL **`http://10.0.2.2:8080/`** apunta a tu PC. Inicia sesión `admin@demo.cl`, abre Catálogo y pulsa **REST**. El ViewModel consulta `GET /api/museums` y `GET /api/catalog` con Retrofit y actualiza la base Room. Si el servidor no responde, los datos locales siguen utilizables.

Las reglas de precio ficticio están en `ShippingCalculator`: Metropolitana $3.500 + $1.200/kg; Regiones $6.500 + $1.200/kg; América $18.000 + $4.400/kg; Europa $28.000 + $4.400/kg; exprés = tarifa × 1,6 (redondeada hacia arriba). Estas cifras **fueron elegidas como parámetros de demostración**, ya que los PDF mencionan reglas simuladas pero no adjuntan los valores originales de la matriz algorítmica.

## Pruebas manuales recomendadas

1. Cuenta cliente: iniciar sesión, abrir mapa, guardar favorito, cerrar sesión, volver e identificar si persiste.
2. Cambiar a otra cuenta: favoritos, solicitudes y carrito deben mantenerse separados.
3. Código `MUSEO:1`: abrir QR manual y navegar a ficha; `DESCONOCIDO`: mostrar error.
4. Cámara: conceder permiso y leer un QR cuyo texto sea `MUSEO:1` (generarlo externamente). Comprobar respuesta si el permiso se deniega.
5. Mapa: abrir con Wi-Fi, pulsar marcador, filtrar por país, solicitar ubicación; comprobar manejo de GPS apagado/permiso denegado.
6. Catálogo cliente: agregar productos, probar stock, elegir destino/exprés, verificar subtotal y total, registrar solicitud.
7. Administrador: cambiar estado en Historial y crear aviso; cambiar a cliente y verificar aviso correspondiente.
8. Investigador: agregar ficha con coordenadas válidas y rechazar latitudes fuera de rango.
9. Formularios: intentar registro con nombre menor a 3 caracteres, correo inválido o clave menor a 8 caracteres.
10. Opcional API: arrancar servidor, pulsar REST y confirmar sincronización local.
11. Ejecutar `RulesTest` y verificar cálculo y errores.
12. Verificar accesibilidad real (TalkBack, tamaños grandes, pantallas pequeñas) antes de entregar.

## Trello / GitHub y entrega

Planificación real: **[Tablero Trello Museo_appMovil](https://trello.com/b/WLWXs7FH/museoappmovil)**. Los **36 tickets propuestos** se encuentran en `trello/trello_cards.csv`, `trello/trello_board.md` y `trello/trello_board.json`, divididos en **5 semanas** y **2 responsables sugeridos**. `trello/kanban_local.html` sirve para mover tarjetas localmente, pero **no crea ni sincroniza automáticamente un tablero Trello**. Para generar un tablero real, conecta Trello en ChatGPT o créalo manualmente en Trello y copia tarjetas por lista. Trello no ofrece importación CSV estándar universal; para importar CSV a menudo se requiere una herramienta externa.

**Rúbrica de la Evaluación Parcial 2:** diseño 15%, formularios 15%, lógica 10%, animaciones 10%, modularidad/persistencia 15%, colaboración GitHub/Trello 20%, dos recursos nativos 15%.

Antes de la fecha de entrega:
- Subir código y README a **GitHub público**. Agregar nombres de los **dos estudiantes** y su equipo (NO se inventaron).
- Asegurar commits distribuidos entre ambos autores, issues y tarjetas Trello con evidencia real.
- Enviar código fuente a **AVA grupal** y activar **AVA individual** como indica la pauta.
- **No realizar commits posteriores a la fecha de entrega y anteriores a la evaluación:** la pauta indica sanción automática.

## Restricción legal y seguridad

El caso exige datos sintéticos o agregados; no deben recolectarse datos reales. Los nombres museológicos, coordenadas aproximadas, enlaces, artículos, costos, perfiles y transacciones del MVP son de **demostración**. El backend de ejemplo es de consulta pública, no es una plataforma productiva y no dispone de controles empresariales.

**Autores:** _Completar nombres del equipo y sección antes de entregar._
