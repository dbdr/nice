
TOP=${HOME}/Nice

JAVAC=javac -classpath ${TOP}/classes:${TOP}/src:${TOP}/stdlib -sourcepath ${TOP}/src -d ${TOP}/classes -O -g
JAVACC=java -classpath ${HOME}/lib/java/JavaCC/JavaCC.zip COM.sun.labs.javacc.Main
JAVADOC=javadoc -linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

recompile:
	cd src;\
	${JAVAC} bossa/*/*.java nice/**/*.java mlsub/*/*.java ../stdlib/nice/lang/*.java

recompileGNU:
	cd src; ${JAVAC} gnu/*/*.java gnu/*/*/*.java kawa/*/*.java

recompileSTDLIB:
	${JAVAC} ${TOP}/stdlib/nice/lang/*.java

parser:
	cd src/bossa/parser; ${JAVACC} Parser.jj

clean:
	rm -rf ${TOP}/classes/*

scratch: clean parser recompile

test:
	cd regtest; ./regtest

## Documentation

jdoc:
	mkdir -p doc
	${JAVADOC} -windowtitle "Nice" -doctitle "The Nice compiler source" \
	-overview ${TOP}/src/overview.html \
	-d ${TOP}/doc -private -author -version \
	mlsub.typing mlsub.typing.lowlevel mlsub.compilation bossa bossa.syntax bossa.util bossa.link gnu.bytecode gnu.expr gnu.math gnu.mapping kawa.standard kawa.lang

tags:
	jtags

jar:
	cp src/nice/tools/compiler.jar lib/nice/Nice.jar
	jar umf src/mainClass lib/nice/Nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu -C classes kawa
	cd stdlib; jar uf ../lib/nice/Nice.jar nice/*/*.nice*

dist: jar
	tar cf lib/nice/Nice.tar bin/nicer bin/nicec lib/nice/Nice.jar lib/emacs/nice-mode.el

# CVS

status:
	cvs status 2>&1 | egrep "File:|Examining" | grep -v "Up-to-date"
