SHELL = /bin/zsh
TOP=${PWD}
NICEJAR=/usr/local/lib/nice/Nice.jar

# Compile with stable version
JC=javac -classpath "${TOP}/classes:${NICEJAR}" -sourcepath "${TOP}/src" -d "${TOP}/classes" -O -g
JAVAC=  javac -classpath "${TOP}/classes:${TOP}/src:${TOP}/stdlib" -sourcepath "${TOP}/src" -d "${TOP}/classes" -O -g
JAVACC= CLASSPATH="${TOP}/external/JavaCC.zip:${CLASSPATH}" java COM.sun.labs.javacc.Main
JAVADOC=javadoc #-linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

NICEC= nicec --classpath="${TOP}/classes:${NICEJAR}" -d "${TOP}/classes"
NICEC_DEV= CLASSPATH="${TOP}/src/nice/tools/compiler.jar:${TOP}/classes" java -Dnice.systemJar="${TOP}/stdlib" nice.tools.compiler.package -d "${TOP}/bootstrap.classes" --classpath="${TOP}/stdlib:${TOP}/classes" --sourcepath="${TOP}/stdlib:."

# run this first when you just checked out a fresh version
first: bootstrap compiler jar

parser: src/bossa/parser/Parser.java
src/bossa/parser/Parser.java: src/bossa/parser/Parser.jj
	cd src/bossa/parser; ${JAVACC} Parser.jj

setDate:
	cd src/nice/tools/compiler; ./setBuildDate

nice-pass: 
	cd src; ${NICEC} bossa.syntax

compiler: setDate nice-pass
	cd src; ${NICEC} -R -o nice/tools/compiler.jar nice.tools.compiler

compiler-bootstrap: setDate
	cd src; ${NICEC_DEV} -r bossa.syntax
	cd src; ${NICEC_DEV} -R nice.tools.compiler

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
	${JAVAC} "${TOP}"/stdlib/nice/lang/*.java

# Compile individual files with: F=file.java make compile
compile:
	${JAVAC} "${F}"

# Part of the standard library implemented in Java
# Since these are not required by the compiler source,
# they don't get recompiled automatically by dependence analysis
native:
	${JAVAC} stdlib/nice/*/*.java src/nice/tools/code/*.java

clean:
	rm -rf classes lib/nice src/bossa/parser/{Parse*.java,Token*.java,ASCII_*.java} bootstrap.classes
	find "${TOP}" \( -name "*.class" -o -name "*.nicei" -o -name "*~" \) -exec rm {} \;

scratch: clean first

## Documentation

jdoc:
	mkdir -p doc
	cd src; ${JAVADOC} -windowtitle "Nice" -doctitle "The Nice compiler source" \
	-overview "${TOP}"/src/overview.html \
	-d "${TOP}"/doc -private -author -version \
	nice.tools.code \
	mlsub.typing mlsub.typing.lowlevel mlsub.compilation \
	bossa.syntax bossa.util bossa.link bossa.modules \
	gnu.bytecode gnu.expr gnu.math gnu.mapping gnu.kawa.reflect

tags:
	jtags

stdlib.jar:
	${NICEC_DEV} -o lib/nice/stdlib.jar --sourcepath="${TOP}/stdlib" nice.getopt
# package.nicei files must be included since the standard library is used
# to lookup packages when compiling Nice programs
# the next two lines do just that
	cd bootstrap.classes; jar uf ../lib/nice/stdlib.jar nice/*/package.nicei

jar: stdlib.jar
	mkdir -p lib/nice
	cp src/nice/tools/compiler.jar lib/nice/Nice.jar
	jar umf src/mainClass lib/nice/Nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu


dist: jar
	# make sure that nicec is executable
	chown a+rx bin/nicec
	tar cf Nice-`date +"%Y.%m.%d"`.tar bin/nicer bin/nicec lib/nice/Nice.jar lib/nice/stdlib.jar lib/emacs/nice-mode.el


#****************************************************************
# CVS
#****************************************************************

status:
	cvs -z 9 status 2>&1 | awk -f bin/parse_cvs_status.awk

# Sourceforge

sourceforge:
	scp web/*.html web/*.css bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

#****************************************************************
# Bootstrap
#****************************************************************

bootstrap: parser
	cd src/gnu/lists && ./withCollections
	ln -sf nicec bin/nicer
	mkdir -p classes
	cd classes; jar xf ../external/java-getopt.jar gnu
	-cd src/bossa/syntax && mv -f dispatch.java.bootstrap dispatch.java
	${JAVAC} \
		stdlib/nice/lang/Native.java stdlib/nice/lang/inline/*.java \
		src/mlsub/compilation/Module.java \
		src/bossa/modules/Package.java src/bossa/util/*.java \
		src/gnu/expr/*.java src/gnu/mapping/*.java \
		src/nice/tools/code/*.java src/mlsub/typing/*.java src/mlsub/typing/lowlevel/*.java \
		src/bossa/syntax/dispatch.java
	cd src; ${NICEC} bossa.modules
	cd src; ${NICEC} nice.tools.ast
	cd src/bossa/syntax && \
	${JAVAC} *.java && \
	mv dispatch.java dispatch.java.bootstrap
	rm classes/bossa/syntax/dispatch.class
	cd src; ${NICEC} bossa.syntax
	cd src;\
	${JC} bossa/modules/Package.java nice/tools/runJar.java ../stdlib/nice/lang/rawArray.java gnu/bytecode/dump.java
