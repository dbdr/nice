tcltest::test 8.4.6.3-modifier-1 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m1 {
	static class One {
	    public void pub() {}
	    protected void pro() {}
	    void pack() {}
	    public static void spub() {}
	    protected static void spro() {}
	    static void spack() {}
	}
	static class Two extends One {
	    public void pub() {}
	    public void pro() {}
	    public void pack() {}
	    public static void spub() {}
	    public static void spro() {}
	    public static void spack() {}
	}
	static class Three extends One {
	    protected static void spro() {}
	    protected static void spack() {}
	}
	static class Four extends One {
	    static void spack() {}
	}
    }
} PASS

tcltest::test 8.4.6.3-modifier-2 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m2 {
	class One {
	    public void pub() {}
	}
	class Two extends One {
	    protected void pub() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-3 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m3 {
	class One {
	    public void pub() {}
	}
	class Two extends One {
	    void pub() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-4 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m4 {
	class One {
	    public void pub() {}
	}
	class Two extends One {
	    private void pub() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-5 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m5 {
	class One {
	    protected void pro() {}
	}
	class Two extends One {
	    void pro() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-6 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m6 {
	class One {
	    protected void pro() {}
	}
	class Two extends One {
	    private void pro() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-7 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m7 {
	class One {
	    void pack() {}
	}
	class Two extends One {
	    private void pack() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-8 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m8 {
	static class One {
	    public static void pub() {}
	}
	static class Two extends One {
	    protected static void pub() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-9 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m9 {
	static class One {
	    public static void pub() {}
	}
	static class Two extends One {
	    static void pub() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-10 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m10 {
	static class One {
	    public static void pub() {}
	}
	static class Two extends One {
	    private static void pub() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-11 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m11 {
	static class One {
	    protected static void pro() {}
	}
	static class Two extends One {
	    static void pro() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-12 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m12 {
	static class One {
	    protected static void pro() {}
	}
	static class Two extends One {
	    private static void pro() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-13 { Access of overriding or hiding methods
        must equal or exceed prior access } {
    empty_class T8463m13 {
	static class One {
	    static void pack() {}
	}
	static class Two extends One {
	    private static void pack() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-modifier-14 { strictfp, abstract, native, and
        synchronized modifiers may change in overridden or hidden versions } {
    empty_class T8463m14 {
	abstract static class One {
	    strictfp void strict() {}
	    abstract void abs();
	    native void nat();
	    synchronized void synch() {}
	    static strictfp void sstrict() {}
	    static native void snat();
	    static synchronized void ssynch() {}
	}
	abstract static class Two extends One {
	    abstract void strict();
	    native void abs();
	    synchronized void nat() {}
	    strictfp void synch() {}
	    static native void sstrict();
	    static synchronized void snat() {}
	    static strictfp void ssynch() {}
	}
    }
} PASS

tcltest::test 8.4.6.3-private-1 { private methods are not overridden or
        hidden } {
    empty_class T8463p1 {
	static class One {
	    private final int m() { return 1; }
	}
	static class Two extends One {
	    private static void m() {}
	}
	static class Three extends Two {
	    Object m() { return null; }
	}
    }
} PASS

tcltest::test 8.4.6.3-default-1 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d1a.java {
package p1;
public class T8463d1a {
    void m() {}
}
class T8463d1c extends p2.T8463d1b {
    // conflicting return type, even though a.m() not inherited
    int m() { return 1; }
}
    }] [saveas p2/T8463d1b.java {
package p2;
public class T8463d1b extends p1.T8463d1a {}
    }]
} FAIL

tcltest::test 8.4.6.3-default-2 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d2a.java {
package p1;
public class T8463d2a {
    static void m() {}
}
class T8463d2c extends p2.T8463d2b {
    // conflicting return type, even though a.m() not inherited
    static int m() { return 1; }
}
    }] [saveas p2/T8463d2b.java {
package p2;
public class T8463d2b extends p1.T8463d2a {}
    }]
} FAIL

tcltest::test 8.4.6.3-default-3 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d3a.java {
package p1;
public class T8463d3a {
    void m() {}
}
class T8463d3c extends p2.T8463d3b {
    // conflicting throws clause, even though a.m() not inherited
    void m() throws Exception {}
}
    }] [saveas p2/T8463d3b.java {
package p2;
public class T8463d3b extends p1.T8463d3a {}
    }]
} FAIL

tcltest::test 8.4.6.3-default-4 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d4a.java {
package p1;
public class T8463d4a {
    static void m() {}
}
class T8463d4c extends p2.T8463d4b {
    // conflicting throws clause, even though a.m() not inherited
    static void m() throws Exception {}
}
    }] [saveas p2/T8463d4b.java {
package p2;
public class T8463d4b extends p1.T8463d4a {}
    }]
} FAIL

tcltest::test 8.4.6.3-default-5 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d5a.java {
package p1;
public class T8463d5a {
    final void m() {}
}
class T8463d5c extends p2.T8463d5b {
    // can't override final a.m, even though not inherited
    void m() {}
}
    }] [saveas p2/T8463d5b.java {
package p2;
public class T8463d5b extends p1.T8463d5a {}
    }]
} FAIL

tcltest::test 8.4.6.3-default-6 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d6a.java {
package p1;
public class T8463d6a {
    static final void m() {}
}
class T8463d6c extends p2.T8463d6b {
    // can't override final a.m, even though not inherited
    static void m() {}
}
    }] [saveas p2/T8463d6b.java {
package p2;
public class T8463d6b extends p1.T8463d6a {}
    }]
} FAIL

tcltest::test 8.4.6.3-default-7 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d7a.java {
package p1;
public class T8463d7a {
    void m() {}
}
class T8463d7c extends p2.T8463d7b {
    // conflicting return type, even though a.m() not inherited
    public int m() { return 1; }
}
    }] [saveas p2/T8463d7b.java {
package p2;
public class T8463d7b extends p1.T8463d7a {
    public int m() { return 1; }
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-8 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d8a.java {
package p1;
public class T8463d8a {
    static void m() {}
}
class T8463d8c extends p2.T8463d8b {
    // conflicting return type, even though a.m() not inherited
    public static int m() { return 1; }
}
    }] [saveas p2/T8463d8b.java {
package p2;
public class T8463d8b extends p1.T8463d8a {
    public static int m() { return 1; }
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-9 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d9a.java {
package p1;
public class T8463d9a {
    void m() {}
}
class T8463d9c extends p2.T8463d9b {
    // conflicting throws clause, even though a.m() not inherited
    public void m() throws Exception {}
}
    }] [saveas p2/T8463d9b.java {
package p2;
public class T8463d9b extends p1.T8463d9a {
    public void m() throws Exception {}
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-10 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d10a.java {
package p1;
public class T8463d10a {
    static void m() {}
}
class T8463d10c extends p2.T8463d10b {
    // conflicting throws clause, even though a.m() not inherited
    public static void m() throws Exception {}
}
    }] [saveas p2/T8463d10b.java {
package p2;
public class T8463d10b extends p1.T8463d10a {
    public static void m() throws Exception {}
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-11 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d11a.java {
package p1;
public class T8463d11a {
    final void m() {}
}
class T8463d11c extends p2.T8463d11b {
    // can't override final a.m, even though not inherited
    public void m() {}
}
    }] [saveas p2/T8463d11b.java {
package p2;
public class T8463d11b extends p1.T8463d11a {
    public void m() {}
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-12 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d12a.java {
package p1;
public class T8463d12a {
    static final void m() {}
}
class T8463d12c extends p2.T8463d12b {
    // can't override final a.m, even though not inherited
    public static void m() {}
}
    }] [saveas p2/T8463d12b.java {
package p2;
public class T8463d12b extends p1.T8463d12a {
    public static void m() {}
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-13 { It is possible to avoid inheriting a
        package scope method, but it must still be overridden correctly } {
    compile [saveas p1/T8463d13a.java {
package p1;
public class T8463d13a {
    public class E1 extends Exception {}
    public class E2 extends Exception {}
    void m() throws E1 {}
}
class T8463d13c extends p2.T8463d13b {
    // conflicting throws clause, even though a.m() not inherited
    public void m() throws E2 {}
}
    }] [saveas p2/T8463d13b.java {
package p2;
public class T8463d13b extends p1.T8463d13a {
    public void m() throws E2 {}
}
    }]
} FAIL

tcltest::test 8.4.6.3-default-14 { It is possible to avoid inheriting a
        package scope method, but it must still be hidden correctly } {
    compile [saveas p1/T8463d14a.java {
package p1;
public class T8463d14a {
    public class E1 extends Exception {}
    public class E2 extends Exception {}
    static void m() throws E1 {}
}
class T8463d14c extends p2.T8463d14b {
    // conflicting throws clause, even though a.m() not inherited
    public static void m() throws E2 {}
}
    }] [saveas p2/T8463d14b.java {
package p2;
public class T8463d14b extends p1.T8463d14a {
    public static void m() throws E2 {}
}
    }]
} FAIL

tcltest::test 8.4.6.3-signature-1 { return types must not conflict } {
    empty_class T8463s1 {
	class One {
	    int m() { return 1; }
	}
	class Two extends One {
	    void m() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-signature-2 { return types must not conflict } {
    empty_class T8463s2 {
	static class One {
	    static int m() { return 1; }
	}
	static class Two extends One {
	    static void m() {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-signature-3 { throws clauses must be compatible } {
    empty_class T8463s3 {
	class One {
	    void m() {}
	}
	class Two extends One {
	    void m() throws Exception {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-signature-4 { throws clauses must be compatible } {
    empty_class T8463s4 {
	static class One {
	    static void m() {}
	}
	static class Two extends One {
	    static void m() throws Exception {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-signature-5 { throws clauses must be compatible } {
    empty_class T8463s5 {
	class E1 extends Exception {}
	class E2 extends Exception {}
	class E3 extends Exception {}
	interface I1 {
	    void m() throws E1, E2;
	}
	interface I2 {
	    void m() throws E2, E3;
	}
	class C implements I1, I2 {
	    public void m() throws E2 {}
	}
	class D extends C {
	    public void m() {}
	}
    }
} PASS

tcltest::test 8.4.6.3-signature-6 { throws clauses must be compatible } {
    empty_class T8463s6 {
	class E1 extends Exception {}
	class E2 extends Exception {}
	class E3 extends Exception {}
	interface I1 {
	    void m() throws E1, E2;
	}
	interface I2 {
	    void m() throws E2, E3;
	}
	class C implements I1, I2 {
	    public void m() throws E1 {}
	}
    }
} FAIL

tcltest::test 8.4.6.3-signature-7 { throws clauses must be compatible } {
    empty_class T8463s7 {
	class E1 extends Exception {}
	class E2 extends Exception {}
	class E3 extends Exception {}
	interface I1 {
	    void m() throws E1, E2;
	}
	interface I2 {
	    void m() throws E2, E3;
	}
	class C implements I1, I2 {
	    public void m() throws E3 {}
	}
    }
} FAIL

