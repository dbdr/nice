tcltest::test 8.7-checked-exception-1 { Static initializers may not throw
        checked exceptions } {
    empty_class T87ce1 {
        static { if (false) throw new ClassNotFoundException(); }
    }
} FAIL

tcltest::test 8.7-checked-exception-2 { Static initializers may not throw
        checked exceptions } {
    empty_class T87ce2 {
        static int m() throws ClassNotFoundException { return 1; }
        static { m(); }
    }
} FAIL

tcltest::test 8.7-checked-exception-3 { Static initializers may not throw
        checked exceptions } {
    empty_class T87ce3 {
        static int m() throws ClassNotFoundException { return 1; }
        static { if (false) m(); }
    }
} FAIL

tcltest::test 8.7-abrupt-1 { Static initializers must be able to complete
        normally } {
    empty_class T87a1 {
        static { throw new RuntimeException(); }
    }
} FAIL

tcltest::test 8.7-abrupt-2 { Static initializers must be able to complete
        normally, by 14.20, if(true) always completes normally } {
    empty_class T87a2 {
        static { if (true) throw new RuntimeException(); }
    }
} PASS

tcltest::test 8.7-return-1 { Static initializers may not return } {
    empty_class T87r1 {
        static { return; }
    }
} FAIL

tcltest::test 8.7-return-2 { Static initializers may not return } {
    empty_class T87r2 {
        static { if (false) return; }
    }
} FAIL

tcltest::test 8.7-return-3 { Static initializers may not return } {
    empty_class T87r3 {
        static { return 1; }
    }
} FAIL

tcltest::test 8.7-return-4 { Static initializers may not return } {
    empty_class T87r4 {
        static { if (false) return 1; }
    }
} FAIL

tcltest::test 8.7-this-1 { Static initializers may not access this } {
    empty_class T87t1 {
        static { this.toString(); }
    }
} FAIL

tcltest::test 8.7-this-2 { Static initializers may not access this, not
        even implicitly } {
    empty_class T87t2 {
        static { toString(); }
    }
} FAIL

tcltest::test 8.7-super-1 { Static initializers may not access super } {
    empty_class T87s1 {
        static { super.toString(); }
    }
} FAIL
