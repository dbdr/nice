tcltest::test 15.28-primitive-1 { literals are constant expressions } {
    switch_labels T1528p1 int {case 1: case '1':}
} PASS

tcltest::test 15.28-primitive-2 { literals are constant expressions } {
    constant_expression T1528p2 {1L == 1L} {1f == 1f} {1.0 == 1.0} {true}
} PASS

tcltest::test 15.28-primitive-3 { casts can form constant expressions } {
    constant_expression T1528p3 {(boolean) true} \
                                {(int) 1L == (float) 1.0} \
                                {(char) 0x61 == 'a'} \
                                {(char) 97.0D == 'a'} \
                                {(double) 'a' == 97.0D} \
                                {(int) 97L == (int) 'a'}
} PASS

tcltest::test 15.28-primitive-4 { Unary +, -, ~, ! form constant expressions } {
    constant_expression T1528p4 {+2 == 2} {-0x80000000 == 0x80000000} \
                                {~0 == 0xffffffff} {!false}
} PASS

tcltest::test 15.28-primitive-5 { ++ and -- are not constant expressions } {
    empty_class T1528p5 {
        void foo(int i) {
            switch (i) {
                case i++:
            }
        }
    }
} FAIL

tcltest::test 15.28-primitive-6 { ++ and -- are not constant expressions } {
    empty_class T1528p6 {
        void foo(int i) {
            switch (i) {
                case i--:
            }
        }
    }
} FAIL

tcltest::test 15.28-primitive-7 { *, /, and % form constant expressions } {
    constant_expression T1528p7 {5 * 3 == 15} {5 / 3 == 1} {5 % 3 == 2}
} PASS

tcltest::test 15.28-primitive-8 { + and - form constant expressions } {
    constant_expression T1528p8 {1 + 2 == 3} {1 - 2 == -1}
} PASS

tcltest::test 15.28-primitive-9 { <<, >>, and >>> form constant expressions } {
    constant_expression T1528p9 {2 << 1 == 4} {2 >> 1 == 1} {-2 >>> 1 == 0x7fffffff}
} PASS

tcltest::test 15.28-primitive-10 { <, <=, >, and >= form constant expressions } {
    constant_expression T1528p10 {1 < 2} {2 <= 2} {2 > 1} {1 >= 1}
} PASS

tcltest::test 15.28-primitive-11 { == and != form constant expressions } {
    constant_expression T1528p11 {1 == 1} {1 != 2} \
                                 {true == true} {true != false} \
                                 {'a' == 'a'} {'a' != 'b'}
} PASS

tcltest::test 15.28-primitive-12 { bitwise &, ^, and | form constant expressions } {
    constant_expression T1528p12 {(0xaa & 0xa5) == 0xa0} \
                                 {(0xaa ^ 0xa5) == 0x0f} \
                                 {(0xaa | 0xa5) == 0xaf}
} PASS

tcltest::test 15.28-primitive-13 { logical &, ^, and | form constant expressions } {
    constant_expression T1528p13 {true & true} {true ^ false} {false | true}
} PASS

tcltest::test 15.28-primitive-14 { && and || form constant expressions } {
    constant_expression T1528p14 {true && true} {true || false}
} PASS

tcltest::test 15.28-primitive-15 { short-circuited portion of && or || must
        still be constant expressions } {
    empty_main T1528p15 {
        boolean t = true, f = false;
        switch (args.length) {
            case 0:
            case ((false && t) ? 0 : 1):
            case ((true || f) ? 2 : 0):
        }
    }
} FAIL

tcltest::test 15.28-primitive-16 { ?: forms a constant expression } {
    constant_expression T1528p16 {true ? true : false}
} PASS

tcltest::test 15.28-primitive-17 { unused portion of ?: must still
        be a constant expression } {
    empty_main T1528p17 {
        int i1 = 1, i2 = 2;
        switch (args.length) {
            case 0:
            case (true ? 1 : i1):
            case (false ? i2 : 2):
        }
    }
} FAIL


# simple variable names

