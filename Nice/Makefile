
# Target directory for 'make install'
PREFIX= /usr/local
BINDIR= ${PREFIX}/bin
DOCDIR= ${PREFIX}/share/doc/nice
MANDIR= ${PREFIX}/share/man/man1
LISPDIR=${PREFIX}/share/emacs/site-lisp/nice

# Get the version from the debian changelog.
VERSION = ${shell expr "`head -1 debian/changelog`" : '.*(\(.*\))'}

SHELL = /bin/zsh
TOP=${PWD}

javac = javac
#javac = jikes

JAVAC_FLAGS = -O -g

JAVAC= ${javac} -classpath "${TOP}/classes:${TOP}/classes.old" -sourcepath "${TOP}/src" -d "${TOP}/classes" $(JAVAC_FLAGS)
JAVACC= CLASSPATH="${TOP}/external/JavaCC.zip:${CLASSPATH}" java COM.sun.labs.javacc.Main
JAVADOC=javadoc

NICEC_ARGS= --exclude-runtime 
NICEC=nicec ${NICEC_ARGS} -d "${TOP}/classes.old" --sourcepath="${TOP}/stdlib.old:${TOP}/stdlib:${TOP}/src.old:${TOP}/src" --classpath="${TOP}/classes"
NICEC1=${TOP}/bin/nicec ${NICEC_ARGS} -d "${TOP}/classes" --sourcepath="${TOP}/stdlib:${TOP}/src" 



all: src/nice/tools/compiler.jar compiler2 archive2

stable:   clean bootstrap compiler1 ant testengine archiveOld
complete: stable   compiler2 archive2
fixpoint: complete compiler3 archive3
world:    fixpoint check test

src/nice/tools/compiler.jar:
	$(MAKE) complete

compiler1: setDate
	${NICEC}  bossa.syntax
	${NICEC}  -R -a src/nice/tools/compiler.jar nice.tools.compiler

compiler2 compiler3: setDate
	${NICEC1} -R bossa.syntax
	${NICEC1} -R -a src/nice/tools/compiler.jar nice.tools.compiler

archiveOld: src/nice/tools/compiler.jar
	mkdir -p share/java
	cp src/nice/tools/compiler.jar share/java/nice.jar
	jar umf src/mainClass share/java/nice.jar -C classes java -C classes nice -C classes mlsub -C classes bossa -C classes gnu -C classes.old bossa -C classes.old mlsub -C classes.old nice
	if [ -r classes/lang/package.nicei ]; then cd classes; jar uf ../share/java/nice.jar nice/*/package.nicei; fi

archive2 archive3 share/java/nice.jar: src/nice/tools/compiler.jar
	mkdir -p share/java
	cp src/nice/tools/compiler.jar share/java/nice.jar
	jar umf src/mainClass share/java/nice.jar -C classes java -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	if [ -r classes/lang/package.nicei ]; then cd classes; jar uf ../share/java/nice.jar nice/*/package.nicei; fi


install: share/java/nice.jar
	mkdir -p ${BINDIR} ${PREFIX}/share/java \
                 ${MANDIR} ${LISPDIR} ${DOCDIR}
	cp bin/nicec ${BINDIR}
	cp share/java/nice.jar ${PREFIX}/share/java/ 
	cp lib/emacs/nice-mode.el lib/emacs/nice-startup.el ${LISPDIR}
	./bin/nicec --man > ${MANDIR}/nicec.1
	-groff -mandoc -Thtml ${MANDIR}/nicec.1 > ${DOCDIR}/nicec.html


setDate:
	cd src/nice/tools/compiler; ./setBuildDate ${VERSION}

test:
	cd regtest; /usr/bin/time ./regtest

check:
	/usr/bin/time java -classpath "classes" nice.tools.testsuite.TestNice testsuite

