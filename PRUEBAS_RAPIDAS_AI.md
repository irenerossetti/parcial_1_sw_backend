# 🧪 Pruebas Rápidas - Funcionalidades AI

**Tiempo total:** 15 minutos  
**Objetivo:** Verificar que todas las funcionalidades AI funcionan correctamente

---

## 🚀 ANTES DE EMPEZAR

### 1. Iniciar Backend (1 min)
```bash
cd backend
mvnw spring-boot:run
```
Espera a ver: "Started BackendApplication"

### 2. Iniciar Frontend (1 min)
```bash
cd diagram
npm start
```
Espera a ver: "Compiled successfully"

### 3. Abrir Navegador
- URL: http://localhost:4200
- Navegador: Chrome o Edge (recomendado)
- Login: admin / admin123

---

## ✅ PRUEBA 1: Chatbot AI (2 min)

### Pasos:
1. Verás botón azul flotante en esquina inferior derecha
2. Si es primera vez, aparecerá tooltip "¡Hey! Pregunta aquí"
3. Click en el botón azul
4. Escribe: "¿Cuántas políticas tengo?"
5. Presiona Enter

### ✅ Resultado esperado:
- El chatbot responde con información del sistema
- Aparecen 4 sugerencias de preguntas
- El historial se guarda (recarga la página y verás el mensaje)

### ❌ Si falla:
- Abre consola (F12) y busca errores
- Verifica API key en `diagram/src/environments/environment.ts`
- Verifica conexión a internet

---

## ✅ PRUEBA 2: Búsqueda de Plantillas (2 min)

### Pasos:
1. Click en "Plantillas" en el menú
2. Verás 10 plantillas
3. En el campo de búsqueda, escribe: "crédito"
4. Click en "Buscar con AI" (o presiona Enter)
5. Debe aparecer "Aprobación de Crédito Bancario"

### ✅ Resultado esperado:
- La búsqueda encuentra plantillas relevantes
- Puedes ver detalles de la plantilla
- Puedes usar la plantilla

### ❌ Si falla:
- La búsqueda normal (sin AI) debe funcionar
- Verifica API key
- Revisa consola por errores

---

## ✅ PRUEBA 3: Generador de Plantillas (3 min)

### Pasos:
1. Estando en "Plantillas", verás botón rosa flotante
2. Click en el botón rosa (icono de estrella)
3. En el textarea, escribe: "Proceso de aprobación de gastos con 3 niveles"
4. Click en "Generar Plantilla"
5. Espera 5-10 segundos

### ✅ Resultado esperado:
- Aparece plantilla generada con:
  - Nombre del proceso
  - Descripción
  - Categoría
  - Tags
  - Flujo con nodos
- Botón "Usar Esta Plantilla" disponible

### ❌ Si falla:
- Verifica API key
- Verifica conexión a internet
- Intenta con descripción más simple
- Revisa consola por errores

---

## ✅ PRUEBA 4: Asistente de Voz (3 min)

### Pasos:
1. Ve a "Políticas" → Crea o edita una
2. Click en "Diagrama"
3. Verás botón morado flotante (debajo del chatbot)
4. Click en el botón morado
5. Permite acceso al micrófono (si pregunta)
6. Di claramente: "Agregar nodo validación"
7. Espera respuesta

### ✅ Resultado esperado:
- El botón cambia a color rosa mientras escucha
- Aparece animación de ondas
- Se muestra el texto que dijiste
- El asistente responde hablando: "Nodo validación agregado correctamente"
- Se crea el nodo en el diagrama

### Comandos para probar:
```
"Agregar nodo validación"
"Agregar calle cliente"
"Guardar diagrama"
"Ayuda"
```

### ❌ Si falla:
- Verifica permisos de micrófono en el navegador
- Usa Chrome o Edge (mejor compatibilidad)
- Habla más claro y pausado
- Verifica que no haya ruido de fondo

---

## ✅ PRUEBA 5: Análisis de Cuellos de Botella (2 min)

### Pasos (desde consola del navegador):
1. Abre consola (F12)
2. Pega este código:

```javascript
// Obtener el servicio
const analyzer = angular.element(document.body).injector().get('BottleneckAnalyzerService');

// Analizar política ID 1
analyzer.analyzeWithAI(1).subscribe(analysis => {
  console.log('=== ANÁLISIS DE CUELLOS DE BOTELLA ===');
  console.log('Política:', analysis.politicaNombre);
  console.log('Eficiencia:', analysis.eficienciaGeneral + '%');
  console.log('Cuellos detectados:', analysis.cuellosDetectados.length);
  console.log('Recomendaciones:', analysis.recomendaciones);
  console.table(analysis.cuellosDetectados);
});
```

### ✅ Resultado esperado:
- Se muestra análisis en consola
- Aparecen cuellos de botella detectados
- Se muestran recomendaciones
- Tabla con detalles de cada cuello

### ❌ Si falla:
- Verifica que exista política con ID 1
- Verifica que haya trámites en el sistema
- Si no hay datos, el análisis estará vacío (normal)

---

## ✅ PRUEBA 6: Generación de Reportes (2 min)

### Pasos (desde consola del navegador):
1. Abre consola (F12)
2. Pega este código:

