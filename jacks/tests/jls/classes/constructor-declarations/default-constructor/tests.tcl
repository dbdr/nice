tcltest::test 8.8.7-default-1 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters is automatically provided } {
    empty_class T887d1 {
Object o = new T887d1();
    }
} PASS

tcltest::test 8.8.7-default-2 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters is automatically provided } {
    saveas T887d2.java {
class T887d2_super {}
class T887d2_subclass extends T887d2_super {}
class T887d2 {
    Object o = new T887d2_subclass();
}
    }

    compile T887d2.java
} PASS



tcltest::test 8.8.7-throws-1 { A default
        constructor has no throws clause } {
    saveas T887t1.java {
class T887t1_super {
    T887t1_super() throws Exception {
        throw new Exception();
    }
}
class T887t1_subclass extends T887t1_super {}
    }

    compile T887t1.java
} FAIL





# A compile-time error occurs if a default constructor
# is provided by the compiler but the superclass does
# not have an accessible constructor that takes no arguments.

tcltest::test 8.8.7-inaccessible-default-constructor-toplevel-1 {
        A private constructor is not accessible in a subclass } {
    saveas T887idct1.java {
class T887idct1_super {
    private T887idct1_super() {}
}

class T887idct1_subclass extends T887idct1_super {}
    }

    compile T887idct1.java
} FAIL


tcltest::test 8.8.7-inaccessible-default-constructor-toplevel-2 {
        A constructor with default accessibility in some other
        package is not accessible in a subclass } {

    saveas T887idct2_super.java {
package T887idct2_pkg;
public class T887idct2_super {
    T887idct2_super() {}
}
    }

    saveas T887idct2.java {
import T887idct2_pkg.T887idct2_super;
class T887idct2_subclass extends T887idct2_super {}
    }

    compile T887idct2_super.java T887idct2.java
} FAIL







tcltest::test 8.8.7-accessible-default-constructor-inner-1 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {

    empty_class T887adci1 {
private T887adci1() {}
static class T887adci1_inner extends T887adci1 {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-2 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {

    empty_class T887adci2 {
private T887adci2() {}
class T887adci2_inner extends T887adci2 {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-3 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {

    empty_class T887adci3 {
private T887adci3() {}
Object o = new T887adci3() {
    void f() {}
};
}
} PASS





# FIXME: add tests for default protection that depends on class protection
