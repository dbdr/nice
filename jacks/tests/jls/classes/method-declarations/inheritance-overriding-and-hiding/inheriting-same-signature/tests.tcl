tcltest::test 8.4.6.4-multiple-1 { multiple inheritance is not a problem, if
        no ambiguous reference is made } {
    empty_class T8464m1 {
	interface I1 {
	    void m();
	}
	interface I2 {
	    void m();
	}
	abstract class C implements I1, I2 {
	}
    }
} PASS

tcltest::test 8.4.6.4-multiple-2 { multiple paths still gives single
        inheritance } {
    empty_class T8464m2 {
	interface Super {
	    void m();
	}
	interface I1 extends Super {}
	interface I2 extends Super {}
	abstract class C implements I1, I2 {
	    {
		m();
	    }
	}
    }
} PASS

tcltest::test 8.4.6.4-multiple-3 { It is possible to have access to multiple
        methods, when one is not inherited across package boundaries. The
        inherited method need not match the hidden one. } {
    compile [saveas p1/T8464m3a.java {
package p1;
public class T8464m3a {
    int m() { return 1; }
}
class T8464m3c extends p2.T8464m3b {
    // inherited static b.m() does not clash with accessible instance a.m()
    int i = m();
}
    }] [saveas p2/T8464m3b.java {
package p2;
public class T8464m3b extends p1.T8464m3a {
    protected static int m() { return 1; }
}
    }]
} PASS

tcltest::test 8.4.6.4-multiple-4 { It is possible to have access to multiple
        methods, when one is not inherited across package boundaries. The
        inherited method need not match the hidden one. } {
    compile [saveas p1/T8464m4a.java {
package p1;
public class T8464m4a {
    void m() {}
}
class T8464m4c extends p2.T8464m4b {
    // inherited return type of b.m() does not clash with accessible a.m()
    int i = m();
}
    }] [saveas p2/T8464m4b.java {
package p2;
public class T8464m4b extends p1.T8464m4a {
    protected int m() { return 1; }
}
    }]
} PASS

tcltest::test 8.4.6.4-multiple-5 { It is possible to have access to multiple
        methods, when one is not inherited across package boundaries. The
        inherited method need not match the hidden one. } {
    compile [saveas p1/T8464m5a.java {
package p1;
public class T8464m5a {
    public class E extends Exception {}
    void m() {}
}
class T8464m5c extends p2.T8464m5b {
    // inherited throws clause of b.m() does not clash with accessible a.m()
    {
	try {
	    m();
	} catch (E e) {
	}
    }
}
    }] [saveas p2/T8464m5b.java {
package p2;
public class T8464m5b extends p1.T8464m5a {
    protected void m() throws E {}
}
    }]
} PASS

tcltest::test 8.4.6.4-multiple-6 { It is possible to have access to multiple
        methods, when one is not inherited across package boundaries. The
        inherited method need not match the hidden one. } {
    compile [saveas p1/T8464m6a.java {
package p1;
public class T8464m6a {
    int m() throws Exception { return 1; }
}
class T8464m6c extends p2.T8464m6b {
    // inherited of b.m() does not clash with throws clause of accessible a.m()
    int i = m();
}
    }] [saveas p2/T8464m6b.java {
package p2;
public class T8464m6b extends p1.T8464m6a {
    protected int m() { return 1; }
}
    }]
} PASS

tcltest::test 8.4.6.4-multiple-7 { It is possible to have access to multiple
        methods, when one is not inherited across package boundaries. The
        inherited method need not match the hidden one. } {
    compile [saveas p1/T8464m7a.java {
package p1;
public class T8464m7a {
    final int m() { return 1; }
}
class T8464m7c extends p2.T8464m7b {
    // inherited b.m() does not violate final accessible a.m()
    int i = m();
}
    }] [saveas p2/T8464m7b.java {
package p2;
public class T8464m7b extends p1.T8464m7a {
    protected int m() { return 1; }
}
    }]
} PASS

tcltest::test 8.4.6.4-multiple-8 { It is possible to have access to multiple
        methods, when one is not inherited across package boundaries. The
        inherited method need not match the hidden one. } {
    compile [saveas p1/T8464m8a.java {
package p1;
public class T8464m8a {
    final int m() { return 1; }
}
abstract class T8464m8c extends p2.T8464m8b {
    // inherited abstract b.m() can be implemented outside package p1.
    // Therefore, even though accessible a.m() is final, it need not provide
    // the implementation of b.m() since it is not inherited
    // Note, however, that no concrete subclass of b can live in p1.
    int i = m();
}
    }] [saveas p2/T8464m8b.java {
package p2;
public abstract class T8464m8b extends p1.T8464m8a {
    protected abstract int m();
}
    }]
} PASS

tcltest::test 8.4.6.4-abstract-1 { in multiple inheritance, a non-abstract
        method must implement the inherited abstract ones } {
    empty_class T8464a1 {
	interface I {
	    void m();
	}
	class C {
	    void m() {}
	}
	class D extends C implements I {} // C.m() not public
    }
} FAIL

tcltest::test 8.4.6.4-abstract-2 { in multiple inheritance, a non-abstract
        method must implement the inherited abstract ones } {
    empty_class T8464a2 {
	interface I {
	    void m();
	}
	static class C {
	    public static void m() {}
	}
	static class D extends C implements I {} // C.m() static
    }
} FAIL

tcltest::test 8.4.6.4-abstract-3 { in multiple inheritance, a non-abstract
        method must implement the inherited abstract ones } {
    empty_class T8464a3 {
	interface I {
	    void m();
	}
	class C {
	    public int m() { return 1; }
	}
	class D extends C implements I {} // C.m() has wrong return type
    }
} FAIL

tcltest::test 8.4.6.4-abstract-4 { in multiple inheritance, a non-abstract
        method must implement the inherited abstract ones } {
    empty_class T8464a4 {
	interface I {
	    void m();
	}
	class C {
	    public void m() throws Exception {}
	}
	class D extends C implements I {} // C.m() has incompatible throws
    }
} FAIL

tcltest::test 8.4.6.4-abstract-5 { in multiple inheritance, a non-abstract
        method must implement the inherited abstract ones } {
    empty_class T8464a5 {
	interface I {
	    void m() throws Exception;
	}
	class C {
	    public void m() {}
	}
	class D extends C implements I {}
    }
} PASS

tcltest::test 8.4.6.4-abstract-6 { in multiple inheritance with no
        implementation, all abstract methods are inherited, regardless of
        throws clauses } {
    empty_class T8464a6 {
	class E1 extends Exception {}
	class E2 extends Exception {}
	interface I1 {
	    void m() throws E1;
	}
	interface I2 {
	    void m() throws E2;
	}
	abstract class C implements I1, I2 {}
    }
} PASS

tcltest::test 8.4.6.4-abstract-7 { in multiple inheritance with no
        implementation, all abstract methods are inherited } {
    empty_class T8464a7 {
	abstract class C {
	    abstract void m(); // note non-public accessibility
	}
	interface I {
	    void m();
	}
	abstract class D extends C implements I {}
    }
} PASS