```javascript
// Obtener el servicio
const reportGen = angular.element(document.body).injector().get('ReportGeneratorService');

// Generar reporte con AI
reportGen.generateAIReport().subscribe(report => {
  console.log('=== REPORTE DEL SISTEMA ===');
  console.log('Título:', report.titulo);
  console.log('Fecha:', report.fecha);
  console.log('\nRESUMEN EJECUTIVO:');
  console.log(report.resumenEjecutivo);
  console.log('\nESTADÍSTICAS:');
  console.table(report.estadisticas);
  console.log('\nTENDENCIAS:');
  report.tendencias.forEach(t => console.log('- ' + t));
  console.log('\nRECOMENDACIONES:');
  report.recomendaciones.forEach(r => console.log('- ' + r));
});
```

### ✅ Resultado esperado:
- Se muestra reporte completo en consola
- Resumen ejecutivo generado con AI
- Estadísticas del sistema
- Tendencias identificadas
- Recomendaciones accionables

### ❌ Si falla:
- Verifica API key
- Si no hay datos, el reporte estará básico (normal)

---

## ✅ PRUEBA 7: Clasificación de Trámites (2 min)

### Pasos (desde consola del navegador):
1. Abre consola (F12)
2. Pega este código:

```javascript
// Obtener el servicio
const classifier = angular.element(document.body).injector().get('TramiteClassifierService');

// Clasificar trámite
const descripcion = "Necesito solicitar un préstamo personal urgente por emergencia médica";
const titulo = "Solicitud de Préstamo";

classifier.classifyTramite(descripcion, titulo).subscribe(result => {
  console.log('=== CLASIFICACIÓN DE TRÁMITE ===');
  console.log('Descripción:', descripcion);
  console.log('\nPOLÍTICA SUGERIDA:');
  console.log('- Nombre:', result.politicaSugerida.nombre);
  console.log('- Confianza:', result.politicaSugerida.confianza + '%');
  console.log('\nPRIORIDAD:', result.prioridad);
  console.log('\nRAZONAMIENTO:', result.razonamiento);
  console.log('\nTAGS:', result.tags.join(', '));
});
```

### ✅ Resultado esperado:
- Se muestra clasificación en consola
- Política sugerida con nivel de confianza
- Prioridad (ALTA/MEDIA/BAJA)
- Razonamiento explicativo
- Tags extraídos

### ❌ Si falla:
- Verifica API key
- Verifica que existan políticas en el sistema
- Si no hay políticas, usará fallback (normal)

---

## 📊 RESUMEN DE PRUEBAS

Marca cada prueba completada:

- [ ] ✅ Prueba 1: Chatbot AI
- [ ] ✅ Prueba 2: Búsqueda de Plantillas
- [ ] ✅ Prueba 3: Generador de Plantillas
- [ ] ✅ Prueba 4: Asistente de Voz
- [ ] ✅ Prueba 5: Análisis de Cuellos de Botella
- [ ] ✅ Prueba 6: Generación de Reportes
- [ ] ✅ Prueba 7: Clasificación de Trámites

---

## 🎯 PRUEBA RÁPIDA DE APP FLUTTER (5 min)

### Configuración:
1. Abre `flutter/lib/core/services/api_service.dart`
2. Cambia la URL:
```dart
static const String baseUrl = 'http://TU_IP:8080/api';
// Ejemplo: 'http://192.168.1.100:8080/api'
```

### Pasos:
```bash
cd flutter
flutter pub get
flutter run -d chrome
```

### Pruebas:
1. Registra un usuario
2. Inicia sesión
3. Ve la lista de trámites
4. Crea un nuevo trámite
5. Ve el detalle con timeline
6. Descarga el PDF

---

## ❓ TROUBLESHOOTING

### El chatbot no responde:
```
1. Verifica API key en environment.ts
2. Abre consola (F12) y busca errores
3. Verifica conexión a internet
4. Intenta recargar la página
```

### El asistente de voz no funciona:
```
1. Verifica permisos de micrófono
2. Usa Chrome o Edge
3. Habla más claro
4. Verifica que no haya ruido
```

### Los servicios no funcionan:
```
1. Verifica que el backend esté corriendo
2. Verifica API key de OpenRouter
3. Revisa consola por errores
4. Verifica conexión a internet
```

### Errores de compilación:
```
1. Ejecuta: npm install
2. Ejecuta: ng serve
3. Si persiste, borra node_modules y reinstala
```

---

## 📝 NOTAS IMPORTANTES

### API Key de OpenRouter:
- Ya está configurada en `environment.ts`
- Modelo: `openai/gpt-3.5-turbo`
- Costo: ~$0.002 por 1K tokens (muy económico)

### Navegadores Compatibles:
- ✅ Chrome (recomendado)
- ✅ Edge (recomendado)
- ⚠️ Firefox (funciona pero sin voz)
- ❌ Safari (no soporta Web Speech API)

### Permisos Necesarios:
- Micrófono (para asistente de voz)
- Internet (para OpenAI)

---

## 🎉 RESULTADO ESPERADO

Si todas las pruebas pasan:
- ✅ 13 funcionalidades AI funcionando
- ✅ Sistema listo para demo
- ✅ Sin errores críticos
- ✅ Experiencia fluida

---

**Tiempo total de pruebas:** 15 minutos  
**Estado esperado:** TODO FUNCIONANDO ✅

**¡Listo para la demo! 🚀**
