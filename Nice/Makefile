
# Target directory for 'make install'
PREFIX= /usr/local
BINDIR= ${PREFIX}/bin
DOCDIR= ${PREFIX}/share/doc/nice
MANDIR= ${PREFIX}/share/man/man1
LISPDIR=${PREFIX}/share/emacs/site-lisp/nice

# Get the version from the NEWS file.
VERSION = ${shell expr "`head -1 NEWS`" : '.*(\(.*\))'}
TAG     = ${shell if [ `grep '^ --' NEWS |head -1|wc -c` -le 10 ]; then echo prerelease; fi }

# ANT classpath; we will use NICE_ANTJAR or ANT_HOME (in that order) 
# if present, otherwise try good default
ANT_CLASSPATH= ${shell if [ -n "${NICE_ANTJAR}" ] ; \
		then echo "${NICE_ANTJAR}"; \
	elif [ -n "${ANT_HOME}" ] ; \
		then echo "${ANT_HOME}/lib/ant.jar"; \
	else \
		echo "/usr/share/ant/lib/ant.jar:./external/ant.jar:${CLASSPATH}"; \
	fi }

SHELL := /bin/bash
PWD   := $(shell pwd)

java  = java
javac = javac
jar   = jar

JAVAC_FLAGS = -O -g

JAVAC_GENERIC= ${javac} -classpath "${PWD}/classes:${PWD}/classes.old" -sourcepath "${PWD}/src:${PWD}/stdlib" ${JAVAC_FLAGS}
JAVAC= ${JAVAC_GENERIC} -d "${PWD}/classes"
JAVADOC=javadoc

NICEC_ARGS= --exclude-runtime
NICEC=CLASSPATH=classes JAVA="${java} -Dnice.inlined=classes-inline" bin/nicec.bootstrap ${NICEC_ARGS} -d classes.old --sourcepath=stdlib.old:stdlib:src.old:src --classpath=classes
NICEC1=JAVA="${java}" bin/nicec ${NICEC_ARGS} -d classes --sourcepath=stdlib:src

MANIFEST = src/nice/tools/compiler/Manifest


all: src/nice/tools/compiler/console.jar compiler

compiler: setDate
	${NICEC1} bossa.syntax
	${NICEC1} -a src/nice/tools/compiler/console.jar nice.tools.compiler.console

stable:   clean bootstrap compiler1 testengine archiveOld
complete: stable   compiler2 tools libs archive2
fixpoint: complete compiler3 archive3
world:    fixpoint check test

tools: nicedoc unit ant publish nicetestengine

# The bootstrap at the end of the universe ;-)
universe:
	$(MAKE) complete
	cp share/java/nice.jar external/nice-bootstrap.jar
	$(MAKE) world

src/nice/tools/compiler/console.jar:
	$(MAKE) complete

compiler1: setDate
	${NICEC}  bossa.syntax
	${NICEC}  -R -a src/nice/tools/compiler/console.jar nice.tools.compiler.console

compiler2 compiler3: setDate
	${NICEC1} -R bossa.syntax
	${NICEC1} -R -a src/nice/tools/compiler/console.jar nice.tools.compiler.console

archiveOld: src/nice/tools/compiler/console.jar
	mkdir -p share/java
	cp src/nice/tools/compiler/console.jar share/java/nice.jar
	$(jar) umf $(MANIFEST) share/java/nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	$(jar) uf share/java/nice.jar -C classes.old bossa -C classes.old mlsub -C classes.old nice
	if [ -r classes/lang/package.nicei ]; then cd classes; $(jar) uf ../share/java/nice.jar nice/*/package.nicei; fi

archive archive2 archive3 share/java/nice.jar: src/nice/tools/compiler/console.jar
	mkdir -p share/java
	cp src/nice/tools/compiler/console.jar share/java/nice.jar
	$(jar) umf $(MANIFEST) share/java/nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	if [ -r classes/lang/package.nicei ]; then cd classes; $(jar) uf ../share/java/nice.jar nice/*/package.nicei; fi


