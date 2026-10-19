# REST demo (opcional)

Arranca desde la raíz del proyecto:

```bash
python mock_api/server.py
```

Verifica `http://localhost:8080/api/museums` y `http://localhost:8080/api/catalog`.

Para probar Retrofit en Android ejecuta en un **emulador Android**, cuya dirección del equipo anfitrión es `10.0.2.2`. La sincronización se activa manualmente con el botón **REST**; no es necesario cambiar `USE_REMOTE_API`. En el catálogo del rol administrador pulsa `REST`. En un dispositivo físico cambia `BACKEND_URL` por la IP local de tu PC. No utilices este backend como servicio productivo; no incorpora autenticación real.
