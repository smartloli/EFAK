@echo off

rem ---------------------------------------------------------------------------
rem Start script for the KE Server
rem ---------------------------------------------------------------------------

echo KE Service check ...

setlocal

if exist "%JAVA_HOME%" goto okKafkaEagke
echo Error: The JAVA_HOME environment variable is not defined correctly.
echo Error: This environment variable is needed to run this program.
goto end

:okKafkaEagke
if exist "%KE_HOME%" goto okHome
echo Error: The KE_HOME environment variable is not defined correctly.
echo Error: This environment variable is needed to run this program.
goto end

:okHome

if exist %KE_HOME%\kms\webapps\ke (
    rem Delete ke dir menu.
    rd /s /q %KE_HOME%\kms\webapps\ke
)

if exist %KE_HOME%\kms\work (
    rem Delete tomcat work dir menu.
    rd /s /q %KE_HOME%\kms\work
)

md %KE_HOME%\kms\webapps\ke
cd %KE_HOME%\kms\webapps\ke
rem echo Current Path :: %cd%
%JAVA_HOME%\bin\jar -xvf %KE_HOME%\kms\webapps\ke.war

ping /n 2 127.1 >nul

cd %KE_HOME%
SET CLSNAME=org.smartloli.kafka.eagle.plugin.server.TomcatServerListen

setlocal enabledelayedexpansion
for %%i in (%KE_HOME%\kms\webapps\ke\WEB-INF\lib\dom*.jar,%KE_HOME%\kms\webapps\ke\WEB-INF\lib\log4j*.jar,%KE_HOME%\kms\webapps\ke\WEB-INF\lib\slf4j*.jar,%KE_HOME%\kms\webapps\ke\WEB-INF\lib\kafka-eagle*.jar) do (
  set CLSPATH=!CLSPATH!;%%i
)
rem echo %CLSPATH%
%JAVA_HOME%\bin\java -classpath %CLSPATH% %CLSNAME% >> %KE_HOME%\logs\ke.out

echo Kafka Eagle system monitor port successful...

ping /n 3 127.1 >nul

if exist %KE_HOME%\kms\webapps\ke\WEB-INF\classes\*.properties (
  rd /s /q %KE_HOME%\kms\webapps\ke\WEB-INF\classes\*.properties
)
copy %KE_HOME%\conf\*.properties %KE_HOME%\kms\webapps\ke\WEB-INF\classes\

ping /n 3 127.1 >nul
set CATALINA_HOME=%KE_HOME%\kms
call  %KE_HOME%\kms\bin\startup.bat

:end
