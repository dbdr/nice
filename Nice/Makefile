
TOP=${HOME}/Nice

JAVAC=javac -classpath ${TOP}/classes:${TOP}/src:${TOP}/stdlib -sourcepath ${TOP}/src -d ${TOP}/classes -O -g
JAVACC=java -classpath ${TOP}/external/JavaCC.zip COM.sun.labs.javacc.Main
JAVADOC=javadoc #-linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

# run this first when you just checked out a fresh version
first: parser
	cd src; nicec bossa.modules
	mkdir -p classes
	cd classes; jar xf ../external/java-getopt-1.0.7.jar gnu
	ln -sf nicec bin/nicer
	cd src;\
	${JAVAC} bossa/modules/Package.java nice/tools/runJar.java gnu/bytecode/dump.java ../stdlib/nice/*/*.java

recompile:
	cd src; ${JAVAC} bossa/modules/Package.java
#	${JAVAC} bossa/*/*.java nice/**/*.java mlsub/*/*.java ../stdlib/nice/lang/*.java

recompileGNU:
	cd src; ${JAVAC} gnu/*/*.java gnu/*/*/*.java

recompileSTDLIB:
	${JAVAC} ${TOP}/stdlib/nice/lang/*.java

compiler:
	${TOP}/src/nice/tools/compiler/setBuildDate ${TOP}/src/nice/tools/compiler
	cd src; nicec -e -Rs nice.tools.compiler

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
	cd src; ${JAVADOC} -windowtitle "Nice" -doctitle "The Nice compiler source" \
	-overview ${TOP}/src/overview.html \
	-d ${TOP}/doc -private -author -version \
	nice.tools.code \
	mlsub.typing mlsub.typing.lowlevel mlsub.compilation \
	bossa.syntax bossa.util bossa.link \
	gnu.bytecode gnu.expr gnu.math gnu.mapping gnu.kawa.reflect

tags:
	jtags

jar:
	mkdir -p lib/nice
	cp src/nice/tools/compiler.jar lib/nice/Nice.jar
	jar umf src/mainClass lib/nice/Nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	cd stdlib; jar uf ../lib/nice/Nice.jar nice/*/*.nice*

dist: jar
	tar cf lib/nice/Nice.tar bin/nicer bin/nicec lib/nice/Nice.jar lib/emacs/nice-mode.el

# CVS

status:
	cvs status 2>&1 | egrep "File:|Examining" | grep -v "Up-to-date"
