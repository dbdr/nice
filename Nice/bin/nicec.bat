@echo off
rem ---------------------------------------------------------------------------
rem nicec.bat - a nice compiler batch file
rem
rem Environment Variariable Prerequisites:
rem
rem     NICE - the root directory where nice is installed
rem
rem thanks to the catalina team for some batch file inspiration
rem
rem questions to jamie@thecodecollective.com
rem
rem ---------------------------------------------------------------------------

rem -- save the original classpath
set ORIGINALCLASSPATH=%CLASSPATH%

rem -- do we have a nice environment variable?
if not "%NICE%" == "" goto gotNice
echo You must set NICE to point to the directory you've installed nice in e.g.
echo set NICE=c:\java\nice
goto cleanup
:gotNice

rem -- set up the reference to the nice jar file
set NICEJAR=%NICE%\share\java\nice.jar
set CLASSPATH=%CLASSPATH%;%NICEJAR%
java -Dnice.systemJar=%NICEJAR% -Dnice.runtime=%NICEJAR% nice.tools.compiler.package %1 %2 %3 %4 %5 %6 %7 %8 %9

rem -- cleanup environment variables
:cleanup
set NICEJAR=
set CLASSPATH=%ORIGINALCLASSPATH%
:finish
