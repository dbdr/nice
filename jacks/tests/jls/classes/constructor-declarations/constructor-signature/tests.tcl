tcltest::test 8.8.2-signature-1 { The signature of a
        constructor consists of the number and types
        of formal parameters to the constructor } {
    empty_class T882s1 {
T882s1() {}
T882s1(int i) {}
T882s1(Object o) {}
    }
} PASS

tcltest::test 8.8.2-invalid-signature-1 { A class may not
        declare two constructors with the same signature,
        or a compile-time error occurs } {
    empty_class T882is1 {
T882is1(int i) {}
T882is1(int j) {}
    }
} FAIL
