
PREFIX= /usr/local
# Used by Debian to install in a private directory
DESTDIR= 
INSTALLDIR= ${DESTDIR}${PREFIX}
DOCDIR= ${INSTALLDIR}/share/doc/nice
MANDIR= ${INSTALLDIR}/share/man/man1
LISPDIR=${INSTALLDIR}/share/emacs/site-lisp/nice

SHELL = /bin/zsh
TOP=${PWD}
NICEJAR=/usr/share/java/nice.jar

# Compile with stable version
JC=javac -classpath "${TOP}/classes:${NICEJAR}" -sourcepath "${TOP}/src" -d "${TOP}/classes" -O -g
JAVAC=  javac -classpath "${TOP}/classes:${TOP}/src:${TOP}/stdlib" -sourcepath "${TOP}/src" -d "${TOP}/classes" -O -g
JAVACC= CLASSPATH="${TOP}/external/JavaCC.zip:${CLASSPATH}" java COM.sun.labs.javacc.Main
JAVADOC=javadoc #-linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java122/docs/api

NICEC_ARGS= -d "${TOP}/classes" --exclude-runtime 
NICEC=nicec ${NICEC_ARGS} --sourcepath="${TOP}/src"
NICEC1=${TOP}/bin/nicec ${NICEC_ARGS} --sourcepath="${TOP}/stdlib:${TOP}/src"

# Get the version from the debian changelog.
VERSION = ${shell dpkg-parsechangelog | sed -n 's/^Version: //p'}
DEB = nice_${VERSION}_all.deb
TARFILE = Nice-`date +"%Y.%m.%d"`.tar
CVSBUILDDIR = /tmp/cvs-build


all: clean bootstrap compiler1 share/java/nice.jar compiler2 archive

compiler1: setDate
	${NICEC}  bossa.syntax
	${NICEC}  -R -a src/nice/tools/compiler.jar nice.tools.compiler

compiler2: setDate
	${NICEC1} -R bossa.syntax
	${NICEC1} -R -a src/nice/tools/compiler.jar nice.tools.compiler

share/java/nice.jar archive: src/nice/tools/compiler.jar
	mkdir -p share/java
	cp src/nice/tools/compiler.jar share/java/nice.jar
	jar umf src/mainClass share/java/nice.jar -C classes nice -C classes mlsub -C classes bossa -C classes gnu
	if [ -r classes/lang/package.nicei ]; then cd classes; jar uf ../share/java/nice.jar nice/*/package.nicei; fi



setDate:
	cd src/nice/tools/compiler; ./setBuildDate

deb:
	@echo Creating Debian package version ${VERSION}
	env CVSROOT=:pserver:anonymous@cvs.nice.sourceforge.net:/cvsroot/nice cvs-buildpackage -MNice -W${CVSBUILDDIR} -us -uc -rfakeroot -THEAD

dist:
	rm -rf ${TOP}/dist
	mkdir ${TOP}/dist
	$(MAKE) install DESTDIR=${TOP}/dist PREFIX="" 
	cd ${TOP}/dist && tar -cf ../${TARFILE} . --owner=0 --group=0

install: share/java/nice.jar
	mkdir -p ${INSTALLDIR}/bin ${INSTALLDIR}/share/java \
                 ${MANDIR} ${LISPDIR} ${DOCDIR}
	cp bin/nicec ${INSTALLDIR}/bin/
	cp share/java/nice.jar ${INSTALLDIR}/share/java/ 
	cp lib/emacs/nice-mode.el ${LISPDIR}
	./bin/nicec --man > ${MANDIR}/nicec.1
	groff -mandoc -Thtml ${MANDIR}/nicec.1 > ${DOCDIR}/nicec.html

test:
	cd regtest; /usr/bin/time ./regtest

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

release-file:
	(echo user anonymous bonniot@users.sf.net; \
         echo binary; echo cd incoming; echo put ${TARFILE}; \
         echo lcd ${CVSBUILDDIR}; echo put ${DEB} )\
        | ftp -n upload.sourceforge.net
	echo "Redirect /Nice.tar http://prdownloads.sourceforge.net/nice/${TARFILE}" > web/.htaccess

.PHONY: web
web:
	scp web/*.html web/*.css web/.htaccess bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

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
		src/nice/tools/code/*.java src/mlsub/typing/*.java src/mlsub/typing/lowlevel/*.java \
		src/bossa/syntax/dispatch.java
	cd src; ${NICEC} -R bossa.modules
	cd src; ${NICEC} -r nice.tools.ast
	cd src/bossa/syntax && \
	${JAVAC} *.java && \
	mv dispatch.java dispatch.java.bootstrap
	rm classes/bossa/syntax/dispatch.class
	cd src; ${NICEC} -r bossa.syntax
	cd src;\
	${JC} bossa/modules/Package.java nice/tools/runJar.java ../stdlib/nice/lang/rawArray.java gnu/bytecode/dump.java


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

