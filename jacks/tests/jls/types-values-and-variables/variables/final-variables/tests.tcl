tcltest::test 4.5.4-instance-1 { final variable must be definitely assigned } {
    empty_class T454i1 {
        final int val;
    }
} FAIL

tcltest::test 4.5.4-instance-2 { final variable must be definitely assigned } {
    empty_class T454i2 {
        final int val = 0;
        final String str = null;
    }
} PASS

tcltest::test 4.5.4-instance-3 { final variable must be definitely assigned } {
    empty_class T454i3 {
        final int val;
        final String str;

        T454i3() {
            val = 0;
            str = null;
        }
    }
} PASS

tcltest::test 4.5.4-instance-4 { final variable must be definitely assigned } {
    empty_class T454i4 {
        final int val;
        final String str;

        {
            val = 0;
            str = null;
        }
    }
} PASS

tcltest::test 4.5.4-instance-5 { final variable can only be assigned to once } {
    empty_class T454i5 {
        final int val = 0;
        final String str = null;
        
        T454i5() {
            val = 1;
            str = "";
        }
    }
} FAIL

tcltest::test 4.5.4-instance-6 { final variable can only be assigned to once } {
    empty_class T454i6 {
        final int val = 0;
        final String str = null;
        
        {
            val = 1;
            str = "";
        }
    }
} FAIL

tcltest::test 4.5.4-instance-7 { final variable can only be assigned to once } {
    empty_class T454i7 {
        final int val;
        final String str;
        {
            val = 0;
            str = null;
        }
        T454i7() {
            val = 1;
            str = "";
        }
    }
} FAIL

tcltest::test 4.5.4-instance-8 { final variable must be assigned before it
        can be used } {
    empty_class T454i8 {
        final int val;
        
        T454i8() {
            int tmp = val;
            val = 1;
        }
    }
} FAIL

tcltest::test 4.5.4-instance-9 { blank final variable must be assigned in
        every constructor } {
    empty_class T454i9 {
        final int val;
        final String str;

        T454i9() { val = 0; }
        T454i9(int foo) { str = null; }
    }
} FAIL

tcltest::test 4.5.4-instance-10 { final variable can only be assigned to once } {
    empty_class T454i10 {
        final int val = 0;
        final String str = null;

        T454i10() {
            val = 1 + val;
            str = "foo" + str;
        }
    }
} FAIL

tcltest::test 4.5.4-static-1 { final variable must be definitely assigned } {
    empty_class T454s1 {
        static final int val;
        static final String str;
    }
} FAIL

tcltest::test 4.5.4-static-2 { final variable must be definitely assigned } {
    empty_class T454s2 {
        static final int val = 0;
        static final String str = null;
    }
} PASS

tcltest::test 4.5.4-static-3 { final variable must be definitely assigned } {
    empty_class T454s3 {
        static final int val;
        static final String str;
        static {
            val = 0;
            str = null;
        }
    }
} PASS

tcltest::test 4.5.4-static-4 { final variable can only be assigned to once } {
    empty_class T454s4 {
        static final int val = 0;
        static final String str = null;
        static {
            val = 1;
            str = "";
        }
    }
} FAIL

tcltest::test 4.5.4-static-5 { final variable must be assigned before it
        can be used } {
    empty_class T454i5 {
        static final int val;
        static {
            int i = val;
            val = 1;
        }
    }
} FAIL

tcltest::test 4.5.4-static-6 { final variable can only be assigned to once } {
    empty_class T454s6 {
        static final int val = 0;
        static final String str = null;

        static void main(String args[]) {
            val = 1 + val;
            str = "foo" + str;
        }
    }
} FAIL

tcltest::test 4.5.4-reference-1 { a final variable can reference an Object, the
        reference can not change but the object being referenced can change } {
    empty_class T454r1 {
        final StringBuffer sbuf = new StringBuffer();
        
        void append() {
            sbuf.append("nuggy");
        }
    }
} PASS

tcltest::test 4.5.4-reference-2 { a final variable can reference an array, the
        elements of the array can be modified } {
    empty_class T454r2 {
        final Object[] objarr = {new Object(), new Object()};

        void swap() {
            Object tmp = objarr[0];
            objarr[0] = objarr[1];
            objarr[1] = tmp;
        }
    }
} PASS

tcltest::test 4.5.4-9 { blank final variable must be assigned in every 
        constructor } {
    empty_class T4549 {
        final int val;
        T4549() { val = 0; }
        T4549(int foo) { this(); }
    }
} PASS

tcltest::test 4.5.4-10 { blank final variable must be assigned in every 
        constructor } {
    empty_class T45410 {
        final int val;
        T45410() { val = 0; }
        T45410(int foo) { this(); val = 1;}
    }
} FAIL

tcltest::test 4.5.4-local-1 { final may only be assigned once } {
    empty_main T454l1 {
        final int i;
        i = 1;
        i = 2;
    }
} FAIL

tcltest::test 4.5.4-local-2 { final may only be assigned once } {
    empty_main T454l2 {
        final int i = 1;
        i = 2;
    }
} FAIL

tcltest::test 4.5.4-local-3 { final variable can only be assigned to once } {
    empty_main T454l3 {
        final int val = 0;
        final String str = null;
        val = 1 + val;
        str = "foo" + str;
    }
} FAIL

tcltest::test 4.5.4-parameter-1 { final parameter may not be assigned } {
    empty_class T454p1 {
        void foo(final int i) {
            i = 1;
        }
    }
} FAIL

tcltest::test 4.5.4-parameter-2 { final parameter may not be assigned } {
    empty_main T454p2 {
        try {
            throw new Exception();
        } catch (final Exception e) {
            e = new Exception();
        }
    }
} FAIL
