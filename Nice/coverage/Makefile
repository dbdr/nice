TOP=${PWD}

all: report

instrumented.jar: ../share/java/nice.jar
	cp ../share/java/nice.jar /tmp/instrumented.jar
	ijar -file /tmp/instrumented.jar -cvg $(TOP) -branch 'nice.**' 'bossa.**' -none nice.tools.compiler.fun nice.getopt.dispatch 'nice.tools.compiler.fun\$$compile' nice.lang.fun 'nice.doc.fun\$$printMan'  nice.getopt.fun nice.tools.ast.fun 'nice.getopt.fun\$$parse' nice.tools.compiler.fun
	mv /tmp/instrumented.jar .

tests:
	rm -f *.cvg
	$(MAKE) testsuite regtest

testsuite: instrumented.jar
	java -Dassertions=true -classpath instrumented.jar nice.tools.testsuite.TestNice ../testsuite

regtest: instrumented.jar
	mkdir -p bin
	cp ../bin/nicec bin
	mkdir -p share/java
	ln -sf ../../instrumented.jar share/java/nice.jar
	cd ../regtest; CLASSPATH=$(TOP)/instrumented.jar NICE_TOP=$(TOP) ./regtest 

report: tests
	CLASSPATH=instrumented.jar gjreport -source ../src:../stdlib -html report -method