install: share/java/nice.jar
	mkdir -p ${BINDIR} ${PREFIX}/share/java \
                 ${MANDIR} ${LISPDIR} ${DOCDIR}
	cp bin/nicec ${BINDIR}
	ln -sf nicec ${BINDIR}/niceunit
	cp share/java/nice.jar ${PREFIX}/share/java/ 
	cp lib/emacs/nice-mode.el lib/emacs/nice-startup.el ${LISPDIR}
	./bin/nicec --man > ${MANDIR}/nicec.1
	./bin/niceunit --man > ${MANDIR}/niceunit.1
	-groff -mandoc -Thtml ${MANDIR}/nicec.1 > ${DOCDIR}/nicec.html


setDate:
	${JAVAC} src/nice/tools/compiler/JavaVersion.java
	${java} -classpath classes nice.tools.compiler.JavaVersion > src/nice/tools/compiler/javaVersion
	@echo "We are using compiler `cat src/nice/tools/compiler/javaVersion` (at least our java version let us assume that)"
	cd src/nice/tools/compiler; ./setBuildDate "${VERSION}" "`cat javaVersion`" "${TAG}"
	@rm src/nice/tools/compiler/javaVersion
	@rm classes/nice/tools/compiler/JavaVersion.class

test:
	cd regtest; JAVA="${java}" /usr/bin/time ./regtest

check:
	/usr/bin/time ${java} -Xms64M -classpath "classes" nice.tools.testsuite.TestNice testsuite

check_lib:
	/usr/bin/time ${java} -Dassertions=true -classpath "classes" nice.tools.testsuite.TestNice testsuite/lib

.PHONY: coverage
coverage: share/java/nice.jar
	$(MAKE) -C coverage all

GCJ=gcj
GCJTEMP = /tmp/Nice-gcj.jar
gcj:
	cp src/nice/tools/compiler/console.jar $(GCJTEMP)
	$(jar) umf $(MANIFEST) $(GCJTEMP) -C classes nice/doc -C classes nice/getopt -C classes nice/lang -C classes nice/tools/ast -C classes nice/tools/code -C classes nice/tools/compiler -C classes nice/tools/util -C classes nice/tools/typing -C classes mlsub -C classes bossa -C classes gnu
	$(GCJ) --main=nice.tools.compiler.console.fun -o bin/nicec.bin $(GCJTEMP)

gcj-testsuite:
	cp src/nice/tools/compiler/console.jar $(GCJTEMP)
	$(jar) umf src/mainClass $(GCJTEMP) -C classes nice/doc -C classes nice/getopt -C classes nice/lang -C classes nice/tools/ast -C classes nice/tools/code -C classes nice/tools/compiler -C classes nice/tools/util -C classes nice/tools/testsuite -C classes nice/tools/typing -C classes mlsub -C classes bossa -C classes gnu
	$(GCJ) -g --main=nice.tools.testsuite.TestNice -o bin/testsuite.bin $(GCJTEMP)

