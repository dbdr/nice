tcltest::test 16.8-constructor-1 { A blank final instance variable is DA
        after alternate constructor invocation } {
    empty_class T168c1 {
        final int i;
        T168c1() {
            this(1);
            i = 1;
        }
        T168c1(int i) {
            i = 1;
        }
    }
} FAIL

tcltest::test 16.8-constructor-2 { A blank final instance variable is DA
        after alternate constructor invocation } {
    empty_class T168c2 {
        final int i;
        T168c2() {
            this(1);
        }
        T168c2(int j) {
            i = 1;
        }
    }
} PASS

tcltest::test 16.8-constructor-3 { A blank final instance variable is DA
        after superconstructor if it is DA after initializers } {
    empty_class T168c3 {
        final int i;
        static {}
        Object o = new Object();
        T168c3() {
            i = 1;
        }
    }
} PASS

tcltest::test 16.8-constructor-4 { A blank final instance variable is DA
        after superconstructor if it is DA after initializers } {
    empty_class T168c4 {
        final int i;
        Object o = new Integer(i = 1);
        T168c4() {
            i = 1;
        }
    }
} FAIL

tcltest::test 16.8-constructor-5 { A blank final instance variable is DA
        after superconstructor if it is DA after initializers } {
    empty_class T168c5 {
        final int i;
        Object o = new Object();
        { i = 1; }
        T168c5() {
            i = 1;
        }
    }
} FAIL

