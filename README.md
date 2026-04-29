# Workflow Backend - Spring Boot

Backend del sistema de gestión de trámites con workflow.

## 🚀 Despliegue en Render

### Configuración Manual

1. **Build Command**:
   ```bash
   chmod +x mvnw && ./mvnw clean package -DskipTests
   ```

2. **Start Command**:
   ```bash
   java -Dserver.port=$PORT -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

3. **Variables de Entorno**:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `MONGODB_URI` = Tu connection string de MongoDB Atlas
   - `JWT_SECRET` = Una clave secreta larga

### Verificar Despliegue

```bash
curl https://tu-app.onrender.com/api/kpis/test
```

## 🏃 Ejecutar Localmente

```bash
./mvnw spring-boot:run
```

## 📝 Usuarios de Prueba

- **Admin**: admin@workflow.com / admin123
- **Funcionario**: funcionario@workflow.com / func123

## 📖 Documentación Completa

Ver [RENDER_CONFIG.md](./RENDER_CONFIG.md) para más detalles.
