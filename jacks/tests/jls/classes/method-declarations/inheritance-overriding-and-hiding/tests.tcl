tcltest::test 8.4.6-miranda-1 { Abstract classes no longer need Miranda
        methods (generated place-holders in classes which inherit an abstract
        method from an interface without overriding it) } {
    compile [saveas T846m1a.java {
interface T846m1a { void m(); }
}] [saveas T846m1b.java {
// If a compiler still inserts Miranda methods, then class b will have a
// bogus declaration "abstract void m();"
abstract class T846m1b implements T846m1a {}
}]
# Now, leave b in .class form only, and recompile a to lose m()
    delete T846m1b.java
    compile [saveas T846m1a.java {
interface T846m1a {}
}]
# The real test: if c inherits b.m(), the compiler goofed in compiling b
    compile -classpath . [saveas T846m1c.java {
abstract class T846m1c extends T846m1b {
    void foo() { m(); }
}
}]
} FAIL

tcltest::test 8.4.6-inheritance-1 { A class inherits non-private accessible
        methods that it does not hide by redeclaration } {
    empty_class T846i1 {
	private int m() { return 1; }
	static class Inner extends T846i1 {
	    int j = m(); // m not inherited, and enclosing m not available
	}
    }
} FAIL

tcltest::test 8.4.6-inheritance-2 { A class inherits non-private accessible
        methods that it does not hide by redeclaration } {
    empty_class T846i2 {
	private int m() { return 1; }
	class Inner extends T846i2 {
	    int j = this.m(); // m not inherited
	}
    }
} FAIL

tcltest::test 8.4.6-inheritance-3 { A class inherits non-private accessible
        methods that it does not hide by redeclaration. } {
    compile [saveas p1/T846i3a.java {
package p1;
public class T846i3a {
    int m() { return 1; }
    static class Inner extends p2.T846i3b {
	// package m not inherited from b, enclosing m unavailable
	int j = m();
    }
}
    }] [saveas p2/T846i3b.java {
package p2;
public class T846i3b extends p1.T846i3a {
    static int m() { return 1; } // does not override or hide T846i3a.m
}
    }]
} FAIL

tcltest::test 8.4.6-inheritance-4 { A class inherits non-private accessible
        methods that it does not hide by redeclaration. } {
    compile [saveas p1/T846i4a.java {
package p1;
public class T846i4a {
    int m() { return 1; }
    class Inner extends p2.T846i4b {
	// package m not inherited from b
	int j = this.m();
    }
}
    }] [saveas p2/T846i4b.java {
package p2;
public class T846i4b extends p1.T846i4a {
    static int m() { return 1; } // does not override or hide T846i4a.m
}
    }]
} FAIL

tcltest::test 8.4.6-inheritance-5 { A class inherits non-private accessible
        methods that it does not hide by redeclaration. } {
    compile [saveas p1/T846i5a.java {
package p1;
public class T846i5a {
    static int m() { return 1; }
    class Inner extends p2.T846i5b {
	// the superclass did not inherit m
	int j = this.m();
    }
}
    }] [saveas p2/T846i5b.java {
package p2;
public class T846i5b extends p1.T846i5a {
    int m() { return 1; } // does not override or hide T846i5a.m
}
    }]
} FAIL

tcltest::test 8.4.6-inheritance-6 { A class inherits non-private accessible
        methods that it does not hide by redeclaration } {
    empty_class T846i6 {
	class One {
	    int m() { return 1; }
	}
	class Two extends One {
	    {
		m();
		this.m();
	    }
	}
    }
} PASS
