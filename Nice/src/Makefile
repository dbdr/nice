
TOP=${HOME}/Bossa/src/bossa

JAVAC=javac -sourcepath ${HOME}/Bossa/src -g
JAVACC=java -classpath ${HOME}/lib/java/JavaCC/JavaCC.zip COM.sun.labs.javacc.Main

SYNTAX_SRC:=$(wildcard ${TOP}/syntax/*.java)
SYNTAX_CLASS=${SYNTAX_SRC:.java=.class}

UTIL_SRC:=$(wildcard ${TOP}/util/*.java)
UTIL_CLASS=${UTIL_SRC:.java=.class}

TEST_SRC:=$(wildcard ${TOP}/test/*.java)
TEST_CLASS=${TEST_SRC:.java=.class}

ALL_CLASS=${UTIL_CLASS} ${SYNTAX_CLASS} ${TEST_CLASS}

all: util parser syntax test

util: ${UTIL_CLASS}

syntax: ${SYNTAX_CLASS}

test: ${TEST_SRC} parser syntax
	${JAVAC} ${TOP}/test/*.java

parser: ${TOP}/parser/Parser.java

${TOP}/parser/Parser.java: ${TOP}/parser/Parser.jj
	cd ${TOP}/parser; ${JAVACC} Parser.jj

clean:
	rm -f ${ALL_CLASS}
	rm -f ${TOP}/parser/*.java ${TOP}/parser/*.class

# RULES
.SUFFIXES: .java .class

.java.class:
	${JAVAC} $<

## Documentation

doc:
	javadoc -windowtitle "Bossa" -doctitle "The Bossa interpreter source" -d ${TOP}/../../doc -private bossa.syntax bossa.util
