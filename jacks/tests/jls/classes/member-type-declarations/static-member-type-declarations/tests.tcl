tcltest::test 8.5.2-static-member-usage-1 { a member type
        with the static modifier may use static members
        of the enclosing class } {
    empty_class T852smu1 {
    static int foo;

    static class T852smu1_Test {
        T852smu1_Test() { foo = 0; }
    }
    }
} PASS

tcltest::test 8.5.2-static-member-usage-2 { a member type
        with the static modifier may use static members
        of the enclosing class } {
    empty_class T852smu2 {
    static void foo() {}

    static class T852smu2_Test {
        T852smu2_Test() { foo(); }
    }
    }
} PASS

tcltest::test 8.5.2-non-static-member-usage-1 { It is a
        compile-time error if a static class contains a
        usage of a non-static member of the enclosing class } {
    empty_class T852nsmu1 {
    int foo;

    static class T852nsmu1_Test {
        T852nsmu1_Test() { foo = 0; }
    }
    }
} FAIL

tcltest::test 8.5.2-non-static-member-usage-2 { It is a
        compile-time error if a static class contains a
        usage of a non-static member of the enclosing class } {
    empty_class T852nsmu2 {
    void foo() {}

    static class T852nsmu2_Test {
        T852nsmu2_Test() { foo(); }
    }
    }
} FAIL

tcltest::test 8.5.2-accessible-static-member-usage-1 {
        a member type with the static modifer may access
        private static members of the enclosing class } {
    empty_class T852asmu1 {
    private static int foo;

    static class T852asmu1_Test {
        T852asmu1_Test() { foo = 0; }
    }
    }
} PASS

tcltest::test 8.5.2-accessible-static-member-usage-2 {
        a member type with the static modifer may access
        private static members of the enclosing class } {
    empty_class T852asmu2 {
    private static void foo() {}

    static class T852asmu2_Test {
        T852asmu2_Test() { foo(); }
    }
    }
} PASS

tcltest::test 8.5.2-inherited-non-static-member-usage-1 {
        a static member type may use non-static members of
        a superclass that happens to be the enclosing class } {
    empty_class T852insmu1 {
    int foo;

    static class T852insmu1_Test extends T852insmu1 {
        T852insmu1_Test() { foo = 0; }
    }
    }
} PASS

tcltest::test 8.5.2-inherited-non-static-member-usage-2 {
        a static member type may use non-static members of
        a superclass that happens to be the enclosing class } {
    empty_class T852insmu2 {
    void foo() {}

    static class T852insmu2_Test extends T852insmu2 {
        T852insmu2_Test() { foo(); }
    }
    }
} PASS

tcltest::test 8.5.2-accessible-inherited-non-static-member-usage-1 {
        a static member type may use private non-static members of
        a superclass that happens to be the enclosing class } {
    empty_class T852ainsmu1 {
    private int foo;

    static class T852ainsmu1_Test extends T852ainsmu1 {
        T852ainsmu1_Test() { super.foo = 0; }
    }
    }
} PASS

tcltest::test 8.5.2-accessible-inherited-non-static-member-usage-2 {
        a static member type may use private non-static members of
        a superclass that happens to be the enclosing class } {
    empty_class T852ainsmu2 {
    private void foo() {}

    static class T852ainsmu2_Test extends T852ainsmu2 {
        T852ainsmu2_Test() { super.foo(); }
    }
    }
} PASS

