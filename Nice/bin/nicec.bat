@echo off
rem ---------------------------------------------------------------------------
rem nicec.bat - a nice compiler batch file
rem
rem Environment Variariable Prerequisites:
rem
rem     NICE - the root directory where nice is installed
rem ---------------------------------------------------------------------------

call niceclasspath

for %%x in (%NICEPATH%) do goto gotNice

goto end

:gotNice
java -Xms8M -classpath %NICEPATH%\nice.jar;%CLASSPATH% nice.tools.compiler.console.dispatch --runtime=%NICEPATH%\nice.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
rem -- clean up a bit
set NICEPATH=
@echo on