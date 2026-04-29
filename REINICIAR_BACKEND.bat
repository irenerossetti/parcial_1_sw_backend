@echo off
echo ========================================
echo REINICIANDO BACKEND CON RECOMPILACION
echo ========================================
echo.

echo [1/3] Deteniendo backend si esta corriendo...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [2/3] Limpiando y recompilando...
call mvnw.cmd clean compile

echo.
echo [3/3] Iniciando backend...
call mvnw.cmd spring-boot:run

pause
