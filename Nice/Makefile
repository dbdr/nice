SHELL = /bin/zsh
TOP=${PWD}

JAVAC=  javac -classpath ${TOP}/classes:${TOP}/src:${TOP}/stdlib -sourcepath ${TOP}/src -d ${TOP}/classes -O -g
JAVACC= CLASSPATH=${TOP}/external/JavaCC.zip:${CLASSPATH} java COM.sun.labs.javacc.Main
JAVADOC=javadoc #-linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

NICEC= NICEJAR=${TOP}/stdlib nicec --classpath="${TOP}/stdlib:${TOP}/classes:${TOP}/src"

# run this first when you just checked out a fresh version
first: parser bootstrap compiler jar

parser: src/bossa/parser/Parser.java
src/bossa/parser/Parser.java: src/bossa/parser/Parser.jj
	cd src/bossa/parser; ${JAVACC} Parser.jj

setDate:
	cd src/nice/tools/compiler; ./setBuildDate

compiler: setDate
	cd src; ${NICEC} -Rs nice.tools.compiler && cp -f nice/tools/compiler.jar nice/tools/.compiler.jar

compiler-bootstrap: setDate nice-pass
	cd src; CLASSPATH=${TOP}/src/nice/tools/.compiler.jar:${TOP}/src:${TOP}/classes java -Dnice.systemJar=${TOP}/stdlib nice.tools.compiler.package --classpath="${TOP}/stdlib:${TOP}/classes:${TOP}/src" -Rs nice.tools.compiler

test:
	cd regtest; /usr/bin/time ./regtest

# erase out-of-date class files and 'make dep' to recompile if javac 
# has no -[X]depend
dep:
	cd src; ${JAVAC} bossa/modules/Package.java

recompile: parser
#	${JAVAC} -Xdepend src/bossa/modules/Package.java
	cd src; ${JAVAC} bossa/**/*.java nice/**/*.java mlsub/**/*.java ../stdlib/nice/lang/*.java

recompileGNU:
	cd src; ${JAVAC} gnu/*/*.java gnu/*/*/*.java

recompileSTDLIB:
	${JAVAC} ${TOP}/stdlib/nice/lang/*.java

# Part of the standard library implemented in Java
# Since these are not required by the compiler source,
# they don't get recompiled automatically by dependence analysis
native:
	${JAVAC} stdlib/nice/*/*.java src/nice/tools/code/*.java

clean:
	rm -rf classes lib/nice src/bossa/parser/{Parse*.java,Token*.java,ASCII_*.java}
	find ${TOP} \( -name "*.class" -or -name "*.nicei" -or -name "*~" \) -exec rm {} \;

scratch: clean first

## Documentation

jdoc:
	mkdir -p doc
	cd src; ${JAVADOC} -windowtitle "Nice" -doctitle "The Nice compiler source" \
	-overview ${TOP}/src/overview.html \
	-d ${TOP}/doc -private -author -version \
	nice.tools.code \
	mlsub.typing mlsub.typing.lowlevel mlsub.compilation \
	bossa.syntax bossa.util bossa.link bossa.modules \
	gnu.bytecode gnu.expr gnu.math gnu.mapping gnu.kawa.reflect

tags:
	jtags

jar:
	mkdir -p lib/nice
	cp src/nice/tools/compiler.jar lib/nice/Nice.jar
	jar umf src/mainClass lib/nice/Nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	cd src; jar uf ../lib/nice/Nice.jar bossa/syntax/*.class nice/tools/ast/*.class
	cd stdlib; jar uf ../lib/nice/Nice.jar nice/*/*.nice*

dist: jar
	jar cfM lib/nice/Nice-`date +"%Y.%m.%d"`.jar bin/nicer bin/nicec lib/nice/Nice.jar lib/emacs/nice-mode.el

#****************************************************************
# CVS
#****************************************************************

status:
	cvs -z 9 status 2>&1 | awk -f bin/parse_cvs_status.awk

# src/gnu has two repositories:
# - a 'Nice' repository
# - a 'Kawa' repository

cvs-Kawa:
	find ${TOP}/src/gnu -type d -name 'CVS-Kawa' \
	-exec sh -c 'dir=`dirname "$$0"`; ln -sf $${dir}/CVS-Kawa $${dir}/CVS' {} \;

cvs-Nice:
	find ${TOP}/src/gnu -type d -name 'CVS-Kawa' \
	-exec sh -c 'dir=`dirname "$$0"`; mv -i $${dir}/CVS-Kawa $${dir}/CVS' {} \;

# Sourceforge

sourceforge:
	scp web/*.html web/*.css bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

#****************************************************************
# Bootstrap
#****************************************************************

bootstrap:
	ln -sf nicec bin/nicer
	mkdir -p classes
	cd classes; jar xf ../external/java-getopt-1.0.7.jar gnu
	cd src/bossa/syntax && mv -f dispatch.java.bootstrap dispatch.java;true
	${JAVAC} \
		stdlib/nice/lang/Native.java stdlib/nice/lang/inline/*.java \
		src/mlsub/compilation/Module.java \
		src/bossa/modules/Package.java src/bossa/util/*.java \
		src/gnu/expr/*.java src/gnu/mapping/*.java \
		src/nice/tools/code/*.java src/mlsub/typing/*.java src/mlsub/typing/lowlevel/*.java
	cd src; ${NICEC} bossa.modules
	cd src; ${NICEC} nice.tools.ast
	cd src/bossa/syntax && \
	${JAVAC} *.java && \
	mv dispatch.java dispatch.java.bootstrap
	rm classes/bossa/syntax/dispatch.class
	cd src; ${NICEC} bossa.syntax
	cd src;\
	${JAVAC} bossa/modules/Package.java nice/tools/runJar.java ../stdlib/nice/lang/rawArray.java gnu/bytecode/dump.java

nice-pass: 
	cd src; ${NICEC} bossa.syntax
