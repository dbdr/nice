tcltest::test 7.3-1 { A compilation unit may be empty } {
    compile [saveas T731.java {}]
} PASS

tcltest::test 7.3-2 { A compilation unit may contain only a package statement } {
    compile [saveas test/T732.java {package test;}]
} PASS

tcltest::test 7.3-3 { A compilation unit may contain only import statements } {
    compile [saveas T733.java {import java.io.*;}]
} PASS

tcltest::test 7.3-4 { A compilation unit may contain only a type declaration } {
    compile [saveas T734.java {class T734 {}}]
} PASS

tcltest::test 7.3-5 { Type declarations are optional in compilation units } {
    compile [saveas test/T735.java {package test; import java.io.*;}]
} PASS

tcltest::test 7.3-6 { Package declarations are optional in compilation units } {
    compile [saveas T736.java {import java.io.*; class T736 {}}]
} PASS

tcltest::test 7.3-7 { Import declarations are optional in compilation units } {
    compile [saveas test/T737.java {package test; class T737 {}}]
} PASS

tcltest::test 7.3-8 { Compilation units may contain package, import, and
        type-declarations } {
    compile [saveas test/T738.java {package test; import java.io.*; class T738 {}}]
} PASS

tcltest::test 7.3-9 { package must come before import } {
    compile [saveas test/T739.java {import java.io.*; package test;}]
} FAIL

tcltest::test 7.3-10 { package must come before type-declarations } {
    compile [saveas test/T7310.java {class T7310 {} package test;}]
} FAIL

tcltest::test 7.3-11 { import must come before type-declarations } {
    compile [saveas T7311.java {class T7311 {} import java.io.*;}]
} FAIL

tcltest::test 7.3-12 { import must come before type-declarations,
        in this case, the extra ; } {
    compile [saveas test/T7312.java {package test;; import java.io.*;}]
} FAIL

tcltest::test 7.3-13 { Every compilation unit implicitly imports
        every public type from java.lang } {
     compile [saveas test/T7313.java {
package test; class T7313 {
    Object o;
    String s;
    Integer i;
}
}]
} PASS