tcltest::test 15.28-simple-name-1 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sn1 {
        static final int i = 1;
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-2 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sn2 {
        final int i = 1;
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-3 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sn3 {
        void foo (int j) {
            final int i = 1;
            switch (j) {
                case i:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-4 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sn4 {
        static final int i = 1 + 1;
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-5 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sn5 {
        final int i = 1 - 1;
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-6 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sn6 {
        void foo (int j) {
            final int i = true ? 1 : 0;
            switch (j) {
                case i:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-7 { a final variable initialized
        by a final variable that is initialized by constant expression
        is a constant expression } {
    empty_class T1528sn7 {
        static final int i1 = 1;
        static final int i2 = i1;
        void foo (int j) {
            switch (j) {
                case i2:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-8 { a final variable initialized
        by a final variable that is initialized by constant expression
        is a constant expression } {
    empty_class T1528sn8 {
        final int i1 = 2 / 1;
        final int i2 = i1;
        void foo (int j) {
            switch (j) {
                case i2:
            }
        }
    }
} PASS

tcltest::test 15.28-simple-name-9 { a final variable initialized
        by a final variable that is initialized by constant expression
        is a constant expression } {
    empty_class T1528sn9 {
        void foo (int j) {
            final int i1 = 2 * 2;
            final int i2 = i1;
            switch (j) {
                case i2:
            }
        }
    }
} PASS


# simple variable names that are not constant expressions

tcltest::test 15.28-nonconst-simple-name-1 { a non-final variable initialized
        by a constant expression is not a constant expression } {
    empty_class T1528nsn1 {
        int i = 1;
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-name-2 { a final variable initialized
        by a non-final variable is not a constant expression } {
    empty_class T1528nsn2 {
        int i1 = 1;
        final int i2 = i1;
        void foo (int j) {
            switch (j) {
                case i2:
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-name-3 { a final variable initialized
        by a non-constant expression is not a constant expression } {
    empty_class T1528nsn3 {
        int i1 = 0;
        final int i2 = i1++;
        void foo (int j) {
            switch (j) {
                case i2:
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-name-4 { an argument variable with
        the final modifier is not a constant expression } {
    empty_class T1528nsn4 {
        void foo (final int i) {
            switch (i) {
                case i:
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-name-5 { a final variable initialized by
        an argument variable with the final modifier is not a constant expression } {
    empty_class T1528nsn5 {
        void foo (final int i) {
            final int j = i;
            switch (i) {
                case j:
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-name-6 { a final variable initialized by
        a function invocation is not a constant expression } {
    empty_class T1528nsn6 {
        void foo (int i) {
            final int j = Integer.parseInt("1");
            switch (i) {
                case j:
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-name-7 { a final variable initialized by
        a ternary conditional operator with a non-constant value is not
        a constant expression } {
    empty_class T1528nsn7 {
        void foo (int i) {
            final int j = true ? 1 : Integer.parseInt("1");
            switch (i) {
                case j:
            }
        }
    }
} FAIL


# FIXME: Is this one really covered by the JLS?

tcltest::test 15.28-uninitialized-simple-name-1 { a final variable that does
        not have an initializer is not a constant expression } {
    empty_class T1528usn1 {
        static final int i;
        static { i = 1; }
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} FAIL

tcltest::test 15.28-uninitialized-simple-name-2 { a final variable that does
        not have an initializer is not a constant expression } {
    empty_class T1528usn2 {
        static final int i;
        static { i = Integer.parseInt("1"); }
        void foo (int j) {
            switch (j) {
                case i:
            }
        }
    }
} FAIL

tcltest::test 15.28-uninitialized-simple-name-3 { a final variable that does
        not have an initializer is not a constant expression } {
    empty_class T1528usn3 {
        void foo (int j) {
            final int i;
            i = 1;
            switch (j) {
                case i:
            }
        }
    }
} FAIL

tcltest::test 15.28-uninitialized-simple-name-4 { a final variable that does
        not have an initializer is not a constant expression } {
    empty_class T1528usn4 {
        void foo (int j) {
            final int i;
            i = Integer.parseInt("1");
            switch (j) {
                case i:
            }
        }
    }
} FAIL



# casting simple variable names

tcltest::test 15.28-cast-simple-name-1 { a final variable initialized
        by a constant expression and then cast to a primitive is a
        constant expression } {
    empty_class T1528csn1 {
        final long l1 = 1L;
        final long l2 = Long.MAX_VALUE;
        void foo (int j) {
            switch (j) {
                case (int) l1:
                case (int) l2:
            }
        }
    }
} PASS

tcltest::test 15.28-cast-simple-name-2 { a final variable initialized
        by a constant expression and then cast to a primitive is a
        constant expression } {
    empty_class T1528csn2 {
        final boolean t = true;
        void foo (int j) {
            switch (j) {
                case 0:
                case ((boolean) t ? 1 : 0):
            }
        }
    }
} PASS

tcltest::test 15.28-cast-simple-name-3 { any number of casts
        can be applied to a constant expression } {
    empty_class T1528csn3 {
        final byte b = (byte) (float) 1.0D;
        final char c = (char) b;
        final short s = (short) c;
        final int i = (int) s;
        final long l = (long) i;
        void foo (int j) {
            switch (j) {
                case 0:
                case (((double) l == 1.0D) ? (int) l : 0):
                case 2:
            }
        }
    }
} PASS


# fully qualified variable name

tcltest::test 15.28-qualified-name-1 { qualified references
        of final constant fields are constant } {
    empty_class T1528qn1 {
        static final int i = 1;
        void foo(int j) {
            switch (j) {
                case T1528qn1.i:
                case Integer.MAX_VALUE:
            }
        }
    }
} PASS

tcltest::test 15.28-qualified-name-2 { qualified references
        of final constant fields are constant } {
    empty_class T1528qn2 {
        static int i = 1;
        void foo(int j) {
            switch (j) {
                case T1528qn2.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-3 { qualified references
        of final constant fields are constant } {
    empty_class T1528qn3 {
        static int i = 1;
        static final int i1 = i;
        void foo(int j) {
            switch (j) {
                case T1528qn3.i1:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-4 { qualified references
        of final constant fields are constant } {
    empty_class T1528qn4 {
        static final int i;
        static {i = 1;}
        void foo(int j) {
            switch (j) {
                case T1528qn4.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-5 { qualified references
        of final constant fields must fit the form TypeName.Identifier } {
    empty_class T1528qn5 {
        static final T1528qn5 t = new T1528qn5();
        final int i = 1;
        void foo(int j) {
            switch (j) {
                case T1528qn5.t.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-6 { qualified references
        of final constant fields must fit the form TypeName.Identifier } {
    empty_class T1528qn6 {
        static final T1528qn6 t = new T1528qn6();
        final int i = 1;
        void foo(int j) {
            switch (j) {
                case t.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-7 { qualified references
        of final constant fields must fit the form TypeName.Identifier } {
    empty_class T1528qn7 {
        static final T1528qn7 t = new T1528qn7();
        final int i = 1;
        void foo(int j) {
            switch (j) {
                case T1528qn7.t.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-8 { qualified references
        of final constant fields must fit the form TypeName.Identifier } {
    empty_class T1528qn8 {
        static final T1528qn8 t = new T1528qn8();
        static final int i = 1;
        void foo(int j) {
            switch (j) {
                case t.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-9 { qualified references
        of final constant fields must fit the form TypeName.Identifier } {
    empty_class T1528qn9 {
        static final int i = 1;
        void foo(int j) {
            final T1528qn9 t = new T1528qn9();
            switch (j) {
                case t.i:
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-name-10 { qualified references
        of final constant fields must fit the form TypeName.Identifier } {
    empty_class T1528qn10 {
        final int i = 1;
        void foo(int j) {
            final T1528qn10 t = new T1528qn10();
            switch (j) {
                case t.i:
            }
        }
    }
} FAIL


# String constants

tcltest::test 15.28-string-1 { literals are constant expressions } {
    constant_expression T1528s1 {"" == ""} {"hello, world" == "hello, world"}
} PASS

tcltest::test 15.28-string-2 { literals are constant expressions } {
    constant_expression T1528s2 {"" + 1 == "1"} \
                                {"" + 1L == "1"} \
                                {"" + 1f == "1.0"} \
                                {"" + 1.0 == "1.0"} \
                                {"" + '1' == "1"} \
                                {"" + true == "true"}
} PASS

tcltest::test 15.28-string-3 { casts to type String are
        constant expressions } {
    constant_expression T1528s3 {(String) "" == ""}
} PASS

tcltest::test 15.28-string-4 { casts to primitive types are
        constant expressions } {
    constant_expression T1528s3 {"" + (boolean) true == "true"} \
                                {"" + (int) 1.5 == "1"}
} PASS

tcltest::test 15.28-string-5 { Unary +, -, ~, ! form constant expressions } {
    constant_expression T1528s5 {"" + +2 == "2"}\
                                {"" + -0x80000000 == "-2147483648"} \
                                {"" + ~0 == "-1"} \
                                {"" + !false == "true"}
} PASS

tcltest::test 15.28-string-6 { *, /, and % form constant expressions } {
    constant_expression T1528s6 {"" + 5 * 3 == "15"} \
                                {"" + 5 / 3 == "1"} \
                                {"" + 5 % 3 == "2"}
} PASS

tcltest::test 15.28-string-7 { String + forms constant expressions } {
    constant_expression T1528s7 {"" + "test" + "ing" == "testing"} \
                                {"a" + "b" == "\u0061b"} \
                                {"" + ("1" + '2' + 3 +4L) == "1234"}
} PASS

tcltest::test 15.28-string-8 { + and - form constant expressions } {
    constant_expression T1528s8 {"" + (1 + 2) == "3"} \
                                {"" + (1 - 2) == "-1"}
} PASS

tcltest::test 15.28-string-9 { <<, >>, and >>> form constant expressions } {
    constant_expression T1528s9 {"" + (2 << 1) == "4"} \
                                {"" + (2 >> 1) == "1"} \
                                {"" + (-2 >>> 1) == "2147483647"}
} PASS

tcltest::test 15.28-string-10 { <, <=, >, and >= form constant expressions} {
    constant_expression T1528s10 {"" + (1 < 2) == "true"} \
                                 {"" + (2 <= 2) == "true"} \
                                 {"" + (2 > 1) == "true"} \
                                 {"" + (1 >= 1) == "true"}
} PASS

tcltest::test 15.28-string-11 { == and != form constant expressions } {
    constant_expression T1528s11 {"1" == "1"} {"1" != "2"}
} PASS

tcltest::test 15.28-string-12 { bitwise &, ^, and | form constant expressions } {
    constant_expression T1528s12 {"" + (0xaa & 0xa5) == "160"} \
                                 {"" + (0xaa ^ 0xa5) == "15"} \
                                 {"" + (0xaa | 0xa5) == "175"}
} PASS

tcltest::test 15.28-string-13 { logical &, ^, and | form constant expressions} {
    constant_expression T1528s13 {"" + (true & true) == "true"} \
                                 {"" + (true ^ false) == "true"} \
                                 {"" + (false | true) == "true"}
} PASS

tcltest::test 15.28-string-14 { && and || form constant expressions } {
    constant_expression T1528s14 {"" + (true && true) == "true"} \
                                 {"" + (true || false) == "true"}
} PASS

tcltest::test 15.28-string-15 { ?: forms constant expressions } {
    constant_expression T1528s15 {"" + (true ? 1 : 2) == "1"}
} PASS

tcltest::test 15.28-string-16 { ?: forms constant expressions } {
    constant_expression T1528s16 {(true ? "1" : "") == "1"}
} PASS

tcltest::test 15.28-string-17 { ?: forms constant expressions } {
    constant_expression T1528s17 {"" + (true ? "foo" : "bar") == "foo"} \
                                 {"" + (false ? "foo" : "bar") == "bar"} \
} PASS

# simple string variable names

tcltest::test 15.28-simple-namestr-1 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sns1 {
        void foo (int j) {
            final String s = "1";
            switch (j) {
                case 0:
                case ((s == "1") ? 1 : 0):
            }
        }
    }
} PASS

tcltest::test 15.28-simple-namestr-2 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sns2 {
        void foo (int j) {
            final String s = "1" + "2";
            switch (j) {
                case 0:
                case ((s == "12") ? 1 : 0):
            }
        }
    }
} PASS

tcltest::test 15.28-simple-namestr-3 { a final variable initialized
        by a constant expression is a constant expression } {
    empty_class T1528sns3 {
        void foo (int j) {
            final String s = "1" + (int) 2.0D;
            switch (j) {
                case 0:
                case ((s == "12") ? 1 : 0):
            }
        }
    }
} PASS

tcltest::test 15.28-simple-namestr-4 { a final variable initialized
        by a final variable that is initialized by constant expression
        is a constant expression } {
    empty_class T1528sns4 {
        void foo (int j) {
            final String s1 = "1";
            final String s2 = s1;
            switch (j) {
                case 0:
                case ((s2 == "1") ? 1 : 0):
            }
        }
    }
} PASS



# simple string variable names that are not constant expressions

tcltest::test 15.28-nonconst-simple-namestr-1 { a non-final variable initialized
        by a constant expression is not a constant expression } {
    empty_class T1528nsns1 {
        String s = "1";
        void foo (int j) {
            switch (j) {
                case 0:
                case ((s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-namestr-2 { a final variable initialized
        by a non-final variable is not a constant expression } {
    empty_class T1528nsns2 {
        String s1 = "1";
        final String s2 = s1;
        void foo (int j) {
            switch (j) {
                case 0:
                case ((s2 == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-nonconst-simple-namestr-3 { a final variable initialized
        by a function invocation is not a constant expression } {
    empty_class T1528nsns3 {
        final String s = String.valueOf(0L);
        void foo (int j) {
            switch (j) {
                case 0:
                case ((s == "0") ? 1 : 0):
            }
        }
    }
} FAIL



# fully qualified string name

tcltest::test 15.28-qualified-namestr-1 { qualified
        references of final constant fields are constant } {
    empty_class T1528qns1 {
        static final String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns1.s == "1") ? 1 : 0):
            }
        }
    }
} PASS

tcltest::test 15.28-qualified-namestr-2 { qualified
        references of final constant fields are constant } {
    empty_class T1528qns2 {
        static String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns2.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-3 { qualified
        references of final constant fields are constant } {
    empty_class T1528qns3 {
        static String s = "1";
        static final String s1 = s;
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns3.s1 == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-4 { qualified
        references of final constant fields are constant } {
    empty_class T1528qns4 {
        static final String s;
        static {s = "1";}
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns4.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-5 { qualified references
        of final constant must fit the form TypeName.Identifier } {
    empty_class T1528qns5 {
        static final T1528qns5 t = new T1528qns5();
        final String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns5.t.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-6 { qualified references
        of final constant must fit the form TypeName.Identifier } {
    empty_class T1528qns6 {
        static final T1528qns6 t = new T1528qns6();
        final String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((t.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-7 { qualified references
        of final constant must fit the form TypeName.Identifier } {
    empty_class T1528qns7 {
        static final T1528qns7 t = new T1528qns7();
        static final String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((t.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-8 { qualified references
        of final constant must fit the form TypeName.Identifier } {
    empty_class T1528qns8 {
        static final T1528qns8 t = new T1528qns8();
        static final String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((t.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-9 { qualified references
        of final constant must fit the form TypeName.Identifier } {
    empty_class T1528qns9 {
        static final String s = "1";
        void foo(int j) {
            final T1528qns9 t = new T1528qns9();
            switch (j) {
                case 0:
                case ((t.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-qualified-namestr-10 { qualified references
        of final constant must fit the form TypeName.Identifier } {
    empty_class T1528qns10 {
        final String s = "1";
        void foo(int j) {
            final T1528qns10 t = new T1528qns10();
            switch (j) {
                case 0:
                case ((t.s == "1") ? 1 : 0):
            }
        }
    }
} FAIL



# a type other than string does not a constant expression make
#
# these are highly debatable because of the
# note in the JLS about casting to String
# and the fact that the type of a final
# variable is not mentioned in 15.28.
# Also, the only valid cast is (String) ""
# which seems very odd.

tcltest::test 15.28-notstring-1 { casting a String to anything
        other than string makes is not a constant expression,
        casting back to String does not reverse the process } {
    constant_expression T1528nots1 {(String) (Object) "" == ""}
} FAIL

tcltest::test 15.28-notstring-2 { anything other than type String
        is not a constant expression } {
    empty_class T1528nots2 {
        final Object o = "1";
        void foo (int j) {
            switch (j) {
                case 0:
                case ((o == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-notstring-3 { anything other than type String
        is not a constant expression, casting to String does not help } {
    empty_class T1528nots3 {
        final Object o = "1";
        void foo (int j) {
            switch (j) {
                case 0:
                case (((String) o == "1") ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-notstring-4 { anything other than type String
        is not a constant expression } {
    constant_expression T1528nots4 {"" + ((Object) "") == ""}
} FAIL

# null literal

tcltest::test 15.28-null-1 { The null literal is NOT a compile-time constant } {
    constant_expression T1528null1 {null == null}
} FAIL

tcltest::test 15.28-null-2 { The null literal is NOT a compile-time constant } {
    constant_expression T1528null2 {null != ""}
} FAIL

tcltest::test 15.28-null-3 { The null literal is NOT a compile-time constant } {
    constant_expression T1528null3 {"" + null == "null"}
} FAIL

tcltest::test 15.28-null-4 { The null literal is NOT a compile-time constant } {
    constant_expression T1528null4 {(String)null + (String)null == "nullnull"}
} FAIL

tcltest::test 15.28-null-5 { The null literal is NOT a compile-time constant } {
    empty_class T1528null5 {
        static final String s = null;
        void foo(int i) {
            switch (i) {
                case 0:
                case (("" != s) ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-null-6 { The null literal is NOT a compile-time constant } {
    empty_class T1528null6 {
        void foo(int i) {
            final String s = null;
            switch (i) {
                case 0:
                case (("" != s) ? 1 : 0):
            }
        }
    }
} FAIL

tcltest::test 15.28-null-7 { The null literal is NOT a compile-time constant } {
    constant_expression T1528null7 {"" + (true ? null : null) == "null"}
} FAIL


tcltest::test 15.28-example-1 { Example constants } {
    constant_expression T1528e1 {true} {(short)(1*2*3*4*5*6) == 720} \
            {Integer.MAX_VALUE / 2 == 0x3fffffff} \
            {2.0 * Math.PI == 6.283185307179586}
} PASS

tcltest::test 15.28-example-2 { Example constant } {
    constant_expression T1528e2 {"The integer " + Long.MAX_VALUE +
    " is mighty big." == "The integer 9223372036854775807 is mighty big."}
} PASS


# instanceof operator

tcltest::test 15.28-instanceof-1 { The expression argument of instanceof may be constant } {
    constant_expression T1528i1 {null instanceof Object}
} FAIL

tcltest::test 15.28-instanceof-2 { instanceof does not form a constant expression } {
    constant_expression T1528i2 {"" instanceof String}
} FAIL

tcltest::test 15.28-instanceof-3 { instanceof does not form a constant expression } {
    constant_expression T1528i3 {"" + (null instanceof Object) == "false"}
} FAIL

tcltest::test 15.28-instanceof-4 { instanceof does not form a constant expression } {
    constant_expression T1528i3 {"" + ("" instanceof String) == "true"}
} FAIL

# division by zero

tcltest::test 15.28-div0-1 { divide by zero is not a compile time constant
        because it completes abruptly } {
    constant_expression T1528div01 {"" + (5/0) == "0"}
} FAIL

tcltest::test 15.28-div0-2 { divide by zero is not a compile time constant
        because it completes abruptly } {
    constant_expression T1528div02 {"" + (0%0) == "0"}
} FAIL

tcltest::test 15.28-div0-3 { divide by zero is not a compile time constant
        because it completes abruptly } {
    constant_expression T1528div03 {"" + (8/(1-1)) == "0"}
} FAIL

