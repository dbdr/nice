tcltest::test 15.11.1-invalid-1 { Member types are not fields } {
    empty_class T15111i1 {
        static class A {
            static class B {
                static int i = 1;
            }
        }
        int j = new A().B.i;
    }
} FAIL

tcltest::test 15.11.1-ambiguous-1 { Accessing ambiguous fields is an error } {
    compile [saveas T15111a1a.java {
interface T15111a1a {
    int i = 1;
}
class T15111a1b {
    int i = 2;
}
class T15111a1c extends T15111a1b implements T15111a1a {
    static int j = new T15111a1c().i;
}
}]
} FAIL

tcltest::test 15.11.1-ambiguous-2 { There is no ambiguity if one field
        is not accessible } {
    compile [saveas p1/T15111a2a.java {
package p1;
public class T15111a2a {
    int i = 1;
}
}] [saveas T15111a2b.java {
interface T15111a2b {
    int i = 2;
}
class T15111a2c extends p1.T15111a2a implements T15111a2b {
    static int j = new T15111a2c().i; // only b.i is accessible
}
}]
} PASS

tcltest::test 15.11.1-ambiguous-3 { There is no ambiguity if one field
        is not accessible } {
    compile [saveas p1/T15111a3a.java {
package p1;
public class T15111a3a {
    protected int i = 1;
}
}] [saveas T15111a3b.java {
interface T15111a3b {
    int i = 2;
}
class T15111a3c extends p1.T15111a3a implements T15111a3b {}
class T15111a3d extends T15111a3c {
    // Even though d inherits two versions of i, the protected a.i
    // is only accessible if the qualifying expression is type d or lower
    static int j = new T15111a3c().i; // only b.i is accessible
}
}]
} PASS

tcltest::test 15.11.1-ambiguous-4 { There is no ambiguity if one field
        is not accessible } {
    compile [saveas p1/T15111a4a.java {
package p1;
public class T15111a4a {
    protected int i = 1;
}
}] [saveas T15111a4b.java {
interface T15111a4b {
    int i = 2;
}
class T15111a4c extends p1.T15111a4a implements T15111a4b {}
class T15111a4d extends T15111a4c {
    // Even though d inherits two versions of i, the protected a.i
    // is only accessible if the qualifying expression is type d or lower
    static int j = new T15111a4d().i; // both a.i and b.i are accessible
}
}]
} FAIL

tcltest::test 15.11.1-explicit-constructor-1 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T15111ec1 {
	int i;
	T15111ec1(int i) {}
	class Sub extends T15111ec1 {
	    Sub() {
		// using the enclosing i, not the inherited i, is legal
		super(T15111ec1.this.i);
	    }
	}
    }
} PASS

tcltest::test 15.11.1-explicit-constructor-2 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T15111ec2 {
	int i;
	class Inner {
	    Inner(int i) {}
	    Inner() {
		// explicit mention of the only version of i
		this(T15111ec2.this.i);
	    }
	}
    }
} PASS

tcltest::test 15.11.1-explicit-constructor-3 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T15111ec3 {
	int i;
	T15111ec3(int i) {}
	class Sub extends T15111ec3 {
	    Sub() {
		// using the inherited i is illegal
		super(this.i);
	    }
	}
    }
} FAIL

