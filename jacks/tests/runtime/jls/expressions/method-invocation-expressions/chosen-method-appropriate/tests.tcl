tcltest::test 15.12.3-runtime-mode-1 { The invocation mode of a non-static,
        non-private method is virtual } {runtime} {
    saveas p1/T15123rm1_1.java {
package p1;
public class T15123rm1_1 {
    protected void foo() { // this is overridden in T15123rm1_3
        System.out.print("1");
    }
}
    }
    saveas p2/T15123rm1_2.java {
package p2;
import p1.*;
public class T15123rm1_2 extends T15123rm1_1 {
    protected void call() {
        foo(); // must be virtual call
        System.out.print(' ');
        new Object() {
            { foo(); } // must be virtual call of enclosing instance
        };
    }
}
    }
    saveas p3/T15123rm1_3.java {
package p3;
import p2.*;
public class T15123rm1_3 extends T15123rm1_2 {
    public static void main(String[] args) {
        new T15123rm1_3().call();
    }
    protected void foo() { // will be called virtually
        System.out.print("3");
    }
}
    }
    compile_and_run -classpath . p1/T15123rm1_1.java p2/T15123rm1_2.java p3/T15123rm1_3.java
} {3 3}

tcltest::test 15.12.3-runtime-mode-2 { The invocation mode of a static method
        is static } {runtime} {
    compile_and_run [saveas T15123rm2.java {
class T15123rm2 {
    static class One {
	private static void m() {
	    System.out.print(1);
	}
    }
    static class Two extends One {
	static void m() {
	    System.out.print(2);
	}
    }
    public static void main(String[] args) {
	One o = new Two();
	o.m(); // invokes One.m(), even though it is an instance of Two
    }
}
    }]
} 1

tcltest::test 15.12.3-runtime-mode-3 { The invocation mode of a private method
        is private } {runtime} {
    compile_and_run [saveas T15123rm3.java {
class T15123rm3 {
    static class One {
	private void m() {
	    System.out.print(1);
	}
    }
    static class Two extends One {
	void m() {
	    System.out.print(2);
	}
    }
    public static void main(String[] args) {
	One o = new Two();
	o.m(); // invokes One.m(), even though this is an instance of Two
    }
}
    }]
} 1

tcltest::test 15.12.3-runtime-mode-4 { The invocation mode of an instance
        method called with super is super } {runtime} {
    compile_and_run [saveas p1/T15123rm4a.java {
package p1;
public class T15123rm4a {
    protected void m() {
	System.out.print("a ");
    }
}
    }] [saveas T15123rm4c.java {
class T15123rm4b extends p1.T15123rm4a {
    public void foo() {
	m();
	super.m();
	new Object() {
	    {
		m();
		T15123rm4b.super.m();
	    }
	};
    }
    protected void m() {
	System.out.print("b ");
    }
}
class T15123rm4c extends T15123rm4b {
    public void m() {
	System.out.print("c ");
    }
    public static void main(String[] args) {
	new T15123rm4d();
    }
}
class T15123rm4d extends T15123rm4c {
    public void foo() {}
    T15123rm4d() {
	new Object() {
	    {
		T15123rm4d.super.foo();
	    }
	};
    }
}	
    }]
} {c a c a }

tcltest::test 15.12.3-runtime-mode-5 { The invocation mode of a method
        called on an interface is interface } {runtime} {
    compile_and_run [saveas T15123rm5.java {
class T15123rm5 {
    interface I {
	void m();
    }
    static abstract class One implements I {}
    static class Two extends One {
	public void m() {
	    System.out.print(1);
	}
    }
    public static void main(String[] args) {
	One o = new Two();
	I i = o;
	o.m(); // virtual
	System.out.print(' ');
	i.m(); // interface
    }
}
    }]
} {1 1}

tcltest::test 15.12.3-runtime-mode-6 { The invocation mode of a non-static,
        non-private method is virtual } {runtime} {
    compile_and_run [saveas p1/T15123rm6a.java {
package p1;
public class T15123rm6a {
    protected void m() {
	System.out.print(1);
    }
}
    }] [saveas p1/T15123rm6c.java {
package p1;
public class T15123rm6c extends p2.T15123rm6b {}
    }] [saveas p2/T15123rm6b.java {
package p2;
public class T15123rm6b extends p1.T15123rm6a {
    public static void main(String[] args) {
	final T15123rm6d d = new T15123rm6d();
	final p1.T15123rm6c c = d;
	c.m();
	System.out.print(' ');
	d.m();
	System.out.print(' ');
	new Object() {
	    {
		c.m();
		System.out.print(' ');
		d.m();
	    }
	};
    }
}
class T15123rm6d extends p1.T15123rm6c {
    protected void m() {
	System.out.print(4);
    }
}
    }]
} {4 4 4 4}

