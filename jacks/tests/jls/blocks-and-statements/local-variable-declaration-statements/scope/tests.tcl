tcltest::test 14.4.2-valid-scope-1 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs1 {
        int i = (i = 1) + i;
    }
} PASS

tcltest::test 14.4.2-valid-scope-2 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs2 {
        int i = 1, j = i;
    }
} PASS

tcltest::test 14.4.2-valid-scope-3 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs3 {
        int i;
        i = 1;
    }
} PASS

tcltest::test 14.4.2-valid-scope-4 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs4 {
        { int i; }
        int i;
    }
} PASS

tcltest::test 14.4.2-valid-scope-5 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs5 {
        for (int i = (i = 1) + i; ; );
    }
} PASS

tcltest::test 14.4.2-valid-scope-6 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs6 {
        for (int i = 1, j = i; ; );
    }
} PASS

tcltest::test 14.4.2-valid-scope-7 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs7 {
        for (int i; ; i = 1);
    }
} PASS

tcltest::test 14.4.2-valid-scope-8 { The scope of a local variable declarator
        includes its own initializer and further declarations to right } {
    empty_main T1442vs8 {
        for (int i = 1; i < 1; );
        for (int i; ; );
    }
} PASS

tcltest::test 14.4.2-invalid-scope-1 { A local variable cannot be redeclared } {
    empty_main T1442is1 {
        int i;
        int i;
    }
} FAIL

tcltest::test 14.4.2-invalid-scope-2 { A local variable cannot be redeclared } {
    empty_main T1442is2 {
        int i;
        for (int i; ; );
    }
} FAIL

tcltest::test 14.4.2-invalid-scope-3 { A local variable cannot be redeclared } {
    empty_main T1442is3 {
        int i;
        try {
            throw new Exception();
        } catch (Exception i) {
        }
    }
} FAIL

tcltest::test 14.4.2-invalid-scope-4 { A local variable cannot be redeclared } {
    empty_class T1442is4 {
        void foo(int i) {
            int i;
        }
    }
} FAIL

tcltest::test 14.4.2-invalid-scope-5 { A local variable cannot be redeclared } {
    empty_main T1442is5 {
        for (int i; ; ) {
            int i;
        }
    }
} FAIL

tcltest::test 14.4.2-invalid-scope-6 { A local variable cannot be redeclared } {
    empty_main T1442is6 {
        try {
            throw new Exception();
        } catch (Exception i) {
            int i;
        }
    }
} FAIL

tcltest::test 14.4.2-shadow-1 { A local variable may shadow member variables } {
    empty_class T1442s1 {
        Object i;
        void foo() {
            i = new Object();
            int i;
            i = 1;
        }
    }
} PASS

tcltest::test 14.4.2-shadow-2 { A local variable may shadow member variables } {
    empty_class T1442s2 {
        static Object i;
        void foo() {
            i = new Object();
            int i;
            i = 1;
        }
    }
} PASS

tcltest::test 14.4.2-shadow-3 { A local variable may shadow member variables } {
    empty_class T1442s3 {
        static Object i;
        static void foo() {
            i = new Object();
            int i;
            i = 1;
        }
    }
} PASS

tcltest::test 14.4.2-shadow-4 { A local variable may be shadowed by a member } {
    empty_main T1442s4 {
        Object i;
        new Object() {
            int i;
        };
    }
} PASS

tcltest::test 14.4.2-shadow-5 { A local variable may be shadowed by a local } {
    empty_main T1442s5 {
        final Object i = null;
        new Object() {
            {
                Object o = i;
                int i;
                i = 1;
            }
        };
    }
} PASS

tcltest::test 14.4.2-shadow-6 { A local variable may be shadowed by a local } {
    empty_main T1442s6 {
        final Object i = null;
        new Object() {
            {
                Object o = i;
                for (int i = 1; i < 1; );
                o = i;
            }
        };
    }
} PASS

tcltest::test 14.4.2-shadow-7 { A local variable may be shadowed by a local } {
    empty_main T1442s7 {
        final int i = 1;
        new Object() {
            {
                try {
                    int j = i;
                    throw new Exception();
                } catch (Exception i) {
                }
            }
        };
    }
} PASS

tcltest::test 14.4.2-shadow-8 { A local variable may be shadowed by a
        parameter } {
    empty_main T1442s8 {
        final Object i = null;
        new Object() {
            Object o = i;
            void foo(int i) {
                i = 1;
            }
        };
    }
} PASS

tcltest::test 14.4.2-shadow-9 { A local variable may be shadowed by an inherited
        member } {
    ok_pass_or_warn [empty_class T1442s9 {
        void foo(final byte b) {
            class One {
                final int i = 1;
            }
            Object i;
            class Two extends One {
                {
                    switch (b) {
                        case 0:
                        case (i == 1) ? 1 : 0:
                    }
                }
            }
        }
    }]
} OK
