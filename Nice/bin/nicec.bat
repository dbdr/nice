@echo off
rem ---------------------------------------------------------------------------
rem nicec.bat - a nice compiler batch file
rem
rem Environment Variariable Prerequisites:
rem
rem     NICE - the root directory where nice is installed
rem ---------------------------------------------------------------------------


rem -- do we have a nice environment variable?
for %%x in (%NICE%) do goto gotNiceEnvVar

rem -- try some standard places

set NICEPATH=c:\nice
if exist %NICEPATH%\nice.jar goto gotNice

set NICEPATH="c:\Program Files\nice"
if exist %NICEPATH%\nice.jar goto gotNice

set NICEPATH=c:\programs\nice
if exist %NICEPATH%\nice.jar goto gotNice

set NICEPATH=d:\nice
if exist %NICEPATH%\nice.jar goto gotNice

set NICEPATH="d:\Program Files\nice"
if exist %NICEPATH%\nice.jar goto gotNice

set NICEPATH=d:\programs\nice
if exist %NICEPATH%\nice.jar goto gotNice

goto error

:envError
echo Could not find the file: %NICE%\nice.jar
echo The NICE environment variable is not pointing to the right directory!

:error
echo You must set the NICE environment variable to point to the directory you've installed nice in e.g.
echo set NICE=C:\programs\nice	echo (note: do not add a ';' at the end)
echo You can do it by modifying Autoexec.bat or in the system settings.
goto end

:gotNiceEnvVar
if not exist %NICE%\nice.jar goto envError
set NICEPATH=%NICE%

:gotNice
java -classpath %NICEPATH%\nice.jar;%CLASSPATH% nice.tools.compiler.console.fun --runtime=%NICEPATH%\nice.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
rem -- clean up a bit
set NICEPATH=