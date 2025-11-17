@echo off
echo ========================================
echo   DESPLIEGUE AUTOMATICO - TOMCAT
echo ========================================

echo.
echo [1/4] Compilando proyecto...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo.
echo [2/4] Deteniendo Tomcat...
cd C:\tomcat10\bin
call shutdown.bat
timeout /t 5

echo.
echo [3/4] Copiando WAR...
del /q C:\tomcat10\webapps\ROOT.war 2>nul
rmdir /s /q C:\tomcat10\webapps\ROOT 2>nul
copy %~dp0target\gestionbodegas-0.0.1-SNAPSHOT.war C:\tomcat10\webapps\ROOT.war

echo.
echo [4/4] Iniciando Tomcat...
call startup.bat

echo.
echo ========================================
echo   DESPLIEGUE COMPLETADO
echo ========================================
echo.
echo Accede a: http://localhost:8080/html/login.html
echo.
pause