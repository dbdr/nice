
TOP=${HOME}/Bossa
TOPSRC=${TOP}/src/bossa
TOPCLASSES=${TOP}/classes

JAVAC=javac -sourcepath ${HOME}/Bossa/src -g
JAVACC=java -classpath ${HOME}/lib/java/JavaCC/JavaCC.zip COM.sun.labs.javacc.Main
JAVADOC=javadoc -linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java121/docs/api

SYNTAX_SRC:=$(wildcard ${TOPSRC}/syntax/*.java)
SYNTAX_CLASS=${SYNTAX_SRC:.java=.class}

TYPING_SRC:=$(wildcard ${TOPSRC}/typing/*.java)
TYPING_CLASS=${TYPING_SRC:.java=.class}

UTIL_SRC:=$(wildcard ${TOPSRC}/util/*.java)
UTIL_CLASS=${UTIL_SRC:.java=.class}

TEST_SRC:=$(wildcard ${TOPSRC}/test/*.java)
TEST_CLASS=${TEST_SRC:.java=.class}

ALL_CLASS=${UTIL_CLASS} ${TYPING_CLASS} ${SYNTAX_CLASS} ${TEST_CLASS}

all: util parser syntax typing test

util: ${UTIL_CLASS}

syntax: ${SYNTAX_CLASS}

typing: ${TYPING_CLASS}

test: ${TEST_CLASS} parser syntax

parser: ${TOPSRC}/parser/Parser.java ${TOPSRC}/parser/Loader.class

${TOPSRC}/parser/Loader.class: ${TOPSRC}/parser/Loader.java
	${JAVAC} ${TOPSRC}/parser/Loader.java

${TOPSRC}/parser/Parser.java: ${TOPSRC}/parser/Parser.jj
	cd ${TOPSRC}/parser; ${JAVACC} Parser.jj

clean:
	rm -rf ${TOPCLASSES}/*

All: parser
	${JAVAC} bossa/syntax/*.java bossa/typing/*.java bossa/test/*.java

# RULES
.SUFFIXES: .java .class

.java.class:
	${JAVAC} $<

## Documentation

doc:
	${JAVADOC} -windowtitle "Bossa" -doctitle "The Bossa interpreter source" -d ${TOP}/doc -private bossa.syntax bossa.util bossa.typing bossa.engine

tags:
	cd ${TOP}
	jtags

jar:
	cd ${TOP}/classes;\
	jar cf ../Bossa.jar bossa/
