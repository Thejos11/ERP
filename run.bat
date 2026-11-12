@echo off
chcp 65001 > nul
title Little Horizons ERP - Sistema Educativo

set "JAVA_CMD=java"
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "%USERPROFILE%\.jdks\ms-21.0.12.1\bin\java.exe" (
        set "JAVA_CMD=%USERPROFILE%\.jdks\ms-21.0.12.1\bin\java.exe"
    ) else if exist "%USERPROFILE%\.jdks\openjdk-27\bin\java.exe" (
        set "JAVA_CMD=%USERPROFILE%\.jdks\openjdk-27\bin\java.exe"
    ) else (
        echo [ERROR] No se encontro Java instalado en el sistema.
        echo Por favor instale JDK 21 o configure JAVA_HOME en su PATH.
        pause
        exit /b 1
    )
)

if not exist "%~dp0target\little-horizons-erp-1.0.0.jar" (
    echo [INFO] Empaquetando la aplicacion...
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
        call "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" clean package -DskipTests
    ) else (
        call mvn clean package -DskipTests
    )
)

"%JAVA_CMD%" -Dfile.encoding=UTF-8 -jar "%~dp0target\little-horizons-erp-1.0.0.jar"

if %ERRORLEVEL% neq 0 (
    echo.
    echo Ocurrio un detalle al cerrar la aplicacion. Revise error_log.txt si hubo errores.
)
pause
