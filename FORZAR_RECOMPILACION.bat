@echo off
echo ========================================
echo FORZANDO RECOMPILACION COMPLETA
echo ========================================
echo.

echo [1/4] Matando procesos Java...
taskkill /F /IM java.exe 2>nul
timeout /t 3 /nobreak >nul

echo.
echo [2/4] Eliminando carpeta target...
if exist target rmdir /s /q target
timeout /t 2 /nobreak >nul

echo.
echo [3/4] Limpiando proyecto Maven...
call mvnw.cmd clean
timeout /t 2 /nobreak >nul

echo.
echo [4/4] Compilando y ejecutando...
call mvnw.cmd spring-boot:run

pause
