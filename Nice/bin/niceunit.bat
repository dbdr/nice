@echo off
rem ---------------------------------------------------------------------------
rem nicedoc.bat - script to run unit tests in compiled Nice sources
rem
rem Environment Variariable Prerequisites:
rem
rem     NICE - the root directory where nice is installed
rem ---------------------------------------------------------------------------

call niceclasspath

for %%x in (%NICEPATH%) do goto gotNice

goto end

:gotNice
java -classpath %NICEPATH%\nice.jar;%CLASSPATH% nice.tools.unit.console.fun %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
rem -- clean up a bit
set NICEPATH=