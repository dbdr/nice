tcltest::test 8.3.2-abrupt-1 { A class variable initializer may not complete
        abruptly with a checked exception } {
    empty_class T832a1 {
        static int m() throws ClassNotFoundException { return 1; }
        static int i = m();
    }
} FAIL

tcltest::test 8.3.2-abrupt-2 { An instance variable initializer may not
        complete abruptly with a checked exception, unless all constructors
        have a compatible throws clause } {
    empty_class T832a2 {
        int m() throws ClassNotFoundException { return 1; }
        int i = m();
    }
} FAIL

tcltest::test 8.3.2-abrupt-3 { An instance variable initializer may not
        complete abruptly with a checked exception, unless all constructors
        have a compatible throws clause } {
    empty_class T832a3 {
        int m() throws ClassNotFoundException { return 1; }
        int i = m();
        T832a3() throws ClassNotFoundException {}
    }
} PASS

tcltest::test 8.3.2-abrupt-4 { An instance variable initializer may not
        complete abruptly with a checked exception, unless all constructors
        have a compatible throws clause } {
    empty_class T832a4 {
        int m() throws ClassNotFoundException { return 1; }
        int i = m();
        T832a4() throws ClassNotFoundException {}
        T832a4(int i) {}
    }
} FAIL

tcltest::test 8.3.2-abrupt-5 { An instance variable initializer may not
        complete abruptly with a checked exception, unless all constructors
        have a compatible throws clause } {
    empty_class T832a5 {
        int m() throws ClassNotFoundException { return 1; }
        int i = m();
        T832a5() throws Throwable {}
    }
} PASS

tcltest::test 8.3.2-abrupt-6 { An instance variable initializer may not
        complete abruptly with a checked exception, unless all constructors
        have a compatible throws clause; the generated constructor in an
        anonymous class always has the right throws clause } {
    empty_main T832a6 {
        try {
            new Object() {
                int m() throws ClassNotFoundException { return 1; }
                int i = m();
            };
        } catch (ClassNotFoundException e) {
        }
    }
} PASS

tcltest::test 8.3.2-abrupt-7 { An instance variable initializer may not
        complete abruptly with a checked exception, unless all constructors
        have a compatible throws clause; the generated constructor in an
        anonymous class always has the right throws clause } {
    empty_class T832a7 {
        void foo() throws ClassNotFoundException {
            new Object() {
                int m() throws ClassNotFoundException { return 1; }
                int i = m();
            };
        }
    }
} PASS

