
# Target directory for 'make install'
PREFIX= /usr/local
BINDIR= ${PREFIX}/bin
DOCDIR= ${PREFIX}/share/doc/nice
MANDIR= ${PREFIX}/share/man/man1
LISPDIR=${PREFIX}/share/emacs/site-lisp/nice

# Get the version from the debian changelog.
VERSION = ${shell expr "`head -1 debian/changelog`" : '.*(\(.*\))'}
TAG     = ${shell if [ `grep '^ --' debian/changelog |head -1|wc -c` -le 10 ]; then echo prerelease; fi }

# ANT classpath; we will use NICE_ANTJAR or ANT_HOME (in that order) 
# if present, otherwise try good default
ANT_CLASSPATH= ${shell if [ "z${NICE_ANTJAR}" != "z" ] ; \
	then echo "${NICE_ANTJAR}"; \
	elif [ "z${ANT_HOME}" != "z" ] ; \
	then echo "${ANT_HOME}/lib/ant.jar"; \
	else if [ "z${CLASSPATH}" != "z" ]; \
	        then echo "/usr/share/java/ant.jar:./external/ant.jar:${CLASSPATH}"; \
		else echo "/usr/share/java/ant.jar:./external/ant.jar:"; \
	    	fi \
	fi }

SHELL = /bin/sh
TOP=${PWD}

java = java
javac = javac

JAVAC_FLAGS = -O -g

JAVAC_GENERIC= ${javac} -classpath "${TOP}/classes:${TOP}/classes.old" -sourcepath "${TOP}/src:${TOP}/stdlib" $(JAVAC_FLAGS)
JAVAC= ${JAVAC_GENERIC} -d "${TOP}/classes"
JAVADOC=javadoc

NICEC_ARGS= --exclude-runtime 
NICEC=CLASSPATH="${TOP}/classes" JAVA="${java} -Dnice.inlined=classes-inline" ./bin/nicec.bootstrap ${NICEC_ARGS} -d "${TOP}/classes.old" --sourcepath="${TOP}/stdlib.old:${TOP}/stdlib:${TOP}/src.old:${TOP}/src" --classpath="${TOP}/classes"
NICEC1=JAVA="${java}" ${TOP}/bin/nicec ${NICEC_ARGS} -d "${TOP}/classes" --sourcepath="${TOP}/stdlib:${TOP}/src" 



all: src/nice/tools/compiler/console.jar compiler2 archive2

