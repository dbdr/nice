# It is pointless to test 65535 local variables, as accessing them all would
# use more than 65534 bytes in a method (which is tested), and compilers are
# allowed to not define a variable that is never accessed. This is true even
# for 32767 double or long variables. Likewise, it is pointless to test that
# the operand stack is never greater than 65535 slots, as it would require that
# much bytecode to fill the stack.

# We do not test the restriction of 65535 direct superinterfaces. The class
# will first exceed the constant pool limitations, and the demands on the file
# system to support 65535 .class files is just too great to be practical for
# testing. True, a class can have more than this number of interfaces, if they
# are not all direct superinterfaces, but testing the validity of such a class
# proved to be pointless.

tcltest::test 4.10-jvms-1 { A class can have at most
        65535 entries in the constant pool } {jvm} {

    set buff "class T410jvms1 \{\n"

    set over [expr {65555 / 5}]
    for {set i 0} {$i < $over} {incr i} {
        append buff "\tString m${i}(int i) \{\n"
        append buff "\t\treturn \"m${i}-1\" + i + \"m${i}-2\"\n"
        append buff "\t\t    + i + \"m${i}-3\" + i + \"m${i}-4\"\n"
        append buff "\t\t    + i + \"m${i}-5\";\n"
        append buff "\t\}\n"
    }

    append buff "\}\n"

    compile [saveas T410jvms1.java $buff]
} FAIL

tcltest::test 4.10-jvms-2 { A single method can safely have no more than
        65534 byte codes, because of the limitations of exception handlers.
        However, the hard limit is 65535 bytes } {jvm} {

    set buff ""
    append buff "class T410jvms2 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tint i = 0;\n"

    set over 21900
    for {set i 0} {$i < $over} {incr i} {
        append buff "\t\ti++;\n"
    }

    append buff "\t\}\n"
    append buff "\}\n"

    compile [saveas T410jvms2.java $buff]
} FAIL

tcltest::test 4.10-jvms-3 { A single class can have at most 65535 member
        variables, although more than that can be inherited. Note that this
        test overflows the constant pool first } {jvm} {

    set buff ""
    append buff "class T410jvms3 \{\n"

    set over 65536
    for {set i 0} {$i < $over} {incr i} {
        append buff "\tint v${i};\n"
    }

    append buff "\}\n"

    compile [saveas T410jvms3.java $buff]
} FAIL

tcltest::test 4.10-jvms-4 { A single class can have at most 65535 member
        variables, although more than that can be inherited } {jvm} {

    set buff ""
    set interfaces 7
    set over 10000
    for {set j 0} {$j < $interfaces} {incr j} {
	append buff "interface T410jvms4_${j} \{\n"
	for {set i 0} {$i < $over} {incr i} {
	    append buff "\tint v${j}_${i} = 1;\n"
	}
	append buff "\}\n"
    }

    append buff "class T410jvms4 implements T410jvms4_0"
    for {set j 1} {$j < $interfaces} {incr j} {
        append buff ",\n\tT410jvms4_${j}"
    }

    append buff " \{\n\t\tint v = 1;\n\}\n"

    compile [saveas T410jvms4.java $buff]
} PASS

tcltest::test 4.10-jvms-5 { A single class can
        have at most 65535 methods, although more than that can be inherited.
        Note that this test overflows the constant pool first } {jvm} {

    set buff ""
    append buff "class T410jvms5 \{\n"

    set over 65536
    for {set i 0} {$i < $over} {incr i} {
        append buff "\tvoid m${i}() {}\n"
    }

    append buff "\}\n"

    compile [saveas T410jvms5.java $buff]
} FAIL

tcltest::test 4.10-jvms-6 { A single class can have at most 65535 member
        variables, although more than that can be inherited } {jvm} {

    set buff ""
    set interfaces 7
    set over 10000
    for {set j 0} {$j < $interfaces} {incr j} {
	append buff "interface T410jvms6_${j} \{\n"
	for {set i 0} {$i < $over} {incr i} {
	    append buff "\tint m${j}_${i}();\n"
	}
	append buff "\}\n"
    }

    append buff "abstract class T410jvms6 implements T410jvms6_0"
    for {set j 1} {$j < $interfaces} {incr j} {
        append buff ",\n\tT410jvms6_${j}"
    }

    append buff " \{\n\t\tvoid m() \{\}\n\}\n"

    compile [saveas T410jvms6.java $buff]
} PASS

