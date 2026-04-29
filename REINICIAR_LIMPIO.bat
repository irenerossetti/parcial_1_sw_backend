@echo off
cls
echo ================================================
echo    REINICIO LIMPIO DEL BACKEND
echo ================================================
echo.
echo Este script va a:
echo  1. Detener el backend
echo  2. Limpiar la compilacion
echo  3. Recompilar desde cero
echo  4. Iniciar el backend
echo.
echo Presiona cualquier tecla para continuar...
pause >nul

echo.
echo [PASO 1/4] Deteniendo procesos Java...
taskkill /F /IM java.exe 2>nul
if %errorlevel% == 0 (
    echo    ^> Procesos detenidos
) else (
    echo    ^> No habia procesos corriendo
)
timeout /t 2 /nobreak >nul

echo.
echo [PASO 2/4] Eliminando carpeta target...
if exist target (
    rmdir /s /q target
    echo    ^> Carpeta target eliminada
) else (
    echo    ^> No existia carpeta target
)
timeout /t 1 /nobreak >nul

echo.
echo [PASO 3/4] Limpiando proyecto Maven...
call mvnw.cmd clean
echo    ^> Limpieza completada
timeout /t 1 /nobreak >nul

echo.
echo [PASO 4/4] Compilando e iniciando backend...
echo.
echo ================================================
echo    BACKEND INICIANDO...
echo    Espera a ver: "Started BackendApplication"
echo ================================================
echo.

call mvnw.cmd spring-boot:run
