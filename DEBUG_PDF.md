# 🔍 DEBUG PDF - PASO A PASO

## ✅ CAMBIOS REALIZADOS

Agregué logging detallado para ver exactamente dónde falla:

### TramiteController.java
- Logs al inicio del endpoint
- Logs de autenticación
- Logs de permisos
- Logs de generación de PDF
- Logs de error con causa detallada

### TramiteService.java
- Try-catch adicional para capturar errores
- Logs de errores con stack trace completo

---

## 🚀 PASOS PARA DEBUGGEAR

### 1. Reiniciar Backend
```bash
cd backend
taskkill /F /IM java.exe
rmdir /s /q target
mvnw.cmd clean spring-boot:run
```

### 2. Esperar a que inicie
Busca en la terminal:
```
Started BackendApplication in X.XXX seconds
```

### 3. Intentar descargar PDF
1. Ve a http://localhost:4200
2. Login como CLIENTE
3. Click en "Mis Trámites"
4. Click en cualquier trámite
5. Click en "Descargar PDF"

### 4. Ver los logs del backend
En la terminal del backend, deberías ver:

```
=== INICIO DESCARGA PDF ===
ID del trámite: 662e1c52141c0bd41bffa861
Usuario autenticado: cliente@test.com
Trámite encontrado: TRM-2026-0001
Estado del trámite: COMPLETADO
Generando PDF...
```

**Si falla, verás:**
```
=== ERROR AL GENERAR PDF ===
Mensaje: [mensaje de error]
Causa: [causa específica]
[stack trace completo]
```

---

## 📋 COPIA Y PEGA AQUÍ

Una vez que intentes descargar el PDF, copia TODO el output de la terminal del backend desde:
```
=== INICIO DESCARGA PDF ===
```

Hasta el final del error.

Eso me dirá exactamente qué está fallando.

---

## 🎯 POSIBLES CAUSAS

1. **Problema con fechas null**
   - El trámite no tiene `creadoEn` o `finalizadoEn`
   - Solución: Manejar null en el formato de fecha

2. **Problema con historial vacío**
   - El trámite no tiene historial
   - Solución: Ya está manejado, pero verificar

3. **Problema con datos del formulario**
   - El trámite tiene datos null en el formulario
   - Solución: Verificar el método nullSafe

4. **Problema con la librería OpenPDF**
   - Error al crear el documento
   - Solución: Ver el stack trace completo

---

## 🔧 SIGUIENTE PASO

Después de reiniciar el backend y ver los logs, avísame qué error específico aparece.