tcltest::test 4.10-jvms-7 { A method can have
       at most 255 parameters } {jvm} {

    set buff ""
    append buff "class T410jvms7 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tfoo("

    set over 255
    for {set i 0} {$i < $over} {incr i} {
        append buff "${i}, "
    }
    append buff "255); \n"

    append buff "\t\}\n"

    append buff "\tstatic void foo("
    for {set i 0} {$i < $over} {incr i} {
        append buff "int p${i}, "
    }
    append buff "int p255)"
    append buff "\{\n"
    append buff "\t\}\n"

    append buff "\}\n"

    compile [saveas T410jvms7.java $buff]
} FAIL


tcltest::test 4.10-jvms-8 { A method can have at most 255 parameters. This
        limit is reduced to 127 doubles or longs in an instance method } {jvm} {

    set buff ""
    append buff "class T410jvms8 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tnew T410jvms8().foo("

    set over 127
    for {set i 0} {$i < $over} {incr i} {
        append buff "${i}, "
    }
    append buff "127); \n"

    append buff "\t\}\n"

    append buff "\tvoid foo("
    for {set i 0} {$i < $over} {incr i} {
        append buff "long p${i}, "
    }
    append buff "int p127)"
    append buff "\{\n"
    append buff "\t\}\n"

    append buff "\}\n"

    compile [saveas T410jvms8.java $buff]
} FAIL


tcltest::test 4.10-jvms-9 { A method can have at most 255 parameters. This
        limit is reduced to 127 doubles or longs in interface methods } {jvm} {

    set buff ""
    append buff "interface T410jvms9 \{\n"
    set over 127
    append buff "\tvoid foo("
    for {set i 0} {$i < $over} {incr i} {
        append buff "double p${i}, "
    }
    append buff "int p127);\n"
    append buff "\}\n"

    compile [saveas T410jvms9.java $buff]
} FAIL


tcltest::test 4.10-jvms-10 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms10 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tObject intArray = new int"

    set over 256
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff "{null};\n"
    append buff "\t\}\n"
    append buff "\}\n"

    compile [saveas T410jvms10.java $buff]
} FAIL


tcltest::test 4.10-jvms-11 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms11 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tObject o = null;\n"
    append buff "\t\tboolean b = o instanceof int"

    set over 256
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff ";\n\t\}\n"
    append buff "\}\n"

    compile [saveas T410jvms11.java $buff]
} FAIL


tcltest::test 4.10-jvms-12 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms12 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tClass c = int"

    set over 256
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff ".class;\n\t\}\n"
    append buff "\}\n"

    compile [saveas T410jvms12.java $buff]
} FAIL


tcltest::test 4.10-jvms-13 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms13 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tSystem.out.println(i);\n"
    append buff "\t\}"
    append buff "\tstatic int"

    set over 128
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff " i"
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }
    append buff ";\n\}\n"

    compile [saveas T410jvms13.java $buff]
} FAIL


tcltest::test 4.10-jvms-14 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms14 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tSystem.out.println(m());\n"
    append buff "\t\}"
    append buff "\tstatic int"

    set over 128
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff " m()"
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }
    append buff "\{ return null; \}\n\}\n"

    compile [saveas T410jvms14.java $buff]
} FAIL


tcltest::test 4.10-jvms-15 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms15 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tSystem.out.println(m(null));\n"
    append buff "\t\}"
    append buff "\tstatic int m(int"

    set over 128
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff " i"
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }
    append buff ")\{ return 0; \}\n\}\n"

    compile [saveas T410jvms15.java $buff]
} FAIL


tcltest::test 4.10-jvms-16 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms16 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tint"

    set over 128
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff " i"
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }
    append buff "; \}\n\}\n"

    compile [saveas T410jvms16.java $buff]
} FAIL


tcltest::test 4.10-jvms-17 { The number of dimensions
       in an array is limited to 255. } {jvm} {

    set buff ""
    append buff "class T410jvms17 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tObject o = null;\n"
    append buff "\t\to = (int"

    set over 256
    for {set i 0} {$i < $over} {incr i} {
        append buff "\[\]"
    }

    append buff ") null; \}\n\}\n"

    compile [saveas T410jvms17.java $buff]
} FAIL


tcltest::test 4.10-jvms-18 { Strings that occupy more than 65535 UTF8
        bytes do not fit in the constant pool } {jvm} {
    set buff ""
    append buff "class T410jvms18 \{\n"
    append buff "\tint a"

    set over 21845
    for {set i 0} {$i < $over} {incr i} {
        append buff "\\ufffb"
    }

    append buff " = 1;\n"
    append buff "\tpublic static void main(String\[\] args) \{\}\n\}\n"
	    
    compile [saveas T410jvms18.java $buff]
} FAIL

