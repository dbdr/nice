
TOP=${HOME}/Bossa/src/bossa

JAVAC=javac -sourcepath ${HOME}/Bossa/src -g
JAVACC=java -classpath ${HOME}/lib/java/JavaCC/JavaCC.zip COM.sun.labs.javacc.Main
JAVADOC=javadoc -linkoffline file:///C%7C/Apps/jdk1.2.1/docs/api file:///usr/opt/java121/docs/api

SYNTAX_SRC:=$(wildcard ${TOP}/syntax/*.java)
SYNTAX_CLASS=${SYNTAX_SRC:.java=.class}

TYPING_SRC:=$(wildcard ${TOP}/typing/*.java)
TYPING_CLASS=${TYPING_SRC:.java=.class}

UTIL_SRC:=$(wildcard ${TOP}/util/*.java)
UTIL_CLASS=${UTIL_SRC:.java=.class}

TEST_SRC:=$(wildcard ${TOP}/test/*.java)
TEST_CLASS=${TEST_SRC:.java=.class}

ALL_CLASS=${UTIL_CLASS} ${TYPING_CLASS} ${SYNTAX_CLASS} ${TEST_CLASS}

all: util parser syntax typing test

util: ${UTIL_CLASS}

syntax: ${SYNTAX_CLASS}

typing: ${TYPING_CLASS}

test: ${TEST_CLASS} parser syntax

parser: ${TOP}/parser/Parser.java ${TOP}/parser/Loader.class

${TOP}/parser/Loader.class: ${TOP}/parser/Loader.java
	${JAVAC} ${TOP}/parser/Loader.java

${TOP}/parser/Parser.java: ${TOP}/parser/Parser.jj
	cd ${TOP}/parser; ${JAVACC} Parser.jj

clean:
	rm -f ${ALL_CLASS}
	rm -f ${TOP}/parser/*.java ${TOP}/parser/*.class

All: parser
	${JAVAC} bossa/syntax/*.java bossa/typing/*.java bossa/test/*.java

# RULES
.SUFFIXES: .java .class

.java.class:
	${JAVAC} $<

## Documentation

doc:
	${JAVADOC} -windowtitle "Bossa" -doctitle "The Bossa interpreter source" -d ${TOP}/../../doc -private bossa.syntax bossa.util bossa.typing bossa.engine
