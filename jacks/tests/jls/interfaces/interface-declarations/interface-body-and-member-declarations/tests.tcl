tcltest::test 9.1.3-body-1 { An interface may include a field declaration } {
    empty_class T913b1 "interface I { int i = 1; }"
} PASS

tcltest::test 9.1.3-body-2 { An interface may include an abstract method
        declaration } {
     empty_class T913b2 "interface I { void m(); }"
} PASS

tcltest::test 9.1.3-body-3 { An interface may include a class declaration } {
    empty_class T913b3 "interface I { class C {} }"
} PASS

tcltest::test 9.1.3-body-4 { An interface may include an interface
        declaration } {
    empty_class T913b4 "interface I { interface I1 {} }"
} PASS

tcltest::test 9.1.3-body-5 { An interface may include an empty declaration } {
    empty_class T913b5 "interface I { ; }"
} PASS

tcltest::test 9.1.3-body-6 { An interface may not have a constructor } {
    empty_class T913b6 "interface I { I() {} }"
} FAIL

tcltest::test 9.1.3-body-7 { An interface may not have an instance
         initializer } {
    empty_class T913b7 "interface I { {} }"
} FAIL

tcltest::test 9.1.3-body-8 { An interface may not have a static initializer } {
    empty_class T913b8 "interface I { static {} }"
} FAIL

