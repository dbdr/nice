all: coreJava.nice ok.out

coreJava.nice: coreJava.nice.template coreJava.code
	sed -e /BODY/"r coreJava.code" -e /BODY/d coreJava.nice.template \
	> coreJava.nice

coreJava.java: coreJava.java.template coreJava.code
	sed -e /BODY/"r coreJava.code" -e /BODY/d coreJava.java.template \
	> coreJava.java

ok.out: coreJava.java
	CLASSPATH=.:${CLASSPATH}; export CLASSPATH; \
	javac -encoding latin1 coreJava.java && java coreJava > ok.out
