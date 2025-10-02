@echo off
setlocal ENABLEDELAYEDEXPANSION
set APP_HOME=%~dp0
set WRAP_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

REM Offline bridge for Windows: detect --offline or UNIFY_OFFLINE=1
set OFFLINE_REQ=0
for %%A in (%*) do (
  if "%%~A"=="--offline" set OFFLINE_REQ=1
)
if "%UNIFY_OFFLINE%"=="1" set OFFLINE_REQ=1

if "%OFFLINE_REQ%"=="1" (
  if exist "%APP_HOME%\scripts\bootstrap-offline-gradle.sh" (
    bash "%APP_HOME%\scripts\bootstrap-offline-gradle.sh"
  )
  if exist "%APP_HOME%\scripts\gradle-offline.sh" (
    bash "%APP_HOME%\scripts\gradle-offline.sh" %*
    exit /b !ERRORLEVEL!
  )
  echo [gradlew.bat] offline requested but no offline helper found; continuing with wrapper...
)

if not exist "%WRAP_JAR%" (
  if exist "%APP_HOME%\scripts\bootstrap-wrapper.sh" (
    bash "%APP_HOME%\scripts\bootstrap-wrapper.sh"
  )
)

set CLASSPATH=%WRAP_JAR%
if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)
"%JAVA_EXE%" -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
