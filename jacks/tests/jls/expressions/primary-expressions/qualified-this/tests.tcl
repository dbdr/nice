tcltest::test 15.8.4-static-1 { Qualified this may not appear in static
        context } {
    empty_class T1584s1 {
        static int i = 1;
        static int j = T1584s1.this.i;
    }
} FAIL

tcltest::test 15.8.4-static-2 { Qualified this must name an enclosing class,
        which does not exist in a static context } {
    empty_class T1584s2 {
        static int i = 1;
        static class Nested {
            int j = T1584s2.this.i;
        }
    }
} FAIL

tcltest::test 15.8.4-name-1 { Qualified this must name an enclosing class, or
        the class itself } {
    empty_class T1584n1 {
        class Sub1 {
            int i;
        }
        class Sub2 extends Sub1 {
            int j = Sub1.this.i;
        }
    }
} FAIL

tcltest::test 15.8.4-name-2 { Qualified this must name an enclosing class, or
        the class itself } {
    empty_class T1584n2 {
        int i;
        class Sub {
            int j = T1584n2.this.i;
        }
    }
} PASS

tcltest::test 15.8.4-name-3 { Qualified this must name an enclosing class, or
        the class itself } {
    empty_class T1584n3 {
        int i;
        int j = T1584n3.this.i;
    }
} PASS

tcltest::test 15.8.4-syntax-1 { The type in the qualified this is not
        obscured } {
    empty_class T1584syn1 {
        int T1584syn1; // obscure the class name from normal expressions
        String s = T1584syn1.this.toString();
    }
} PASS

tcltest::test 15.8.4-syntax-2 { The type in qualified this may not be a
        primitive type } {
    empty_class T1584syn2 {
        Object o = int.this.toString();
    }
} FAIL

tcltest::test 15.8.4-syntax-3 { The type in qualified this may not be an
        array type } {
    empty_class T1584syn3 {
        Object o = int[].this.toString();
    }
} FAIL

tcltest::test 15.8.4-syntax-4 { The type in qualified this may not be an
        array type } {
    empty_class T1584syn4 {
        Object o = T1584syn4[].this.toString();
    }
} FAIL

tcltest::test 15.8.4-syntax-5 { The type in qualified this may not be null } {
    empty_class T1584syn5 {
        Object o = null.this.toString();
    }
} FAIL

tcltest::test 15.8.4-syntax-6 { The type in qualified this may not be void } {
    empty_class T1584syn6 {
        Object o = void.this.toString();
    }
} FAIL

