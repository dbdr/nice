tcltest::test 6.5.3.2-qualified-1 { It is an error if there is not a member
        subpackage with the given qualified package name } {
    compile [saveas p1/T6532s1.java {
package p1;
class T6532s1 {
    void foo() {
        p1.badsub.Type.i++; // p1.badsub was reclassified from ambiguous
    }
}
}]
} FAIL

tcltest::test 6.5.3.2-qualified-2 { It is an error if the qualifier of a
        package name is not an observable package } {
    empty_main T6532q2 {
        // badpackage.badsub was reclassified from ambiguous
        badpackage.badsub.Type.i++;
    }
} FAIL

tcltest::test 6.5.3.2-qualified-3 { A qualified package name denotes the member
        subpackage of the qualifying package } {
    compile [saveas p1/sub/T6532q3a.java {
package p1.sub;
public class T6532q3a {
    public static int i;
}
}] [saveas p1/T6532q3b.java {
package p1;
class T6532q3b {
    int j = p1.sub.T6532q3a.i; // p1.sub was reclassified from ambiguous
}
}]
} PASS

