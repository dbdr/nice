tcltest::test 8.1.1.2-conflicting-modifier-1 { a final class
        cannot also be declared abstract } {
    compile [saveas T8112cm1.java {
final abstract class T8112cm1 {}
    }]
} FAIL

tcltest::test 8.1.1.2-invalid-subclass-1 { A compile-time
        error occurs if the name of a final class appears
        in the extends clause of another class declaration } {
    compile [saveas T8112is1.java {
final class T8112is1_super {}
class T8112is1 extends T8112is1_super {}
    }]
} FAIL

tcltest::test 8.1.1.2-invalid-subclass-2 { A final
        class can not be extended via an
        anymous innerclass } {
    compile [saveas T8112is2.java {
final class T8112is2 {
    Object o = new T8112is2() {};
}
    }]
} FAIL