tcltest::test 4.10-jvms-19 { Strings that occupy more than 0xffff UTF8
        bytes cannot be constant, according to JVMS. } {jvm} {
    empty_class T410jvms19 {
	static final String s0001 = "a";
	static final String s0002 = s0001 + s0001;
	static final String s0004 = s0002 + s0002;
	static final String s0008 = s0004 + s0004;
	static final String s0010 = s0008 + s0008;
	static final String s0020 = s0010 + s0010;
	static final String s0040 = s0020 + s0020;
	static final String s0080 = s0040 + s0040;
	static final String s0100 = s0080 + s0080;
	static final String s0200 = s0100 + s0100;
	static final String s0400 = s0200 + s0200;
	static final String s0800 = s0400 + s0400;
	static final String s1000 = s0800 + s0800;
	static final String s2000 = s1000 + s1000;
	static final String s4000 = s2000 + s2000;
	static final String s8000 = s4000 + s4000;
	// sffff is still constant!
	static final String sffff = s8000 + s4000 + s2000 + s1000
                                  + s0800 + s0400 + s0200 + s0100
                                  + s0080 + s0040 + s0020 + s0010
                                  + s0008 + s0004 + s0002 + "b";

	static final String toobig = sffff + "c"; // can't be constant
	void m(int i) {
	    switch (i) {
		case 0:
		case (toobig == "a" ? 0 : 1): // using non-constant case label
	    }
	}
    }
} FAIL

tcltest::test 4.10-jvms-20 { Strings that occupy more than 65535 UTF8
        bytes do not fit in the constant pool } {jvm} {
    set buff ""
# 12 bytes
    set huge "abcdefghijkl"
# 48 bytes
    set huge "${huge}${huge}${huge}${huge}"
# 192 bytes
    set huge "${huge}${huge}${huge}${huge}"
# We have to be careful of filename length limits, so stop here
    set over 180

    for {set i 0} {$i < $over} {incr i} {
	append buff "\class ${huge}${i} \{\}\n"
    }
    append buff "class T410jvms20 \{\n"
    append buff "\tstatic void foo(${huge}0 p0"
    for {set i 1} {$i < $over} {incr i} {
	append buff ",\n\t\t ${huge}${i} p${i}"
	for {set j 0} {$j < $over} {incr j} {
	    append buff "\[\]"
	}
    }
    append buff ") \{\}\n"

    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tfoo(null"
    for {set i 1} {$i < $over} {incr i} {
	append buff ", null"
    }
    append buff ");\n\t\}\n\}\n"
	    
    compile [saveas T410jvms20.java $buff]
} FAIL

tcltest::test 4.10-jvms-21 { A single method, including <clinit>, can safely
        have no more than 65534 byte codes, because of the limitations of
        exception handlers. However, the hard limit is 65535 bytes } {jvm} {

    set buff ""
    append buff "class T410jvms21 \{\n"
    append buff "\tpublic static void main(String\[\] args) {}\n"
    append buff "\tstatic double\[\] d = \{\n"

    set over 30000
    for {set i 0} {$i < $over} {incr i} {
        append buff "1, "
    }
    append buff "\n\t\};\n\}\n"

    compile [saveas T410jvms21.java $buff]
} FAIL

tcltest::test 4.10-jvms-22 { A single method, including instance initializers
        (whether inlined into constructors or not), can safely have no more
        than 65534 byte codes, because of the limitations of exception
        handlers. However, the hard limit is 65535 bytes } {jvm} {

    set buff ""
    append buff "class T410jvms22 \{\n"
    append buff "\tpublic static void main(String\[\] args) {}\n"
    append buff "\tdouble\[\] d = \{\n"

    set over 30000
    for {set i 0} {$i < $over} {incr i} {
        append buff "1, "
    }
    append buff "\n\t\};\n\}\n"

    compile [saveas T410jvms22.java $buff]
} FAIL

tcltest::test 4.10-jvms-23 { A single method can safely have no more than
        65534 byte codes, because of the limitations of exception handlers.
        However, the hard limit is 65535 bytes } {jvm} {

    set buff ""
    append buff "class T410jvms23 \{\n"
    append buff "\tpublic static void main(String\[\] args) \{\n"
    append buff "\t\tif (args.length > 0) \{\n"
    append buff "\t\t\tdouble\[\] d = \{\n"

    set over 30000
    for {set i 0} {$i < $over} {incr i} {
        append buff "1, "
    }
    append buff "\n\t\t\t\};\n\t\t\}\n\t\}\n\}\n"

    compile [saveas T410jvms23.java $buff]
} FAIL

