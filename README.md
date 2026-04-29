# Workflow Backend - Spring Boot

Backend del sistema de gestión de trámites con workflow.

## 🚀 Despliegue en Render

### Configuración Manual en Render Dashboard

1. **Environment**: Selecciona `Java`

2. **Build Command**:
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start Command**:
   ```bash
   java -Dserver.port=$PORT -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

4. **Variables de Entorno** (Environment Variables):
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `MONGODB_URI` = Tu connection string de MongoDB Atlas
   - `JWT_SECRET` = Una clave secreta larga (ej: `my-secret-key-12345678`)

### ⚠️ IMPORTANTE: NO agregues la variable PORT

Render proporciona automáticamente la variable `$PORT`. No la configures manualmente.

### Verificar Despliegue

Una vez desplegado, prueba:

```bash
curl https://tu-app.onrender.com/api/kpis/test
```

Deberías ver:
```json
{
  "message": "KPI Controller está funcionando - VERSION 2.0",
  "timestamp": "..."
}
```

## 🏃 Ejecutar Localmente

```bash
./mvnw spring-boot:run
```

O si tienes Maven instalado:

```bash
mvn spring-boot:run
```

## 📝 Usuarios de Prueba

- **Admin**: admin@workflow.com / admin123
- **Funcionario**: funcionario@workflow.com / func123

## 🔧 Troubleshooting

### Error 127 al desplegar
Usa Maven del sistema en lugar del wrapper:
```bash
mvn clean package -DskipTests
```

### Error de conexión a MongoDB
- Verifica que `MONGODB_URI` esté correctamente configurado
- Asegúrate de que MongoDB Atlas permita conexiones desde 0.0.0.0/0
- El formato debe ser: `mongodb+srv://usuario:password@cluster.mongodb.net/workflow`

### Error "Port already in use"
- NO configures la variable `PORT` manualmente
- Render la proporciona automáticamente como `$PORT`

## 📖 Documentación Completa

Ver [RENDER_CONFIG.md](./RENDER_CONFIG.md) para más detalles.
