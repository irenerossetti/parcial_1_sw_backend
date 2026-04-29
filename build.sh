#!/bin/bash
set -e

echo "=== Iniciando build del backend ==="

# Dar permisos de ejecución a mvnw
chmod +x mvnw

# Limpiar y compilar
./mvnw clean package -DskipTests

echo "=== Build completado ==="
