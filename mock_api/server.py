#!/usr/bin/env python3
"""Servidor REST sintético para Android emulator: http://10.0.2.2:8080/.
No requiere dependencias ni acepta datos personales.
"""
import json
from http.server import HTTPServer, BaseHTTPRequestHandler

museums = [
  dict(id=1,name="Archivo de Arte Colonial (Demo)",country="Colombia",city="Bogotá",description="Colección académica ficticia",project="Patrimonio digital",lat=4.711,lon=-74.072,virtualUrl="https://example.org/demo",featured=True),
  dict(id=2,name="Galería Virtual Andina (Demo)",country="Chile",city="Santiago",description="Patrimonio ficticio y educación digital",project="Rutas iberoamericanas",lat=-33.4489,lon=-70.6693,virtualUrl="https://example.org/demo",featured=True),
  dict(id=3,name="Centro de Memoria Digital (Demo)",country="México",city="Ciudad de México",description="Recorrido de exposiciones académicas",project="Patrimonio digital",lat=19.4326,lon=-99.1332,virtualUrl="https://example.org/demo",featured=False),
  dict(id=4,name="Museo de Artes Iberoamericanas (Demo)",country="España",city="Madrid",description="Muestra interactiva de datos ficticios",project="Rutas iberoamericanas",lat=40.4168,lon=-3.7038,virtualUrl="https://example.org/demo",featured=False),
  dict(id=5,name="Museo Virtual de Ciencias (Demo)",country="Perú",city="Lima",description="Ciencia y conservación simuladas",project="Educación museológica",lat=-12.0464,lon=-77.0428,virtualUrl="https://example.org/demo",featured=False),
  dict(id=6,name="Museo de Nuevas Narrativas (Demo)",country="Argentina",city="Buenos Aires",description="Participación cultural",project="Educación museológica",lat=-34.6037,lon=-58.3816,virtualUrl="https://example.org/demo",featured=False),
]
catalog = [
 dict(id=1,title="Historia del arte iberoamericano",category="Libros",description="Libro de muestra académica",price=18500,stock=12,weightKg=0.8,imageKind="book",active=True),
 dict(id=2,title="Kit de conservación documental",category="Materiales",description="Kit académico simulado",price=24500,stock=8,weightKg=1.2,imageKind="kit",active=True),
 dict(id=3,title="Curso de curaduría virtual",category="Cursos",description="Curso ficticio",price=39000,stock=20,weightKg=0,imageKind="course",active=True),
 dict(id=4,title="Taller de museología digital",category="Cursos",description="Taller ficticio",price=29000,stock=12,weightKg=0,imageKind="course",active=True),
 dict(id=5,title="Bolso patrimonio cultural",category="Merchandising",description="Producto ficticio",price=9800,stock=19,weightKg=0.35,imageKind="merch",active=True),
 dict(id=6,title="Cuaderno del investigador",category="Publicaciones",description="Bitácora ficticia",price=6900,stock=14,weightKg=0.4,imageKind="book",active=True),
]
class Handler(BaseHTTPRequestHandler):
 def do_GET(self):
  data = museums if self.path == '/api/museums' else catalog if self.path == '/api/catalog' else None
  if data is None:
   self.send_error(404,'Recurso no disponible');return
  raw=json.dumps(data,ensure_ascii=False).encode('utf-8')
  self.send_response(200);self.send_header('Content-Type','application/json; charset=utf-8')
  self.send_header('Content-Length',str(len(raw)));self.end_headers();self.wfile.write(raw)
 if __name__=='__main__':pass
if __name__=='__main__':
 print('API ficticia activa en http://localhost:8080')
 HTTPServer(('0.0.0.0',8080),Handler).serve_forever()
