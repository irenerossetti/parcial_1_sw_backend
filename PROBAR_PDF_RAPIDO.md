# 🚀 PROBAR PDF PROFESIONAL - GUÍA RÁPIDA

**Tiempo:** 3 minutos  
**Estado:** ✅ LISTO PARA PROBAR

---

## 📋 PASOS RÁPIDOS

### 1. Iniciar el Backend (30 seg):
```bash
cd backend
mvnw spring-boot:run
```

Esperar mensaje: `Started BackendApplication in X seconds`

---

### 2. Crear un Trámite de Prueba (1 min):

**Opción A - Usando Postman/Thunder Client:**

```http
POST http://localhost:8080/api/tramites
Content-Type: application/json
Authorization: Bearer {tu_token_jwt}

{
  "politicaId": "1",
  "clienteNombre": "Juan Pérez",
  "clienteEmail": "juan@ejemplo.com",
  "descripcion": "Solicitud de vacaciones",
  "prioridad": "MEDIA"
}
```

**Opción B - Usando el Frontend:**
1. Abrir `http://localhost:4200`
2. Login
3. Ir a "Trámites"
4. Click "Nuevo Trámite"
5. Llenar formulario
6. Guardar

---

### 3. Completar el Trámite (30 seg):

**Opción A - Usando Postman:**

```http
PATCH http://localhost:8080/api/tramites/{tramiteId}
Content-Type: application/json
Authorization: Bearer {tu_token_jwt}

{
  "estado": "COMPLETADO",
  "finalizadoEn": "2026-04-28T22:30:00"
}
```

**Opción B - Usando MongoDB Compass:**
1. Conectar a MongoDB Atlas
2. Buscar la colección `tramites`
3. Encontrar tu trámite
4. Cambiar campo `estado` a `"COMPLETADO"`
5. Agregar campo `finalizadoEn` con fecha actual

---

### 4. Descargar el PDF (30 seg):

**Opción A - Usando el Navegador:**
```
http://localhost:8080/api/tramites/{tramiteId}/pdf
```

**Opción B - Usando Postman:**
```http
GET http://localhost:8080/api/tramites/{tramiteId}/pdf
Authorization: Bearer {tu_token_jwt}
```
- Click en "Send and Download"
- Guardar como `comprobante.pdf`

**Opción C - Usando el Frontend:**
1. Ir a lista de trámites
2. Buscar trámite COMPLETADO
3. Click en botón "Descargar PDF"

---

### 5. Verificar el PDF (30 seg):

Abrir el PDF y verificar:

