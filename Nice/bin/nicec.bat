@echo off
rem ---------------------------------------------------------------------------
rem nicec.bat - a nice compiler batch file
rem
rem Environment Variariable Prerequisites:
rem
rem     NICE - the root directory where nice is installed
rem ---------------------------------------------------------------------------

rem -- do we have a nice environment variable?
if not "%NICE%" == "" goto gotNice

echo You must set the NICE environment variable to point to the directory you've installed nice in e.g.
echo set NICE=C:\programs\nice	(note: do not add a ';' at the end) 
echo You can do it by modifying Autoexec.bat or in the system settings.
goto end

:gotNice
java -classpath %NICE%\nice.jar;%CLASSPATH% nice.tools.compiler.fun --runtime=%NICE%\nice.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
