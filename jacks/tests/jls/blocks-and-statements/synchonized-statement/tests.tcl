tcltest::test 14.18-expression-1 { The type of the expression must be a
        reference } {
    empty_main T1418e1 {
        synchronized ("") {}
    }
} PASS

tcltest::test 14.18-expression-2 { The type of the expression must be a
        reference } {
    empty_main T1418e2 {
        synchronized (new Object()) {}
    }
} PASS

tcltest::test 14.18-expression-3 { The type of the expression must be a
        reference } {
    empty_main T1418e3 {
        synchronized (1) {}
    }
} FAIL

tcltest::test 14.18-expression-4 { The type of the expression must be a
        reference } {
    empty_main T1418e4 {
        synchronized (null) {}
    }
} FAIL

tcltest::test 14.18-expression-5 { The type of the expression must be a
        reference } {
    empty_main T1418e5 {
        synchronized ('a') {}
    }
} FAIL

tcltest::test 14.18-expression-6 { The type of the expression must be a
        reference } {
    empty_main T1418e6 {
        synchronized (System.out.println()) {}
    }
} FAIL

tcltest::test 14.18-expression-7 { The type of the expression must be a
        reference } {
    empty_main T1418e7 {
        synchronized ((String) null) {}
    }
} PASS

tcltest::test 14.18-syntax-1 { The synchronized expression must be in () } {
    empty_main T1418s1 {
        synchronized "" {}
    }
} FAIL

tcltest::test 14.18-syntax-2 { The synchronized statement must be in {} } {
    empty_main T1418s2 {
        synchronized ("") ;
    }
} FAIL

tcltest::test 14.18-syntax-3 { The synchronized statement must be in {} } {
    empty_main T1418s3 {
        synchronized ("") break;
    }
} FAIL

tcltest::test 14.18-syntax-4 { The synchronized block may not be labeled } {
    empty_main T1418s4 {
        synchronized ("") label: {};
    }
} FAIL

tcltest::test 14.18-lock-1 { A single thread may lock the same object
        multiple times (JLS example) } {
    empty_main T1418l1 {
        Object t = new Object();
        synchronized (t) {
            synchronized (t) {
                System.out.println("made it!");
            }
        }
    }
} PASS

tcltest::test 14.18-lock-2 { A single thread may lock the same object
        multiple times (in a synchronized instance method, this is locked)} {
    empty_class T1418l2 {
        synchronized void foo() {
            synchronized (this) {}
        }
    }
} PASS

tcltest::test 14.18-lock-3 { A single thread may lock the same object
        multiple times (in a synchronized static method, <name>.class is
        locked)} {
    empty_class T1418l3 {
        static synchronized void foo() {
            synchronized (T1418l3.class) {}
        }
    }
} PASS
