tcltest::test 8.6-checked-exception-1 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce1 {
        { if (true) throw new ClassNotFoundException(); }
    }
} FAIL

tcltest::test 8.6-checked-exception-2 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce2 {
        { if (true) throw new ClassNotFoundException(); }
        T86ce2() throws ClassNotFoundException {}
    }
} PASS

tcltest::test 8.6-checked-exception-3 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce3 {
        { if (true) throw new ClassNotFoundException(); }
        T86ce3() throws ClassNotFoundException {}
        T86ce3(int i) {}
    }
} FAIL

tcltest::test 8.6-checked-exception-4 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce4 {
        { if (true) throw new ClassNotFoundException(); }
        T86ce4() throws Throwable {}
    }
} PASS

tcltest::test 8.6-checked-exception-5 { Instance initializers of anonymous
        classes may throw any exception, and the generated constructor must
        automatically throw such exceptions } {
    empty_main T86ce5 {
        try {
            new Object() {
                { if (true) throw new ClassNotFoundException(); }
            };
        } catch (ClassNotFoundException e) {
        }
    }
} PASS

tcltest::test 8.6-checked-exception-6 { Instance initializers of anonymous
        classes may throw any exception, and the generated constructor must
        automatically throw such exceptions } {
    empty_class T86ce6 {
        void foo() throws ClassNotFoundException {
            new Object() {
                { if (true) throw new ClassNotFoundException(); }
            };
        }
    }
} PASS

tcltest::test 8.6-checked-exception-7 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce7 {
        int m() throws ClassNotFoundException { return 1; }
        { m(); }
    }
} FAIL

tcltest::test 8.6-checked-exception-8 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce8 {
        int m() throws ClassNotFoundException { return 1; }
        { m(); }
        T86ce8() throws ClassNotFoundException {}
    }
} PASS

tcltest::test 8.6-checked-exception-9 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce9 {
        int m() throws ClassNotFoundException { return 1; }
        { m(); }
        T86ce9() throws ClassNotFoundException {}
        T86ce9(int i) {}
    }
} FAIL

tcltest::test 8.6-checked-exception-10 { Instance initializers of non-anonymous
        classes may throw checked exceptions only if all constructors
        have compatible throws clauses } {
    empty_class T86ce10 {
        int m() throws ClassNotFoundException { return 1; }
        { m(); }
        T86ce10() throws Throwable {}
    }
} PASS

tcltest::test 8.6-checked-exception-11 { Instance initializers of anonymous
        classes may throw any exception, and the generated constructor must
        automatically throw such exceptions } {
    empty_main T86ce11 {
        try {
            new Object() {
                int m() throws ClassNotFoundException { return 1; }
                { m(); }
            };
        } catch (ClassNotFoundException e) {
        }
    }
} PASS

tcltest::test 8.6-checked-exception-12 { Instance initializers of anonymous
        classes may throw any exception, and the generated constructor must
        automatically throw such exceptions } {
    empty_class T86ce12 {
        void foo() throws ClassNotFoundException {
            new Object() {
                int m() throws ClassNotFoundException { return 1; }
                { m(); }
            };
        }
    }
} PASS

tcltest::test 8.6-abrupt-1 { Instance initializers must be able to complete
        normally } {
    empty_class T86a1 {
        { throw new RuntimeException(); }
    }
} FAIL

tcltest::test 8.6-abrupt-2 { Instance initializers must be able to complete
        normally } {
    empty_class T86a2 {
        { throw new RuntimeException(); }
        T86a2() throws RuntimeException {}
    }
} FAIL

tcltest::test 8.6-abrupt-3 { Instance initializers must be able to complete
        normally, by 14.20, if(true) always completes normally } {
    empty_class T86a3 {
        { if (true) throw new RuntimeException(); }
    }
} PASS

tcltest::test 8.6-abrupt-4 { Instance initializers must be able to complete
        normally } {
    empty_class T86a4 {
        int m() throws ClassNotFoundException { return 1; }
        { m(); }
        T86a4() throws ClassNotFoundException {}
    }
} PASS

tcltest::test 8.6-return-1 { Instance initializers may not return } {
    empty_class T86r1 {
        { return; }
    }
} FAIL

tcltest::test 8.6-return-2 { Instance initializers may not return } {
    empty_class T86r2 {
        { if (false) return; }
    }
} FAIL

tcltest::test 8.6-return-3 { Instance initializers may not return } {
    empty_class T86r3 {
        { return 1; }
    }
} FAIL

tcltest::test 8.6-return-4 { Instance initializers may not return } {
    empty_class T86r4 {
        { if (false) return 1; }
    }
} FAIL

tcltest::test 8.6-this-1 { Instance initializers may access this } {
    empty_class T86t1 {
        { this.toString(); }
    }
} PASS

tcltest::test 8.6-super-1 { Instance initializers may access super } {
    empty_class T86s1 {
        { super.toString(); }
    }
} PASS