GCJ=gcj
GCJJAR = /tmp/Nice-gcj.jar
gcj:
	cp src/nice/tools/compiler.jar $(GCJJAR)
	jar umf src/mainClass $(GCJJAR) -C classes nice/doc -C classes nice/getopt -C classes nice/lang -C classes nice/tools/ast -C classes nice/tools/code -C classes nice/tools/compiler -C classes nice/tools/util -C classes mlsub -C classes bossa -C classes gnu
	$(GCJ) -c -o /tmp/getopt.o --resource gnu/getopt/MessagesBundle.properties classes/gnu/getopt/MessagesBundle.properties
	$(GCJ) -g --main=nice.tools.compiler.fun -o bin/nicec.bin $(GCJJAR) /tmp/getopt.o

clean:
	rm -f src/nice/tools/compiler.jar
	-rm -rf classes share/java src/bossa/parser/{Parse*.java,Token*.java,ASCII_*.java}
	find "${TOP}" \( -name "*.class" -o -name "*.nicei" -o -name "*~" \) -exec rm {} \;


#****************************************************************
# CVS
#****************************************************************

status:
	cvs -z 9 status 2>&1 | awk -f bin/parse_cvs_status.awk

.PHONY: dist
dist:
	$(MAKE) -C distrib send

# Sourceforge

.PHONY: web
web:
	$(MAKE) -C web send

#****************************************************************
#    Bootstrap
#****************************************************************

parser: src/bossa/parser/Parser.java
src/bossa/parser/Parser.java: src/bossa/parser/Parser.jj
	cd src/bossa/parser; ${JAVACC} Parser.jj

bootstrap: parser
	cd src/gnu/lists && ./withCollections
	ln -sf nicec bin/nicer
	mkdir -p classes
	cd classes; jar xf ../external/java-getopt.jar gnu
	cd classes; jar xf ../external/gj.jar java
	-cd src/bossa/syntax && mv -f dispatch.java.bootstrap dispatch.java
	${JAVAC} \
		stdlib/nice/lang/Native.java stdlib/nice/lang/inline/*.java \
		src/mlsub/compilation/Module.java \
		src/bossa/modules/Package.java src/bossa/util/*.java \
		src/gnu/expr/*.java src/gnu/mapping/*.java \
		src/nice/tools/code/*.java src/mlsub/typing/*.java \
		src/mlsub/typing/lowlevel/*.java src/bossa/syntax/dispatch.java
	${NICEC} -R bossa.modules
	${NICEC} -r nice.tools.ast
	cd src/bossa/syntax && \
	${JAVAC} *.java && \
	mv dispatch.java dispatch.java.bootstrap
	rm classes/bossa/syntax/dispatch.class
	${NICEC} -r bossa.syntax
	cd src;\
	${JAVAC} bossa/modules/Package.java nice/tools/runJar.java ../stdlib/nice/lang/rawArray.java gnu/bytecode/dump.java

ant:
	@echo "Building the Ant task definition..."
	@${javac} -classpath "/usr/share/java/ant.jar:./external/ant.jar:./classes:./classes.old" -d classes src/nice/tools/ant/Nicec.java ||\
	echo "Compilation of the Ant task definition failed.\nAnt should be in the classpath, or linked to by ./external/ant.jar"

testengine:
	@echo "Building the testsuite engine..."
	@${JAVAC} src/nice/tools/testsuite/*.java ||\
	echo "Compilation of the testsuite engine failed."


#****************************************************************
#    Recompilations
#****************************************************************

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


#****************************************************************
#    Documentation
#****************************************************************

jdoc:
	mkdir -p doc
	cd src; ${JAVADOC} -windowtitle "Nice" -doctitle "The Nice compiler source" \
	-overview "${TOP}"/src/overview.html \
	-d "${TOP}"/doc -private -author -version \
	nice.tools.code \
	mlsub.typing mlsub.typing.lowlevel mlsub.compilation \
	bossa.syntax bossa.util bossa.link bossa.modules \
	gnu.bytecode gnu.expr gnu.math gnu.mapping

tags:
	jtags

