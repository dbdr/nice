
TOP=${HOME}/Nice
TOPCLASSES=${TOP}/classes

JAVAC=javac -classpath ${TOPCLASSES}:${TOP}/src:${TOP}/stdlib -sourcepath ${TOP}/src -O -g
JAVACC=java -classpath ${HOME}/lib/java/JavaCC/JavaCC.zip COM.sun.labs.javacc.Main
JAVADOC=javadoc -linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

recompile:
	cd src;\
	${JAVAC} -d ${TOP}/classes bossa/syntax/*.java bossa/modules/*.java bossa/parser/*.java nice/tools/*.java bossa/test/Front.java mlsub/*/*.java ../stdlib/*.java

recompileGNU:
	cd src;\
	${JAVAC} -d ${TOP}/classes gnu/*/*.java gnu/*/*/*.java kawa/*/*.java

parser:
	cd src/bossa/parser; ${JAVACC} Parser.jj

clean:
	rm -rf ${TOPCLASSES}/*

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
	cd stdlib; jar uf ../lib/nice/Nice.jar nice/*/*.nice*; cd ..
	cd classes; jar uf ../lib/nice/Nice.jar *.class; cd ..

dist: jar
	tar cf lib/nice/Nice.tar bin/nicer bin/nicec lib/nice/Nice.jar lib/emacs/nice-mode.el
