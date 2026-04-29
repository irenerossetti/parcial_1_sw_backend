# 📋 Endpoints Disponibles para el Cliente

## ✅ Endpoints que YA funcionan para CLIENTE:

### 1. Autenticación
```
POST /api/auth/login
Body: { "email": "cliente@example.com", "password": "123456" }
Response: { "token": "jwt-token", "rol": "CLIENTE", ... }
```

### 2. Crear Trámite
```
POST /api/tramites
Headers: Authorization: Bearer {token}
Body: {
  "politicaId": "id-de-politica",
  "descripcion": "Solicitud de instalación eléctrica"
}
Response: Tramite creado con código único
```

### 3. Ver Mis Trámites
```
GET /api/tramites
Headers: Authorization: Bearer {token}
Response: Lista de trámites del cliente autenticado
```

### 4. Ver Detalle de un Trámite
```
GET /api/tramites/{id}
Headers: Authorization: Bearer {token}
Response: Detalle completo del trámite
```

### 5. Ver Timeline/Progreso
```
GET /api/tramites/{id}/ejecucion
Headers: Authorization: Bearer {token}
Response: {
  "tramite": {...},
  "estadoEjecucion": {
    "nodoActual": "Validación",
    "progreso": 50,
    "timeline": [...]
  }
}
```

### 6. Descargar PDF
```
GET /api/tramites/{id}/pdf
Headers: Authorization: Bearer {token}
Response: Archivo PDF del trámite
```

### 7. Ver Notificaciones
```
GET /api/notificaciones
Headers: Authorization: Bearer {token}
Response: Lista de notificaciones del usuario
```

### 8. Marcar Notificación como Leída
```
PUT /api/notificaciones/{id}/leida
Headers: Authorization: Bearer {token}
```

---

## 🔒 Seguridad Implementada:

- ✅ JWT Authentication
- ✅ El cliente solo ve SUS trámites
- ✅ Validación de permisos en cada endpoint
- ✅ CORS configurado para localhost:4200

---

## 🎯 Flujo Completo del Cliente:

```
1. Login → Obtiene token JWT
2. Ver políticas disponibles → GET /api/politicas
3. Crear trámite → POST /api/tramites
4. Ver mis trámites → GET /api/tramites
5. Ver progreso → GET /api/tramites/{id}/ejecucion
6. Recibir notificaciones → WebSocket + GET /api/notificaciones
7. Descargar PDF → GET /api/tramites/{id}/pdf
```

---

## ✅ Estado: COMPLETO

El backend tiene TODO lo necesario para que el cliente:
- Cree trámites
- Vea su progreso
- Reciba notificaciones
- Descargue PDFs
- Vea timeline

**No necesitas agregar nada más para el flujo básico del cliente.**
