tcltest::test 16.5-anonymous-1 { V is DA before an anonymous class iff V is
        DA after the class instance creation expression } {
    empty_main T165a1 {
        final int v;
        new Object() {
            int i = v;
        };
    }
} FAIL

tcltest::test 16.5-anonymous-2 { V is DA before an anonymous class iff V is
        DA after the class instance creation expression } {
    empty_main T165a2 {
        final int v;
        v = 1;
        new Object() {
            int i = v;
        };
    }
} PASS

tcltest::test 16.5-anonymous-3 { V is DA before an anonymous class iff V is
        DA after the class instance creation expression } {
    empty_class T165a3 {
        T165a3(int j) {}
        void foo() {
            final int v;
            new T165a3(v = 1) {
                int i = v;
            };
        }
    }
} PASS

