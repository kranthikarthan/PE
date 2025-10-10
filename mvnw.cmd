@ECHO OFF
SETLOCAL

SET "MVNW_ROOT=%~dp0"
SET "WRAPPER_JAR=%MVNW_ROOT%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_PROPERTIES=%MVNW_ROOT%\.mvn\wrapper\maven-wrapper.properties"

IF NOT EXIST "%WRAPPER_PROPERTIES%" (
  ECHO Cannot find %WRAPPER_PROPERTIES%
  EXIT /B 1
)

FOR /F "tokens=1,2 delims==" %%A IN ('findstr /R "^wrapperUrl=" "%WRAPPER_PROPERTIES%"') DO (
  SET "WRAPPER_URL=%%B"
)

IF NOT EXIST "%WRAPPER_JAR%" (
  IF EXIST "%SystemRoot%\System32\curl.exe" (
    "%SystemRoot%\System32\curl.exe" -fsSL "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
  ) ELSE (
    powershell -Command "Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
  )
)

SET JAVA_EXE=java
IF DEFINED JAVA_HOME (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

"%JAVA_EXE%" %JAVA_OPTS% -Dmaven.multiModuleProjectDirectory="%MVNW_ROOT%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_OPTS% %*
