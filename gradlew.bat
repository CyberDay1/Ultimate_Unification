@echo off
setlocal
set APP_HOME=%~dp0
set WRAP_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
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
