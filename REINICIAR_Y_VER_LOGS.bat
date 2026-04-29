@echo off
cls
echo ================================================
echo    REINICIANDO BACKEND CON LOGS
echo ================================================
echo.

echo [1/3] Deteniendo Java...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo [2/3] Limpiando...
if exist target rmdir /s /q target
timeout /t 1 /nobreak >nul

echo [3/3] Iniciando backend...
echo.
echo ================================================
echo  MIRA LOS LOGS CUANDO INTENTES DESCARGAR PDF
echo ================================================
echo.

mvnw.cmd clean spring-boot:run
