import csv,json,pathlib,html
p=pathlib.Path('/mnt/data/MuseoDeLosMuseos/trello')
# sprint, owner, task, indicator, criteria
rows=[
('Semana 1 · Fundamentos','A','Crear repositorio GitHub público y README inicial','IE 2.3.2','Registrar commits de ambos integrantes; añadir descripción, capturas y pasos de ejecución'),
('Semana 1 · Fundamentos','B','Crear tablero Trello y asignar tareas','IE 2.3.2','Crear listas, etiquetar rúbrica, asignar integrantes, capturar evidencia de actividad'),
('Semana 1 · Fundamentos','A','Configurar proyecto Android Studio con Kotlin y Compose','IE 2.3.1','Gradle sync; instalación en emulador; pantalla inicial ejecuta'),
('Semana 1 · Fundamentos','B','Definir modelos Room y SQLite por perfil','IE 2.3.1','Tablas users, museums, products, favorites, cart, requests, scans y notices'),
('Semana 1 · Fundamentos','A','Implementar tema visual navy/dorado y componentes','IE 2.1.1','Paleta, jerarquía, fuentes, tarjetas, navegación y accesibilidad base'),
('Semana 1 · Fundamentos','B','Definir datos sintéticos y reglas de privacidad','IE 2.3.1','No almacenar identidades reales ni contraseñas institucionales; coordenadas sintéticas'),
('Semana 1 · Fundamentos','A','Pantalla 1 · Bienvenida y animación','IE 2.2.2','Logo, propósito, comenzar, transiciones visibles'),
('Semana 2 · Usuarios y navegación','A','Pantalla 2 · Registro cliente validado','IE 2.1.2','Nombre, correo, clave, avatar, mensajes por campo; usuario Room'),
('Semana 2 · Usuarios y navegación','B','Pantalla 3 · Login y roles demo','IE 2.2.1','Cliente, investigador y administrador; impedir acceso a rol ajeno'),
('Semana 2 · Usuarios y navegación','A','Pantalla 4 · Dashboard según perfil','IE 2.1.1','Cliente: favoritos; admin: solicitudes; investigador: museos'),
('Semana 2 · Usuarios y navegación','B','Navegación inferior y control de estado','IE 2.2.1','Inicio, mapa, catálogo, notificaciones y perfil sin inconsistencias'),
('Semana 2 · Usuarios y navegación','B','Aplicar MVVM para validaciones desacopladas','IE 2.2.1','Lógica en dominio/viewmodel, errores reflejados en interfaz'),
('Semana 2 · Usuarios y navegación','A','Pantalla 15 · Perfil y configuración','IE 2.1.2','Datos del perfil, preferencias ficticias, cambio/cierre de sesión'),
('Semana 3 · Museos y QR','A','Pantalla 5 · Mapa con marcadores y filtros','IE 2.4.1','Mapa interactivo, países/proyectos y ficha de marcador'),
('Semana 3 · Museos y QR','B','Pantalla 6 · Detalle del museo','IE 2.1.1','Ubicación, proyecto, descripción, ficha, favoritos y edición por rol'),
('Semana 3 · Museos y QR','B','Formularios investigador para museos','IE 2.1.2','Alta/edición de ficha, validación de coordenadas'),
('Semana 3 · Museos y QR','A','Pantalla 7 · Cámara QR con permiso seguro','IE 2.4.1','Escanear MUSEO:1/PRODUCTO:1; alternativa manual'),
('Semana 3 · Museos y QR','A','Pantalla 8 · Resultado QR y error','IE 2.2.2','Éxito, error, abrir ficha; historial local sin duplicados'),
('Semana 3 · Museos y QR','B','Recurso nativo 2 · Ubicación del dispositivo','IE 2.4.1','Solicitar permiso en ejecución y presentar ubicación aproximada'),
('Semana 3 · Museos y QR','B','Pantalla 11 · Favoritos independientes por usuario','IE 2.3.1','Agregar/quitar, Room; verificar aislamiento entre dos perfiles'),
('Semana 4 · Comercio','A','Pantalla 9 · Catálogo con filtros y búsqueda','IE 2.1.1','Cursos, libros, materiales y merchandising; datos ficticios'),
('Semana 4 · Comercio','B','Formularios administrador de catálogo','IE 2.1.2','Agregar, editar, desactivar artículos; validación numérica'),
('Semana 4 · Comercio','A','Pantalla 10 · Detalle producto y cursos','IE 2.2.1','Cantidad, stock, carrito o solicitud de curso'),
('Semana 4 · Comercio','B','Pantalla 12 · Carrito Room','IE 2.3.1','Cantidades por cliente, subtotal y limpieza al confirmar'),
('Semana 4 · Comercio','A','Cotizador ficticio nacional e internacional','IE 2.2.1','Calcular tarifa y total por peso/país/modalidad; pruebas unitarias'),
('Semana 4 · Comercio','B','Pantalla 13 · Historial de solicitudes','IE 2.3.1','Cliente ve sus registros; admin actualiza estado'),
('Semana 4 · Comercio','A','Pantalla 14 · Notificaciones y avisos locales','IE 2.4.1','Persistencia Room y permiso Android 13 para aviso local'),
('Semana 4 · Comercio','B','Crear API REST ficticia y consumir Retrofit','IE 2.3.1','GET /api/museums y GET /api/catalog; caché Room'),
('Semana 5 · QA y entrega','A','Revisar las 15 pantallas con emulador','IE 2.1.1','Navegación y scroll comprobados; capturas por pantalla'),
('Semana 5 · QA y entrega','B','Ejecutar pruebas de validadores y cotización','IE 2.2.1','Casos válidos/invalidos; registrar resultados'),
('Semana 5 · QA y entrega','A','Probar cámara, QR, GPS y permisos','IE 2.4.1','Permisos concedidos/denegados; emulador y equipo físico'),
('Semana 5 · QA y entrega','B','Probar persistencia y segregación entre cuentas','IE 2.3.1','Cerrar sesión, reiniciar app y conservar favoritos / carrito'),
('Semana 5 · QA y entrega','A','Crear README final y documentación','IE 2.3.2','Nombres, instrucciones, casos de prueba, capturas, referencias'),
('Semana 5 · QA y entrega','B','Revisar issues, commits y tablero Trello','IE 2.3.2','Actividad real de ambos integrantes con tarjetas cerradas y evidencia'),
('Semana 5 · QA y entrega','A+B','Entregar repositorio público y código en AVA grupal','Entrega','Verificar idéntico código; no modificar GitHub tras fecha de entrega'),
('Semana 5 · QA y entrega','A+B','Completar AVA individual y preparar defensa','Entrega','Indicar nombre y equipo; preparar demostración de 2 recursos nativos'),
]
records=[]
for i,(sprint,owner,title,tag,accept) in enumerate(rows,1):
 records.append(dict(id=i,list=sprint,card=f'{i:02d}. {title}',owner=owner,label=tag,
       description=f'Objetivo: {title}.\nCriterio de aceptación: {accept}.\nResponsable sugerido: integrante {owner}.\nEvidencia: commits + captura/prueba + comentario de Trello.',checklist=accept))
