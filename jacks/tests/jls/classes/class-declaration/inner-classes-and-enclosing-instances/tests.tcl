tcltest::test 8.1.2-static-1 { Static variables inside inner class must be
        compile-time constants } {
    empty_class T812s1 {
        class I {
            // not primitive or String
            private final static String[] k = {"test", "test2"};
        }
    }
} FAIL

tcltest::test 8.1.2-static-2 { Static variables inside inner class must be
        compile-time constant } {
    empty_class T812s2 {
        class I {
            // good
            private final static int k = 5;
        }
    }
} PASS

tcltest::test 8.1.2-static-3 { Static variables inside inner class must be
        compile-time constant } {
    empty_class T812s3 {
        class I {
            // not final
            private static int k = 5;
        }
    }
} FAIL

tcltest::test 8.1.2-static-4 { Static variables inside inner class must be
        compile-time constant } {
    empty_class T812s4 {
        class I {
            // not constant
            private static int k = new Integer(5).intValue();
        }
    }
} FAIL

tcltest::test 8.1.2-static-5 { static initializers are not allowed in
        anonymous classes } {
    empty_class T812s5 {
        Object o = new Object() {
            static {}
        }
    }
} FAIL

tcltest::test 8.1.2-static-6 { syntax error in illegal static initializers
        should not cause core dump (jikes bug 343) } {
    empty_class T812s6 {
        Object o = new Object() {
            static { a }
        }
    }
} FAIL

tcltest::test 8.1.2-static-7 { local classes in static context still may not
        have static members } {
    empty_main T812s7 {
        class Local { // in static main
            static int i;
        }
    }
} FAIL

tcltest::test 8.1.2-static-8 { local classes in static context still may not
        have static members } {
    empty_main T812s8 {
        class Local { // in static main
            static void foo() {}
        }
    }
} FAIL

tcltest::test 8.1.2-static-9 { local classes in static context still may not
        have static members } {
    empty_main T812s9 {
        class Local { // in static main
            static {}
        }
    }
} FAIL

tcltest::test 8.1.2-static-10 { local classes in static context still may not
        have static members } {
    empty_main T812s10 {
        class Local { // in static main
            static class Inner {}
        }
    }
} FAIL

tcltest::test 8.1.2-static-11 { local classes in static context still may not
        have static members } {
    empty_main T812s11 {
        class Local { // in static main
            interface Inner {}
        }
    }
} FAIL

tcltest::test 8.1.2-static-12 { anonymous classes in static context still
        may not have static members } {
    empty_main T812s12 {
        new Object() { // in static main
            static int i;
        };
    }
} FAIL

tcltest::test 8.1.2-static-13 { anonymous classes in static context still
        may not have static members } {
    empty_main T812s13 {
        new Object() { // in static main
            static void foo() {}
        };
    }
} FAIL

tcltest::test 8.1.2-static-14 { anonymous classes in static context still
        may not have static members } {
    empty_main T812s14 {
        new Object() { // in static main
            static {}
        };
    }
} FAIL

tcltest::test 8.1.2-static-15 { anonymous classes in static context still
        may not have static members } {
    empty_main T812s15 {
        new Object() { // in static main
            static class Inner {}
        };
    }
} FAIL

tcltest::test 8.1.2-static-16 { anonymous classes in static context still
        may not have static members } {
    empty_main T812s16 {
        new Object() { // in static main
            interface Inner {}
        };
    }
} FAIL

tcltest::test 8.1.2-static-17 { anonymous classes in static context still
        may not have static members } {
    empty_class T812s17 {
        T812s17(Object o) {}
        T812s17() {
            // static context in explicit constructor invocation
            this(new Object() {
                static int i;
            });
        }
    }
} FAIL
