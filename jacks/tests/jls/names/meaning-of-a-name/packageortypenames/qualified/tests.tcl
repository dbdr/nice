tcltest::test 6.5.4.2-qualified-1 { A package-or-type name is reclassified as
        a type name if the qualifier package name contains that member type } {
    compile [saveas p1/T6542q1a.java {
package p1;
public class T6542q1a {
    public static class Inner {}
}
}] [saveas T6542q1b.java {
class T6542q1b extends p1.T6542q1a.Inner {}
}]
} PASS

tcltest::test 6.5.4.2-qualified-2 { A package-or-type name is reclassified as
        a type name if the qualifier type name contains that member type } {
    empty_class T6542q2 {
        class Middle {
            class Inner {}
        }
        T6542q2.Middle.Inner t;
    }
} PASS

tcltest::test 6.5.4.2-qualified-3 { It is an error if the qualifier type name
        of a package-or-type name does not contain the named member type } {
    empty_class T6542q3 {
        // T6542q3.Middle is not interpreted as a package name, since T6542q3
        // is not a package name; but it is still an error
        T6542q3.Middle.Inner t;
    }
} FAIL

tcltest::test 6.5.4.2-qualified-4 { A package-or-type name is reclassified as
        a package name if the qualifier package name does not contain that
        member type } {
    compile [saveas p1/sub/T6542q4a.java {
package p1.sub;
public class T6542q4a {}
}] [saveas p1/T6542q4b.java {
package p1;
class T6542q4b {
    // p1.sub is classified as a package name
    p1.sub.T6542q4a t;
}
}]
} PASS

tcltest::test 6.5.4.2-qualified-5 { A package-or-type name is reclassified as
        a package name if the qualifier package name does not contain that
        member type; an error will eventually occur if the qualifier package
        is not observable } {
    empty_class T6542q5 {
        badpackage.badsub.Type t;
    }
} FAIL

tcltest::test 6.5.4.2-qualified-6 { A package-or-type name is reclassified as
        a package name if the qualifier package name does not contain that
        member type; an error will eventually occur if the qualifier package
        does not contain the member subpackage } {
    compile [saveas p1/T6542q6.java {
package p1;
class T6542q6 {
    p1.badsub.Type t;
}
}]
} FAIL
