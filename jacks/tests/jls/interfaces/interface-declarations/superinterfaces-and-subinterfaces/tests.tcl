tcltest::test 9.1.2-accessible-1 { All interfaces in the extends clause must
        be accessible } {
    compile [saveas T912a1a.java {
class T912a1a {
    private interface I {}
}
interface T912a1b extends T912a1a.I {}
}]
} FAIL

tcltest::test 9.1.2-accessible-2 { All interfaces in the extends clause must
        be accessible } {
    compile [saveas T912a2a.java {
interface T912a2a {}
interface T912a2b {}
interface T912a2c {}
interface T912a2d extends T912a2a, T912a2b, T912a2c {}
}]
} PASS

tcltest::test 9.1.2-accessible-3 { All interfaces in the extends clause must
        be accessible } {
    compile [saveas p1/T912a3a.java {
package p1;
public class T912a3a {
    protected interface I {}
}
}] [saveas T912a3b.java {
interface T912a3b extends p1.T912a3a.I {}
}]
} FAIL

tcltest::test 9.1.2-accessible-4 { All interfaces in the extends clause must
        be accessible } {
    compile [saveas p1/T912a4a.java {
package p1;
interface T912a4a {}
    }] [saveas T912a4b.java {
interface T912a4b extends p1.T912a4a {}
    }]
} FAIL

tcltest::test 9.1.2-accessible-5 { All interfaces in the extends clause must
        be accessible } {
    compile [saveas p1/T912a5a.java {
package p1;
public class T912a5a {
    protected interface I {}
}
    }] [saveas T912a5b.java {
class T912a5b extends p1.T912a5a {
    interface Sub extends I {}
}
    }]
} PASS

tcltest::test 9.1.2-accessible-6 { All interfaces in the extends clause must
        be accessible } {
    empty_class T912a6 {
	private interface One {}
	interface Two extends One {}
    }
} PASS

tcltest::test 9.1.2-interface-1 { All types in the extends clause must be
        interfaces } {
    empty_class T912i1 {
	interface I extends Object {}
    }
} FAIL

tcltest::test 9.1.2-interface-2 { All types in the extends clause must be
        interfaces } {
    empty_class T912i2 {
	class C {}
	interface I extends C {}
    }
} FAIL

tcltest::test 9.1.2-circular-1 { An interface must not depend on itself } {
    compile [saveas T912c1.java "interface T912c1 extends T912c1 {}"]
} FAIL

tcltest::test 9.1.2-circular-2 { An interface must not depend on itself } {
    compile [saveas T912c2.java {
interface T912c2 extends T912c2.I {
    interface I {}
}
}]
} FAIL

tcltest::test 9.1.2-circular-3 { An interface must not depend on itself } {
    compile [saveas T912c3a.java {
interface T912c3a extends T912c3b {}
interface T912c3b extends T912c3a {}
}]
} FAIL

tcltest::test 9.1.2-circular-4 { An interface must not depend on itself } {
    compile [saveas T912c4a.java {
interface T912c4a extends T912c4b.Inner2 {
    interface Inner1 {}
}
interface T912c4b {
    interface Inner2 extends T912c4a.Inner1 {}
}
}]
} FAIL

tcltest::test 9.1.2-circular-5 { An interface must not depend on itself } {
    compile [saveas T912c5a.java {
interface T912c5a {
    interface Inner1 extends T912c5b.Inner2 {}
}
interface T912c5b extends T912c5a {
    interface Inner2 {}
}
}]
} PASS

tcltest::test 9.1.2-unique-1 { The extends clause must not list an element
        twice, by any name } {
    compile [saveas T912u1.java {
interface T912u1 extends Cloneable, Cloneable {}
}]
} FAIL

tcltest::test 9.1.2-unique-2 { The extends clause must not list an element
        twice, by any name } {
    compile [saveas T912u2.java {
interface T912u2 extends java.lang.Cloneable, Cloneable {}
}]
} FAIL

tcltest::test 9.1.2-unique-3 { The extends clause must not list an element
        twice, by any name } {
    compile [saveas T912u3a.java {
class T912u3a {
    interface I1 {}
}
class T912u3b extends T912u3a {}
interface T912u3c extends T912u3a.I1, T912u3b.I1 {}
}]
} FAIL

tcltest::test 9.1.2-unique-4 { An interface may inherit the same superinterface
        via more than one path } {
    compile [saveas T912u4a.java {
interface T912u4a {}
interface T912u4b extends T912u4a {}
interface T912u4c extends T912u4a, T912u4b {}
}]
} PASS