stable:   clean bootstrap compiler1 ant testengine archiveOld
complete: stable   compiler2 archive2
fixpoint: complete compiler3 archive3
world:    fixpoint check test

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
	jar umf src/nice/tools/compiler/Manifest share/java/nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu -C classes.old bossa -C classes.old mlsub -C classes.old nice
	if [ -r classes/lang/package.nicei ]; then cd classes; jar uf ../share/java/nice.jar nice/*/package.nicei; fi

archive archive2 archive3 share/java/nice.jar: src/nice/tools/compiler/console.jar
	mkdir -p share/java
	cp src/nice/tools/compiler/console.jar share/java/nice.jar
	jar umf src/nice/tools/compiler/Manifest share/java/nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
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
	cd src; ${JAVAC} nice/tools/compiler/JavaVersion.java
	${java} -classpath classes nice.tools.compiler.JavaVersion > src/nice/tools/compiler/javaVersion
	@echo "We are using compiler `cat src/nice/tools/compiler/javaVersion` (at least our java version let us assume that)"
	cd src/nice/tools/compiler; ./setBuildDate "${VERSION}" "`cat javaVersion`" "${TAG}"
	@rm src/nice/tools/compiler/javaVersion
	@rm classes/nice/tools/compiler/JavaVersion.class

test:
	cd regtest; JAVA="${java}" /usr/bin/time ./regtest

check: 
	if [ `${java} -h 2>&1 | grep -- -enableassertions | wc -l` = 0 ]; then\
	  /usr/bin/time ${java} -Dassertions=true -classpath "classes" nice.tools.testsuite.TestNice testsuite; \
	else \
	  /usr/bin/time ${java} -enableassertions -classpath "classes" nice.tools.testsuite.TestNice testsuite; \
	fi

check_lib:
	if [ `${java} -h 2>&1 | grep -- -enableassertions | wc -l` = 0 ]; then\
	  /usr/bin/time ${java} -Dassertions=true -classpath "classes" nice.tools.testsuite.TestNice testsuite/lib; \
	else \
	  /usr/bin/time ${java} -enableassertions -classpath "classes" nice.tools.testsuite.TestNice testsuite/lib; \
	fi

.PHONY: coverage
coverage: share/java/nice.jar
	$(MAKE) -C coverage all

GCJ=gcj
GCJTEMP = /tmp/Nice-gcj.jar
gcj:
	cp src/nice/tools/compiler/console.jar $(GCJTEMP)
	jar umf src/mainClass $(GCJTEMP) -C classes nice/doc -C classes nice/getopt -C classes nice/lang -C classes nice/tools/ast -C classes nice/tools/code -C classes nice/tools/compiler -C classes nice/tools/util -C classes mlsub -C classes bossa -C classes gnu
	$(GCJ) --main=nice.tools.compiler.fun -o bin/nicec.bin $(GCJTEMP)

gcj-testsuite:
	cp src/nice/tools/compiler/console.jar $(GCJTEMP)
	jar umf src/mainClass $(GCJTEMP) -C classes nice/doc -C classes nice/getopt -C classes nice/lang -C classes nice/tools/ast -C classes nice/tools/code -C classes nice/tools/compiler -C classes nice/tools/util -C classes nice/tools/testsuite -C classes mlsub -C classes bossa -C classes gnu
	$(GCJ) -g --main=nice.tools.testsuite.TestNice -o bin/testsuite.bin $(GCJTEMP)

clean:
	rm -f src/nice/tools/compiler/console.jar
	-rm -rf classes classes-inline share/java src/bossa/parser/{Parse*.java,Token*.java,*CharStream.java}
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
	cd src/bossa/parser; CLASSPATH=`TOP=${TOP} ${TOP}/external/javacc` \
		${java} javacc Parser.jj

bootstrap: parser
	cd src/gnu/lists && ./withCollections
	mkdir -p classes classes-inline
	-cd src/bossa/syntax && mv -f dispatch.java.bootstrap dispatch.java
	${JAVAC} \
		stdlib/nice/lang/Native.java \
		src/mlsub/compilation/Module.java \
		src/bossa/modules/Package.java src/bossa/util/*.java \
		src/gnu/expr/*.java src/gnu/mapping/*.java \
		src/nice/tools/code/*.java src/mlsub/typing/*.java \
		src/mlsub/typing/lowlevel/*.java src/bossa/syntax/dispatch.java
	$(JAVAC_GENERIC) -d classes-inline stdlib/nice/lang/inline/*.java
	cp -a classes-inline/* classes
	${NICEC} -R bossa.modules
	${NICEC} -r nice.tools.ast
	cd src/bossa/syntax && \
	${JAVAC} *.java && \
	mv dispatch.java dispatch.java.bootstrap
	rm classes/bossa/syntax/dispatch.class
	${NICEC} -r bossa.syntax
	cd src;\
	${JAVAC} bossa/modules/Package.java ../stdlib/nice/lang/rawArray.java gnu/bytecode/dump.java

ant:
	@echo "Building the Ant task definition..."
	@${javac} -sourcepath src -classpath "${ANT_CLASSPATH}:./classes:./classes.old" -d classes src/nice/tools/ant/Nicec.java ||\
	echo -e "Compilation of the Ant task definition failed.\n NICE_ANTJAR or ANT_HOME should be set, Ant should be in the CLASSPATH or at least linked to by ./external/ant.jar"

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
