
TOP=${HOME}/Nice
TOPCLASSES=${TOP}/classes

JAVAC=javac -classpath ${TOPCLASSES}:${TOP}/src:${TOP}/lib -sourcepath ${TOP}/src -O -g
JAVACC=java -classpath ${HOME}/lib/java/JavaCC/JavaCC.zip COM.sun.labs.javacc.Main
JAVADOC=javadoc -linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

recompile:
	cd ${TOP}/src; ${JAVAC} -d ${TOP}/classes bossa/syntax/*.java bossa/modules/*.java bossa/parser/*.java mlsub/*/*.java ../lib/*.java

recompileGNU:
	cd ${TOP}/src; ${JAVAC} -d ${TOP}/classes gnu/*/*.java gnu/*/*/*.java kawa/*/*.java

parser:
	cd ${TOP}/src/bossa/parser; ${JAVACC} Parser.jj

clean:
	rm -rf ${TOPCLASSES}/*

scratch: clean parser recompile

## Documentation

jdoc:
	${JAVADOC} -windowtitle "Nice" -doctitle "The Nice compiler source" \
	-overview ${TOP}/src/overview.html \
	-d ${TOP}/doc -private mlsub.typing mlsub.compilation bossa bossa.syntax bossa.util bossa.link gnu.bytecode gnu.expr gnu.math gnu.mapping kawa.standard kawa.lang

tags:
	cd ${TOP}
	jtags

jar:
	cd ${TOP}; cp src/bossa/tools.jar bin/Nice.jar;\
	jar umf src/mainClass bin/Nice.jar -C classes bossa -C classes gnu -C classes kawa -C classes mlsub ;\
	cd lib; jar uf ../bin/Nice.jar nice/*/*.nice*; cd .. ;\
	cd classes; jar uf ../bin/Nice.jar *.class; cd ..

dist: jar
	cd ${HOME};\
	tar cf Nice/bin/Nice.tar bin/nicer bin/nicec ;\
	tar rhf Nice/bin/Nice.tar lib/nice/Nice.jar lib/nice/nice-mode.el
