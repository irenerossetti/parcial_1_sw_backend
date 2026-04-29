# 📋 CÓMO COMPLETAR UN TRÁMITE

## 🎯 OBJETIVO
Tener al menos un trámite COMPLETADO para probar la descarga de PDF.

---

## 🚀 OPCIÓN 1: Desde el Frontend (Recomendado)

### Como FUNCIONARIO o ADMIN:

1. **Login**
   - Email: `admin@test.com` o `funcionario@test.com`
   - Password: `password123`

2. **Ir a Gestión de Trámites**
   - Click en menú "Gestión de Trámites" o "Operativa"

3. **Seleccionar un trámite**
   - Click en cualquier trámite de la lista

4. **Cambiar estado a COMPLETADO**
   - Busca el selector de estado
   - Selecciona "COMPLETADO"
   - Agrega una observación (opcional)
   - Click en "Guardar" o "Actualizar"

5. **Verificar**
   - El trámite debe aparecer con estado COMPLETADO
   - Debe tener fecha de finalización

---

## 🚀 OPCIÓN 2: Avanzar el Trámite Paso a Paso

### Flujo completo:

1. **Como CLIENTE - Crear trámite**
   - Login: `cliente@test.com` / `password123`
   - Click "Crear Trámite"
   - Llenar formulario
   - Enviar

2. **Como FUNCIONARIO - Procesar**
   - Login: `funcionario@test.com` / `password123`
   - Ver trámite en su departamento
   - Click "Avanzar" o "Procesar"
   - Completar campos requeridos
   - Enviar

3. **Repetir hasta COMPLETADO**
   - El trámite avanzará por los diferentes departamentos
   - Hasta llegar al estado COMPLETADO

---

## 🚀 OPCIÓN 3: Desde MongoDB (Avanzado)

Si tienes acceso a MongoDB Compass o mongosh:

```javascript
// Conectar a MongoDB
use workflow_db

// Actualizar un trámite a COMPLETADO
db.tramites.updateOne(
  { codigo: "TRM-2026-0001" },  // Cambia por el código de tu trámite
  { 
    $set: { 
      estado: "COMPLETADO",
      finalizadoEn: new Date()
    }
  }
)

// Verificar
db.tramites.findOne({ codigo: "TRM-2026-0001" })
```

---

## 🔍 VERIFICAR QUE TIENES TRÁMITES COMPLETADOS

### Desde el Frontend:
1. Login como CLIENTE
2. Ve a "Mis Trámites"
3. Busca trámites con badge verde "COMPLETADO"

### Desde MongoDB:
```javascript
db.tramites.find({ estado: "COMPLETADO" }).count()
```

---

## 📊 ESTADOS DE TRÁMITE

```
NUEVO        → Recién creado
EN_PROCESO   → En procesamiento
PENDIENTE    → Esperando información
EN_MORA      → Demorado
COMPLETADO   → ✅ Finalizado (puede descargar PDF)
RECHAZADO    → ❌ Rechazado
```

---

## 🎯 PARA LA DEMO

Recomendación: Tener 3 trámites COMPLETADOS con diferentes datos:

1. **Trámite 1:** Solicitud de permiso
2. **Trámite 2:** Solicitud de certificado
3. **Trámite 3:** Solicitud de licencia

Así puedes demostrar que el PDF se genera correctamente con diferentes datos.

---

## ⚠️ IMPORTANTE

El botón "Descargar PDF" solo aparecerá o funcionará si:
- ✅ El trámite está en estado COMPLETADO
- ✅ El usuario tiene permisos (es el dueño del trámite)
- ✅ El backend está corriendo
- ✅ El usuario está autenticado

---

## 🐛 TROUBLESHOOTING

### "Solo se pueden descargar PDFs de trámites completados"
**Causa:** El trámite no está COMPLETADO
**Solución:** Completar el trámite usando una de las opciones anteriores

### "No autorizado para descargar el PDF"
**Causa:** El usuario no tiene permisos
**Solución:** Login con el usuario correcto (dueño del trámite, funcionario del departamento, o admin)

### "Trámite no encontrado"
**Causa:** El ID del trámite es incorrecto
**Solución:** Verificar que el trámite existe en la base de datos

---

## 📝 CHECKLIST

- [ ] Backend corriendo
- [ ] Frontend corriendo
- [ ] Usuario autenticado
- [ ] Al menos 1 trámite COMPLETADO
- [ ] Botón "Descargar PDF" visible
- [ ] Click en "Descargar PDF"
- [ ] PDF se descarga correctamente
- [ ] PDF se abre sin errores
- [ ] PDF tiene diseño profesional

---

**Siguiente paso:** Una vez que tengas un trámite COMPLETADO, prueba descargar el PDF.
