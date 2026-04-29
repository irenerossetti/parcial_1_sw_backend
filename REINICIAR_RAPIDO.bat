@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Deteniendo Java...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo Compilando...
call mvnw.cmd package -DskipTests

echo Iniciando backend...
java -jar target\backend-0.0.1-SNAPSHOT.jar
