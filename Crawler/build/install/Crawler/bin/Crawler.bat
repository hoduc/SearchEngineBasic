@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Crawler startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and CRAWLER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\Crawler.jar;%APP_HOME%\lib\crawler4j.jar;%APP_HOME%\lib\log4j-core-2.6.2.jar;%APP_HOME%\lib\slf4j-api-1.7.13.jar;%APP_HOME%\lib\lidalia-slf4j-ext-1.0.0.jar;%APP_HOME%\lib\logback-classic-1.1.2.jar;%APP_HOME%\lib\httpclient-4.4.jar;%APP_HOME%\lib\je-5.0.73.jar;%APP_HOME%\lib\tika-parsers-1.5.jar;%APP_HOME%\lib\log4j-api-2.6.2.jar;%APP_HOME%\lib\guava-14.0.1.jar;%APP_HOME%\lib\logback-core-1.1.2.jar;%APP_HOME%\lib\httpcore-4.4.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.9.jar;%APP_HOME%\lib\tika-core-1.5.jar;%APP_HOME%\lib\vorbis-java-tika-0.1.jar;%APP_HOME%\lib\netcdf-4.2-min.jar;%APP_HOME%\lib\apache-mime4j-core-0.7.2.jar;%APP_HOME%\lib\apache-mime4j-dom-0.7.2.jar;%APP_HOME%\lib\commons-compress-1.5.jar;%APP_HOME%\lib\pdfbox-1.8.4.jar;%APP_HOME%\lib\bcmail-jdk15-1.45.jar;%APP_HOME%\lib\bcprov-jdk15-1.45.jar;%APP_HOME%\lib\poi-3.10-beta2.jar;%APP_HOME%\lib\poi-scratchpad-3.10-beta2.jar;%APP_HOME%\lib\poi-ooxml-3.10-beta2.jar;%APP_HOME%\lib\geronimo-stax-api_1.0_spec-1.0.1.jar;%APP_HOME%\lib\tagsoup-1.2.1.jar;%APP_HOME%\lib\asm-debug-all-4.1.jar;%APP_HOME%\lib\isoparser-1.0-RC-1.jar;%APP_HOME%\lib\metadata-extractor-2.6.2.jar;%APP_HOME%\lib\boilerpipe-1.1.0.jar;%APP_HOME%\lib\rome-0.9.jar;%APP_HOME%\lib\vorbis-java-core-0.1.jar;%APP_HOME%\lib\vorbis-java-core-0.1-tests.jar;%APP_HOME%\lib\juniversalchardet-1.0.3.jar;%APP_HOME%\lib\jhighlight-1.0.jar;%APP_HOME%\lib\xz-1.2.jar;%APP_HOME%\lib\fontbox-1.8.4.jar;%APP_HOME%\lib\jempbox-1.8.4.jar;%APP_HOME%\lib\poi-ooxml-schemas-3.10-beta2.jar;%APP_HOME%\lib\dom4j-1.6.1.jar;%APP_HOME%\lib\aspectjrt-1.6.11.jar;%APP_HOME%\lib\xmpcore-5.1.2.jar;%APP_HOME%\lib\xercesImpl-2.8.1.jar;%APP_HOME%\lib\jdom-1.0.jar;%APP_HOME%\lib\xmlbeans-2.3.0.jar;%APP_HOME%\lib\xml-apis-1.3.03.jar

@rem Execute Crawler
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CRAWLER_OPTS%  -classpath "%CLASSPATH%" mypackage.NewsController %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable CRAWLER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%CRAWLER_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
