tcltest::test 15.9-unqualified-1 { a
        ClassInstanceCreationExpression
        that starts with the keyword new
        is known as an unqualified class
        instance creation expression } {

    empty_class T159u1 {
        Object o = new T159u1();
    }
} PASS

tcltest::test 15.9-unqualified-2 { an
        unqualified class instance
        creation expression can create
        instances of a toplevel, member,
        local, or anonymous class } {

    empty_class T159u2 {
        class Inner {}
        static class Sinner {}
        void foo() {
            new T159u2(); // toplevel
            new Inner(); // member
            new Sinner(); // static member

            class Local {}
            new Local(); // local

            new T159u2() {}; // anonymous
        }
    }
} PASS

# FIXME: add qualified example.
