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
inteface T912u1 extends Cloneable, Cloneable {}
}]
} FAIL

tcltest::test 9.1.2-unique-2 { The extends clause must not list an element
        twice, by any name } {
    compile [saveas T912u2.java {
inteface T912u2 extends java.lang.Cloneable, Cloneable {}
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
