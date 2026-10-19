# Matriz de trazabilidad · Caso / rúbrica / evidencias

| Documento original | Requisito | Dónde se evidencia | Prueba pendiente |
|---|---|---|---|
| Caso, pág. 2 | Catálogos unificados y solicitudes | Catálogo, detalle, carrito, Room | Instalar en emulador |
| Caso, pág. 2 | Mapa georreferenciado Iberoamérica | Mapa osmdroid + ficha | Validar conexión de tiles |
| Caso, pág. 2 | Envíos simulados | `domain/Rules.kt` | Revisar fórmula con docente |
| Caso, pág. 2 | Persistencia SQLite | `data/Models.kt` Room | Reiniciar app y comprobar |
| Caso, pág. 2 | Cámara QR + avisos simulados | ZXing + notificación local | Probar recursos en dispositivo |
| Caso, pág. 2 | REST/Retrofit | `data/Api.kt`, `mock_api/server.py` | Probar GET con servidor local |
| Pantallas, págs. 2–5 | 15 vistas descritas | `ui/MuseoApp.kt` | Comparar diseños en emulador |
| Pauta, pág. 3 | Formulario validado desacoplado | `domain/Validators`, pantallas | Escenarios inválidos |
| Pauta, págs. 4–6 | Animaciones coherentes | Pantallas Welcome y QR | Revisar UX |
| Pauta, págs. 4–6 | GitHub + Trello | `trello/` + trabajo posterior | Crear tablero vivo y commits reales |
| Pauta, págs. 4–6 | 2 recursos nativos | Cámara QR y ubicación | Permisos + captura evidencia |

## Observaciones que NO deben presentarse como implementadas

- No hay pasarela de pago, despacho real ni GPS en tiempo real a una base central.
- El enlace de visita virtual es ilustrativo y no apunta a una visita institucional verificada.
- Los datos sintéticos se generaron para el prototipo, no se recuperaron de fuentes oficiales.
- El proyecto Android aún necesita compilación en Android Studio; no hay APK validado.
- El Trello REAL requiere vincular la cuenta y crear el tablero allí: archivos de plantillas no son equivalente a un tablero en línea.
- `USE_REMOTE_API` está predefinido en false; la sincronización REST se hace manualmente con el botón del administrador.