#### ✅ Encabezado:
- [ ] Fondo azul (#2563eb)
- [ ] Título "COMPROBANTE OFICIAL" en blanco grande
- [ ] Subtítulo "Sistema de Gestión de Trámites"
- [ ] Badge verde "✓ COMPLETADO" a la derecha

#### ✅ Información Principal:
- [ ] Tabla con fondo gris claro en etiquetas
- [ ] Código del trámite
- [ ] Nombre del cliente
- [ ] Email del cliente
- [ ] Departamento
- [ ] Fecha de inicio (formato DD/MM/YYYY HH:MM)
- [ ] Fecha de finalización

#### ✅ Historial del Proceso:
- [ ] Tabla con encabezado azul oscuro
- [ ] Columnas: #, Paso, Departamento
- [ ] Filas alternadas (blanco/gris claro)
- [ ] Checkmark ✓ en pasos completados

#### ✅ Sello Digital:
- [ ] Borde azul de 2px
- [ ] Fondo azul muy claro
- [ ] Texto "DOCUMENTO VERIFICADO"
- [ ] Fecha y hora de generación
- [ ] ID único del trámite (12 caracteres)
- [ ] Centrado en la página

#### ✅ Nota Legal:
- [ ] Texto pequeño al pie
- [ ] Centrado
- [ ] Color gris

---

## 🎨 EJEMPLO VISUAL ESPERADO

```
╔═══════════════════════════════════════════════════════════╗
║ [FONDO AZUL #2563eb]                                      ║
║ COMPROBANTE OFICIAL              ║ [FONDO VERDE]         ║
║ Sistema de Gestión               ║ ✓ COMPLETADO         ║
║ de Trámites                      ║                       ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║ [TABLA CON FILAS ALTERNADAS GRIS/BLANCO]                 ║
║ Código de Trámite:    TRM-2026-XXXX                      ║
║ Cliente:              Juan Pérez                          ║
║ Email:                juan@ejemplo.com                    ║
║ Departamento:         Recepción                           ║
║ Fecha de Inicio:      28/04/2026 10:00                   ║
║ Fecha de Finalización: 28/04/2026 15:30                  ║
║                                                           ║
║ ─────────────────────────────────────────────────────    ║
║ Historial del Proceso                                     ║
║ ─────────────────────────────────────────────────────    ║
║                                                           ║
║ [TABLA CON ENCABEZADO AZUL OSCURO]                       ║
║  #  │ Paso              │ Departamento                   ║
║ ────┼───────────────────┼────────────────────            ║
║  1  │ Inicio ✓          │ Recepción                      ║
║  2  │ Validación ✓      │ Validación                     ║
║  3  │ Aprobación ✓      │ Gerencia                       ║
║  4  │ Finalización ✓    │ Cierre                         ║
║                                                           ║
║ ─────────────────────────────────────────────────────    ║
║                                                           ║
║              ┌─────────────────────────┐                 ║
║              │ [BORDE AZUL 2px]        │                 ║
║              │ DOCUMENTO VERIFICADO    │                 ║
║              │                         │                 ║
║              │ Generado: 28/04/2026    │                 ║
║              │          22:30:45       │                 ║
║              │ ID: 66f8a9b2c1d4        │                 ║
║              └─────────────────────────┘                 ║
║                                                           ║
║  [TEXTO PEQUEÑO GRIS CENTRADO]                           ║
║  Este documento es un comprobante oficial generado       ║
║  automáticamente por el Sistema de Gestión de Trámites   ║
╚═══════════════════════════════════════════════════════════╝
```

---

## 🎯 COLORES A VERIFICAR

### Azul Primario (#2563eb):
- Encabezado principal
- Títulos de sección
- Borde del sello

### Azul Oscuro (#1e40af):
- Encabezado de tabla de historial

### Verde Éxito (#10b981):
- Badge "COMPLETADO"

### Gris Claro (#f8fafc):
- Filas alternadas en tablas
- Fondo de etiquetas

### Azul Muy Claro (#eff6ff):
- Fondo del sello digital

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error: "Trámite no encontrado"
**Solución:** Verifica que el ID del trámite sea correcto

### Error: "El trámite aún no está finalizado"
**Solución:** Cambia el estado del trámite a `COMPLETADO`

### Error: "Unauthorized"
**Solución:** Agrega el token JWT en el header Authorization

### El PDF se descarga pero está en blanco:
**Solución:** 
1. Verifica que el trámite tenga datos
2. Revisa los logs del backend
3. Verifica que iText esté en el classpath

### Los colores no se ven:
**Solución:** Abre el PDF con Adobe Reader o Chrome (no todos los lectores soportan colores)

### El sello no aparece:
**Solución:** Verifica que el trámite tenga un ID válido

---

## 📝 CÓDIGO RELEVANTE

### Endpoint del PDF:
```java
// TramiteController.java
@GetMapping("/{id}/pdf")
public ResponseEntity<byte[]> descargarPdf(@PathVariable String id) {
    byte[] pdf = tramiteService.generarPdfCierre(id);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "tramite-" + id + ".pdf");
    
    return ResponseEntity.ok()
            .headers(headers)
            .body(pdf);
}
```

### Método de Generación:
```java
// TramiteService.java
public byte[] generarPdfCierre(String tramiteId) {
    // Verificar que esté completado
    if (tramite.getEstado() != Tramite.EstadoTramite.COMPLETADO) {
        throw new RuntimeException("El trámite aún no está finalizado");
    }
    
    // Generar PDF con diseño profesional
    // ~300 líneas de código
}
```

---

## ✅ CHECKLIST RÁPIDO

- [ ] Backend corriendo
- [ ] Trámite creado
- [ ] Trámite completado
- [ ] PDF descargado
- [ ] Encabezado azul visible
- [ ] Badge verde visible
- [ ] Información completa
- [ ] Historial con checkmarks
- [ ] Sello digital visible
- [ ] Nota legal al pie
- [ ] Diseño profesional ✨

---

## 🎉 RESULTADO ESPERADO

Un PDF profesional que:
- ✅ Se ve bonito y moderno
- ✅ Tiene colores corporativos
- ✅ Está bien organizado
- ✅ Tiene sello de verificación
- ✅ Es oficial y confiable
- ✅ Impresiona en la demo 🌟

---

## 🚀 PARA LA DEMO

### Preparar:
1. Tener un trámite COMPLETADO listo
2. Conocer el ID del trámite
3. Tener el navegador abierto

### Durante la demo:
1. Ir a lista de trámites
2. Click "Descargar PDF"
3. Abrir PDF descargado
4. Mostrar diseño profesional
5. Explicar sello digital
6. Mencionar verificación

### Mencionar:
- Diseño profesional con colores corporativos
- Sello digital con fecha y ID único
- Formato oficial para comprobantes
- Generado automáticamente con iText
- Listo para imprimir o enviar por email

---

**¡El PDF está listo para impresionar! 📄✨**

**Última actualización:** 28 Abril 2026, 22:30 hrs
