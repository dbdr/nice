nicec = $(NICE_TOP)/bin/nicec -e --sourcepath "${NICE_TOP}"

all: B/BIj.class B/BAj.class J/J.class J/Other.class

A/An.class A/In.class: A/A.nice
	$(nicec) regtest.java.A

B/BAj.class: A/An.class B/BAj.java
	javac -classpath "${NICE_TOP}" B/BAj.java

B/BIj.class: A/In.class B/BIj.java
	javac -classpath "${NICE_TOP}" B/BIj.java

J/J.class: J/J.java
	javac -classpath "${NICE_TOP}" J/J.java

J/Other.class: J/Other.java
	javac -classpath "${NICE_TOP}" J/Other.java
