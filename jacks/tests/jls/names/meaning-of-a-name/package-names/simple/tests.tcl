tcltest::test 6.5.3.1-simple-1 { It is an error if there is no top-level
        package with the given simple package name } {
    empty_main T6531s1 {
        badpackage.Type.i++; // badpackage was reclassified from ambiguous
    }
} FAIL

tcltest::test 6.5.3.1-simple-2 { A simple package name denotes the top-level
        package which is in scope } {
    compile [saveas p1/T6531s2.java {
package p1;
class T6531s2 {
    static int i;
    void foo() {
        p1.T6531s2.i++; // p1 was reclassified from ambiguous
    }
}
}]
} PASS