clean:
	rm -f src/nice/tools/compiler/{console.jar,dateBuild.nice,Manifest}
	rm -f nicepublish.jar regtest/*.jar
	rm -rf classes classes-inline share/java src/bossa/parser/{Parse*.java,Token*.java,*CharStream.java}
	rm -f bin/{nicedoc,niceunit}
	if [ -r src/bossa/syntax/dispatch.java -a ! -r src/bossa/syntax/dispatch.java.bootstrap ]; then \
		mv src/bossa/syntax/dispatch.java src/bossa/syntax/dispatch.java.bootstrap; fi
	rm -f src/bossa/syntax/dispatch.java
	find . \( -name "*.class" -o -name "*.nicei" -o -name "*~" \) -exec rm {} \;


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

core-bootstrap:
	mkdir -p classes classes-inline
	ln -sf nicec bin/nicedoc; ln -sf nicec bin/niceunit
	-cd src/bossa/syntax && mv -f dispatch.java.bootstrap dispatch.java
	${JAVAC} src/nice/tools/util/System.java
	${NICEC} -R nice.tools.repository
	${NICEC} -r nice.tools.visibility
	${JAVAC} \
		src/bossa/syntax/dispatch.java \
		src/mlsub/typing/*.java \
		src/gnu/expr/*.java \
		stdlib/nice/lang/{Native,rawArray}.java \
		src/bossa/modules/{Package,CompilationListener}.java \
		src/nice/tools/{code/*.java,util/JDK.java}
	$(JAVAC_GENERIC) -d classes-inline stdlib/nice/lang/inline/*.java
	cp -R -p classes-inline/* classes
#	We put code used by inline methods inside classes-inline so that it is
#	used when running the inline methods, not the versions in the running
#	compiler, which might be outdated.
	mkdir -p classes-inline/nice/tools/code
	cp classes/nice/tools/code/*.class classes-inline/nice/tools/code
	${NICEC} -R bossa.modules
	${NICEC} -r nice.tools.ast
	cd src/bossa/syntax && \
	${JAVAC} *.java && \
	mv dispatch.java dispatch.java.bootstrap
	rm classes/bossa/syntax/dispatch.class
	${NICEC} -r bossa.syntax

src/bossa/parser/Parser.java: src/bossa/parser/Parser.jj
	cd src/bossa/parser; CLASSPATH=`TOP=${PWD} ${PWD}/external/javacc` \
		${java} javacc Parser.jj

bootstrap: core-bootstrap src/bossa/parser/Parser.java
	${JAVAC} src/bossa/parser/JavaccParser.java

ant:
	@echo "Building the Ant task definition..."
	@$(NICEC1) --classpath "${ANT_CLASSPATH}" nice.tools.ant &&\
	${javac} -sourcepath src -classpath "${ANT_CLASSPATH}:./classes:./classes.old" -d classes src/nice/tools/ant/*.java ||\
	echo -e "Compilation of the Ant task definition failed.\n NICE_ANTJAR or ANT_HOME should be set, Ant should be in the CLASSPATH or at least linked to by ./external/ant.jar"

nicedoc:
	@echo "Building nicedoc..."
	$(NICEC1) nice.tools.doc

unit:
	@echo "Building niceunit..."
	$(NICEC1) nice.tools.unit.console

publish:
	$(NICEC1) -a nicepublish.jar nice.tools.repository.publish

libs:
	@echo "Building libraries..."
	$(NICEC1) nice.functional
	$(NICEC1) nice.io

testengine:
	@echo "Building the testsuite engine..."
	@${JAVAC} src/nice/tools/testsuite/*.java ||\
	echo "Compilation of the testsuite engine failed."

nicetestengine:
	$(NICEC1) nice.tools.testsuite

visibility:
	$(NICEC1) -a visibility.jar nice.tools.visibility


#****************************************************************
#    Recompilations
#****************************************************************

# erase out-of-date class files and 'make dep' to recompile if javac 
# has no -[X]depend
dep:
	${JAVAC} src/bossa/modules/Package.java

parser: src/bossa/parser/Parser.java
	${JAVAC} src/bossa/parser/Parser.java

recompile: parser
#	${JAVAC} -Xdepend src/bossa/modules/Package.java
	cd src; ${JAVAC} bossa/**/*.java nice/**/*.java mlsub/**/*.java ../stdlib/nice/lang/*.java

recompileGNU:
	cd src; ${JAVAC} gnu/*/*.java gnu/*/*/*.java

recompileSTDLIB:
	${JAVAC} stdlib/nice/lang/*.java

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
	-overview src/overview.html \
	-d doc -private -author -version \
	nice.tools.code \
	mlsub.typing mlsub.typing.lowlevel mlsub.compilation \
	bossa.syntax bossa.util bossa.link bossa.modules \
	gnu.bytecode gnu.expr gnu.math gnu.mapping

tags:
	jtags
