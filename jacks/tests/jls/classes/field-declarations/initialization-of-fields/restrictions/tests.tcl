tcltest::test 8.3.2.3-illegal-forward-instance-1 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifi1 {
        int i = j;
        int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-2 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifi2 {
        { int i = j; }
        int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-3 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifi3 {
        int i = j += 1;
        int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-4 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifi4 {
        { j += 1; }
        int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-5 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifi5 {
        int i = j++;
        int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-6 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifi6 {
        { j++; }
        int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-7 { Reading a variable is not
        legal before declaration, even if it was assigned before declaration } {
    empty_class T8323ifi7 {
        int i = (i = 1) + i;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-8 { Reading a variable is not
        legal before declaration, even if it was assigned before declaration } {
    empty_class T8323ifi8 {
        int j = i = 1;
        int i = i;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-instance-9 { Reading a variable is not
        legal before declaration, even if it was assigned before declaration } {
    empty_class T8323ifi9 {
	int j = i = 1;
	int k = i;
	int i;
    }
} FAIL

tcltest::test 8.3.2.3-legal-forward-instance-1 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi1 {
        int i = this.j; // not simple usage
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-2 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi2 {
        void foo() { int i = j; } // not in initializer
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-3 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi3 {
        T8323lfi3() { int i = j; } // not in initializer
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-4 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi4 {
        int i = j = 2; // simple assignment
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-5 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi5 {
        { j = 2; } // simple assignment
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-6 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi6 {
        int i = new Object(){ int bar() { return j; } }.bar();
        // not in declaring class
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-7 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi7 {
        { new Object(){ { j++; } }; } // not in declaring class
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-8 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi8 {
        int i = j; // j is static
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-9 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi9 {
        { j++; } // j is static
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-10 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi10 {
        final int i = j; // j is static
        static final int j = 1;
        void foo(int n) {
            switch (n) {
                case 0:
                case i: // i == 1
            }
        }
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-11 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfi11 {
        final int i = this.j; // not simple usage
        final int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-instance-12 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T83231fi12 {
        int j = i = 1;
        final int i;
    }
} PASS

tcltest::test 8.3.2.3-illegal-forward-static-1 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifs1 {
        static int i = j;
        static int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-2 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifs2 {
        static { int i = j; }
        static int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-3 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifs3 {
        static int i = j += 1;
        static int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-4 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifs4 {
        static { j += 1; }
        static int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-5 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifs5 {
        static int i = j++;
        static int j = 1;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-6 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323ifs6 {
        static { j++; }
        static int j = 1;
    }
} FAIL


tcltest::test 8.3.2.3-illegal-forward-static-7 { Reading a variable is not
        legal before declaration, even if it was assigned before declaration } {
    empty_class T8323ifs7 {
        static int i = (i = 1) + i;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-8 { Reading a variable is not
        legal before declaration, even if it was assigned before declaration } {
    empty_class T8323ifs8 {
        static int j = i = 1;
        static int i = i;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-9 { Reading a variable is not
        legal before declaration, even if it was assigned before declaration } {
    empty_class T8323ifs9 {
	static int j = i = 1;
	static int k = i;
	static int i;
    }
} FAIL

tcltest::test 8.3.2.3-illegal-forward-static-10 { Reading a variable is not
        legal before declaration, even though other classes may trigger
        out-of-order reads } {
    compile [saveas T8323ifs10a.java {
class T8323ifs10a {
    int k = T8323ifs10b.i;
}
    }] [saveas T8323ifs10b.java {
class T8323ifs10b {
    static final int i = j;
    static int j;
}
    }]
} FAIL

tcltest::test 8.3.2.3-legal-forward-static-1 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs1 {
        static int i = T8323lfs1.j; // not simple usage
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-2 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs2 {
        static void foo() { int i = j; } // not in initializer
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-3 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs3 {
        T8323lfs3() { int i = j; } // not in initializer
        int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-4 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs4 {
        static int i = j = 2; // simple assignment
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-5 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs5 {
        static { j = 2; } // simple assignment
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-6 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs6 {
        static int i = new Object(){ int bar() { return j; } }.bar();
        // not in declaring class
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-7 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs7 {
        static { new Object(){ { j++; } }; } // not in declaring class
        static int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-8 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T8323lfs8 {
        static final int i = T8323lfs8.j; // not simple usage
        static final int j = 1;
    }
} PASS

tcltest::test 8.3.2.3-legal-forward-static-9 { Simple usage before
        declaration legal if 1. not in initializer; 2. simple assignment;
        or 3. not in declaring class } {
    empty_class T83231fs9 {
	static int j = i = 1;
	static final int i;
    }
} PASS

# see also tests for forward references in 9.3.1.