with open(p/'trello_cards.csv','w',encoding='utf-8-sig',newline='') as f:
 w=csv.writer(f);w.writerow(['Lista','Tarjeta','Descripción','Etiqueta','Responsable sugerido','Criterio de aceptación'])
 for r in records:w.writerow([r['list'],r['card'],r['description'],r['label'],r['owner'],r['checklist']])
(p/'trello_board.json').write_text(json.dumps({'board':'Museo de los Museos | DSY1105 | Evaluación Parcial 2','lists':list(dict.fromkeys(x['list'] for x in records)),'cards':records},ensure_ascii=False,indent=2),encoding='utf-8')
md=['# Tablero de trabajo · Museo de los Museos','', '**36 tareas distribuidas en 5 semanas; responsables sugeridos A y B.**','', 'Este archivo permite copiar varias tarjetas de una lista en Trello. Copia solo las líneas que comienzan por numeración al crear tarjetas. Luego pega las descripciones y asigna responsables.', '']
for s in dict.fromkeys(x['list'] for x in records):
 md+=['## '+s, '']
 md += [r['card']+'  ·  '+r['owner']+'  ·  '+r['label'] for r in records if r['list']==s]
 md.append('')
md+=['## Listas adicionales recomendadas','','- En curso','- En revisión','- Terminado','', 'Las tarjetas solo deben moverse cuando exista evidencia. Un tablero de planificación no sustituye actividad real en Trello ni commits auténticos.']
(p/'trello_board.md').write_text('\n'.join(md),encoding='utf-8')
# Offline interactive Kanban; NOT a Trello board online.
raw=json.dumps(records,ensure_ascii=False).replace('</','<\\/')
style='''body{font-family:Arial,sans-serif;background:#eef2f5;margin:0;color:#19384b}header{background:#073349;color:white;padding:20px 26px}header h1{margin:0;font-size:23px}header small{opacity:.8}.controls{padding:16px 24px;display:flex;gap:12px}.controls button{padding:10px 16px;background:#b99862;border:0;border-radius:8px;color:#152a32;cursor:pointer;font-weight:bold}.board{display:flex;overflow-x:auto;align-items:start;gap:16px;padding:12px 24px 32px}.lane{background:#dee6eb;flex:0 0 300px;border-radius:12px;padding:12px;min-height:350px}.lane h2{font-size:16px;margin:6px 0 12px}.card{background:white;border-radius:10px;padding:12px;margin:8px 0;box-shadow:0 2px 4px #71849333;cursor:grab}.card b{display:block;font-size:14px;margin-bottom:7px}.card span{font-size:11px;color:#526879}.tag{background:#e6cda3;border-radius:8px;padding:3px 7px}.meta{margin:0 0 4px}.card p{font-size:12px;line-height:1.4}footer{padding:20px;font-size:12px}'''
htmlfile='''<!doctype html><html lang="es"><meta charset="utf-8"><title>Museo de los Museos · Kanban</title><style>'''+style+'''</style><header><h1>Museo de los Museos — tablero de planificación</h1><small>Vista offline editable. NO equivale a un tablero Trello publicado.</small></header><div class="controls"><button onclick="exportData()">Exportar avance JSON</button><button onclick="resetBoard()">Restablecer tarjetas</button></div><div class="board" id="board"></div><footer>Arrastra tarjetas entre listas. Los cambios quedan guardados en este navegador mediante localStorage.</footer><script>const initial='''+raw+''';let cards=JSON.parse(localStorage.getItem('museumBoardCards')||'null')||initial.map(x=>({...x}));const lanes=[...new Set(initial.map(x=>x.list)),'En curso','En revisión','Terminado'];function save(){localStorage.setItem('museumBoardCards',JSON.stringify(cards));}function render(){let root=document.getElementById('board');root.innerHTML='';lanes.forEach(name=>{let el=document.createElement('section');el.className='lane';el.innerHTML='<h2>'+name+' ('+cards.filter(x=>x.list===name).length+')</h2>';el.ondragover=e=>e.preventDefault();el.ondrop=e=>{e.preventDefault();let id=Number(e.dataTransfer.getData('text/plain'));let card=cards.find(x=>x.id===id);if(card){card.list=name;save();render();}};cards.filter(x=>x.list===name).forEach(c=>{let card=document.createElement('article');card.className='card';card.draggable=true;card.ondragstart=e=>e.dataTransfer.setData('text/plain',String(c.id));let b=document.createElement('b');b.textContent=c.card;card.appendChild(b);let span=document.createElement('span');span.textContent='Responsable '+c.owner+' · '+c.label;card.appendChild(span);let p=document.createElement('p');p.textContent=c.checklist;card.appendChild(p);el.appendChild(card)});root.appendChild(el)});}function exportData(){let blob=new Blob([JSON.stringify({cards},null,2)],{type:'application/json'});let a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='avance_kanban.json';a.click();URL.revokeObjectURL(a.href);}function resetBoard(){if(confirm('¿Restablecer avance local?')){localStorage.removeItem('museumBoardCards');cards=initial.map(x=>({...x}));render();}}render();</script></html>'''
(p/'kanban_local.html').write_text(htmlfile,encoding='utf-8')
print(len(records),'cards created')
