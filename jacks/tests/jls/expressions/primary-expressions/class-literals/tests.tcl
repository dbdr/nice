tcltest::test 15.8.2-type-1 { The class literal may be for any type } {
    empty_main T1582t1 {
        Class c = Object.class;
    }
} PASS

tcltest::test 15.8.2-type-2 { The class literal may be for any type } {
    empty_main T1582t2 {
        Class c = T1582t2.class;
    }
} PASS

tcltest::test 15.8.2-type-3 { The class literal may be for any type } {
    empty_main T1582t3 {
        Class c = Runnable.class;
    }
} PASS

tcltest::test 15.8.2-type-4 { The class literal may be for any type } {
    empty_main T1582t4 {
        Class c = void.class;
    }
} PASS

tcltest::test 15.8.2-type-5 { The class literal may be for any type } {
    empty_main T1582t5 {
        Class c = int.class;
    }
} PASS

tcltest::test 15.8.2-type-6 { The class literal may be for any type } {
    empty_main T1582t6 {
        Class c = int[].class;
    }
} PASS

tcltest::test 15.8.2-type-7 { The class literal may be for any type } {
    empty_main T1582t7 {
        Class c = Object[].class;
    }
} PASS

tcltest::test 15.8.2-type-8 { The class literal may be for any type } {
    empty_main T1582t8 {
        Class c = Runnable[][][][][][].class;
    }
} PASS

tcltest::test 15.8.2-primary-1 { The class literal serves as a primary } {
    empty_main T1582p1 {
        Object.class.getName();
    }
} PASS

tcltest::test 15.8.2-primary-2 { The class literal serves as a primary } {
    empty_main T1582p2 {
	try {
	    Object.class.forName("java.lang.Object");
	} catch (Exception e) {
	}
    }
} PASS

tcltest::test 15.8.2-syntax-1 { The class literal does not work on
        expressions } {
    empty_class T1582s1 {
        Class c = this.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-2 { The class literal does not work on
        expressions } {
    empty_class T1582s2 {
        Class c = (Object).class;
    }
} FAIL

tcltest::test 15.8.2-syntax-3 { The class literal does not work on
        expressions } {
    empty_class T1582s3 {
        Class c = 1.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-4 { The class literal does not work on
        expressions } {
    empty_class T1582s4 {
        Class c = "".class;
    }
} FAIL

tcltest::test 15.8.2-syntax-5 { The class literal does not work on
        expressions } {
    empty_class T1582s5 {
        int foo;
        Class c = foo.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-6 { The class literal does not work on
        expressions } {
    empty_class T1582s6 {
        int foo;
        Class c = null.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-7 { The type in the class literal is not
        obscured } {
    empty_class T1582s7 {
        int T1582s7; // obscure the class name from normal expressions
        Class c = T1582s7.class;
    }
} PASS

tcltest::test 15.8.2-synthetic-1 { Since the compiler must generate code to
        compile the class literal, this tests that the synthetic class$() method
        does not cause problems - jikes bug 2924 } {
    empty_class T1582synth1b {
	static final Object B_CONST = T1582synth1b.class;
    }
    compile [saveas T1582synth1c.java {
class T1582synth1c extends T1582synth1b {
    static final Object C_CONST = T1582synth1c.class;
}
    }]
    empty_class T1582synth1a {
	Object one = T1582synth1b.B_CONST;
	Object two = T1582synth1c.C_CONST;
    }
} PASS

tcltest::test 15.8.2-synthetic-2 { The compiler should synthesize byte.class
        and friends as Byte.TYPE and friends, rather than using Class.forName
        in the class$() method - jikes bug 3053 } {
    compile [saveas T1582synth2.java {
class T1582synth2 {
    Class c = byte.class;
}
    }] [saveas java/lang/Byte.java {
package java.lang;
public final class Byte extends Number implements Comparable
{
    public static final byte MIN_VALUE = -128;
    public static final byte MAX_VALUE = 127;
    // This may be recursive, but it works for the purpose of the test
    public static final Class TYPE = byte.class;
    public Byte(byte value) {}
    public Byte(String s) {}
    public static native String toString(byte b);
    public static native byte parseByte(String s);
    public static native byte parseByte(String s, int radix);
    public static native Byte valueOf(String s, int radix);
    public static native Byte valueOf(String s);
    public static native Byte decode(String s);
    public native byte byteValue();
    public native short shortValue();
    public native int intValue();
    public native long longValue();
    public native float floatValue();
    public native double doubleValue();
    public native String toString();
    public native int hashCode();
    public native boolean equals(Object obj);
    public native int compareTo(Byte b);
    public native int compareTo(Object o);
}
    }] [saveas java/dummy.java "package java; class dummy{}"]
} PASS

tcltest::test 15.8.2-synthetic-3 { Test synthesis of class literal variables
        across inner classes without enclosing instance } {
    empty_main T1582s3 {
        class Local {
            Local(Object o) {}
            void m() {
                Class c = Boolean.class;
            }
        }
        new Local(new Object() {
            Class c = Byte.class;
        });
    }
} PASS
