all: B/BIj.class B/BAj.class

A/An.class A/In.class: A/A.nice
	nicec --sourcepath "${NICE_TOP}" regtest.java.A

B/BAj.class: A/An.class B/BAj.java
	javac -classpath "${NICE_TOP}" B/BAj.java

B/BIj.class: A/In.class B/BIj.java
	javac -classpath "${NICE_TOP}" B/BIj.java
