rem -- This is a utility to determine the location of the nice.jar file
rem -- which is needed by several Nice utilities (nicec, nicedoc, ...).

rem -- do we have a nice environment variable?
for %%x in (%NICE%) do goto gotNiceEnvVar

rem -- try some standard places

set NICEPATH=c:\nice
if exist "%NICEPATH%\nice.jar" goto end

set NICEPATH="c:\Program Files\nice"
if exist "%NICEPATH%\nice.jar" goto end

set NICEPATH=c:\programs\nice
if exist "%NICEPATH%\nice.jar" goto end

set NICEPATH=d:\nice
if exist "%NICEPATH%\nice.jar" goto end

set NICEPATH="d:\Program Files\nice"
if exist "%NICEPATH%\nice.jar" goto end

set NICEPATH=d:\programs\nice
if exist "%NICEPATH%\nice.jar" goto end

goto error

:envError
echo Could not find the file: %NICE%\nice.jar
echo The NICE environment variable is not pointing to the right directory!

:error
echo You must set the NICE environment variable to point to the directory you've installed nice in e.g.
echo set NICE=C:\programs\nice	echo (note: do not add a ';' at the end)
echo You can do it by modifying Autoexec.bat or in the system settings.
set NICEPATH=
goto end

:gotNiceEnvVar
if not exist "%NICE%\nice.jar" goto envError
set NICEPATH=%NICE%

:end