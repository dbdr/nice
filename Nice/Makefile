
# Target directory for 'make install'
PREFIX= /usr/local
BINDIR= ${PREFIX}/bin
DOCDIR= ${PREFIX}/share/doc/nice
MANDIR= ${PREFIX}/share/man/man1
LISPDIR=${PREFIX}/share/emacs/site-lisp/nice

SHELL = /bin/zsh
TOP=${PWD}

javac = javac
#javac = jikes

JAVAC_FLAGS = -O -g

JAVAC= ${javac} -classpath "${TOP}/classes" -sourcepath "${TOP}/src" -d "${TOP}/classes" $(JAVAC_FLAGS)
JAVACC= CLASSPATH="${TOP}/external/JavaCC.zip:${CLASSPATH}" java COM.sun.labs.javacc.Main
JAVADOC=javadoc

NICEC_ARGS= -d "${TOP}/classes" --exclude-runtime --classpath="${TOP}/classes"
NICEC=nicec ${NICEC_ARGS} --sourcepath="${TOP}/stdlib:${TOP}/src" 
NICEC1=${TOP}/bin/nicec ${NICEC_ARGS} --sourcepath="${TOP}/stdlib.new:${TOP}/src.new" 

# Get the version from the debian changelog.
VERSION = ${shell expr "`head -1 debian/changelog`" : '.*(\(.*\))'}
DEB = nice_${VERSION}_all.deb
TARFILE = Nice-${VERSION}.tar
ZIPFILE = Nice-${VERSION}-windows.zip
CVSBUILDDIR = /tmp/cvs-build


all: complete

stable:   clean bootstrap compiler1 ant testengine archive
complete: stable   compiler2 archive2
fixpoint: complete compiler3 archive3

compiler1: setDate
	${NICEC}  bossa.syntax
	${NICEC}  -R -a src/nice/tools/compiler.jar nice.tools.compiler

compiler2 compiler3: setDate
	if [ ! -r stdlib.new ]; then ln -s stdlib stdlib.new; fi
	if [ ! -r src.new ]; then ln -s src src.new; fi
	${NICEC1} -R bossa.syntax
	${NICEC1} -R -a src/nice/tools/compiler.jar nice.tools.compiler

archive archive2 archive3 share/java/nice.jar: src/nice/tools/compiler.jar
	mkdir -p share/java
	cp src/nice/tools/compiler.jar share/java/nice.jar
	jar umf src/mainClass share/java/nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	if [ -r classes/lang/package.nicei ]; then cd classes; jar uf ../share/java/nice.jar nice/*/package.nicei; fi



setDate:
	cd src/nice/tools/compiler; ./setBuildDate ${VERSION}

deb $(CVSBUILDDIR)/$(DEB):
	@echo Creating Debian package version ${VERSION}
	env CVSROOT=:pserver:anonymous@cvs.nice.sourceforge.net:/cvsroot/nice cvs-buildpackage -MNice -W${CVSBUILDDIR} -us -uc -rfakeroot -F

.PHONY: tar
tar ${TARFILE}: $(CVSBUILDDIR)/$(DEB)
	rm -rf ${TOP}/dist
	mkdir ${TOP}/dist
	cd $(CVSBUILDDIR)/nice-$(VERSION) && $(MAKE) install PREFIX=${TOP}/dist
	cd ${TOP}/dist && tar -cf ../${TARFILE} . --owner=0 --group=0
	rm -rf ${TOP}/dist

.PHONY: zip
zip ${ZIPFILE}: $(CVSBUILDDIR)/$(DEB)
	mkdir -p /tmp/Nice
	./bin/nicec --man | groff -mandoc -Thtml - > /tmp/Nice/nicec.html
	ln -sf ${TOP}/bin/nicec.bat ${TOP}/share/java/nice.jar ${TOP}/web/Readme.txt /tmp/Nice
	mkdir -p /tmp/Nice/Emacs
	ln -sf ${TOP}/lib/emacs/*.el /tmp/Nice/Emacs
	-rm ${ZIPFILE}
	cd /tmp && zip -r ${TOP}/${ZIPFILE} Nice

.PHONY: dist
dist: tar zip

install: share/java/nice.jar
	mkdir -p ${BINDIR} ${PREFIX}/share/java \
                 ${MANDIR} ${LISPDIR} ${DOCDIR}
	cp bin/nicec ${BINDIR}
	cp share/java/nice.jar ${PREFIX}/share/java/ 
	cp lib/emacs/nice-mode.el lib/emacs/nice-startup.el ${LISPDIR}
	./bin/nicec --man > ${MANDIR}/nicec.1
	-groff -mandoc -Thtml ${MANDIR}/nicec.1 > ${DOCDIR}/nicec.html

test:
	cd regtest; /usr/bin/time ./regtest

check:
	java -classpath "classes" nice.tools.testsuite.TestNice testsuite

gcj:
	gcj --main=nice.tools.compiler.package -o bin/nicec.bin -Dnice.systemJar="../share/java/nice.jar" -Dnice.runtime="../share/java/nice.jar" share/java/nice.jar

clean:
	rm -f src/nice/tools/compiler.jar
	-rm -rf dist classes share/java src/bossa/parser/{Parse*.java,Token*.java,ASCII_*.java}
	find "${TOP}" \( -name "*.class" -o -name "*.nicei" -o -name "*~" \) -exec rm {} \;


#****************************************************************
# CVS
#****************************************************************

status:
	cvs -z 9 status 2>&1 | awk -f bin/parse_cvs_status.awk

# Sourceforge

release: release-file web

release-file: ${TARFILE} ${ZIPFILE}
	(echo user anonymous bonniot@users.sf.net; \
         echo binary; echo cd incoming; \
         echo put ${TARFILE}; echo put ${ZIPFILE}; \
         echo lcd ${CVSBUILDDIR}; echo put ${DEB} )\
        | ftp -n upload.sourceforge.net
	echo "Redirect /Nice.tar http://prdownloads.sourceforge.net/nice/${TARFILE}" > web/.htaccess
	echo "Redirect /Nice.zip http://prdownloads.sourceforge.net/nice/${ZIPFILE}" >> web/.htaccess
	echo "Redirect /nice.deb http://prdownloads.sourceforge.net/nice/${DEB}" >> web/.htaccess

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
	@${javac} -classpath "/usr/share/java/ant.jar:./external/ant.jar:./classes" -d classes src/nice/tools/ant/Nicec.java ||\
	echo "Compilation of the Ant task definition failed.\nAnt should be in the classpath, or linked to by ./external/ant.jar"

testengine:
	@echo "Building the testsuite engine..."
	@${JAVAC} src/nice/tools/testsuite/TestNice.java ||\
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

