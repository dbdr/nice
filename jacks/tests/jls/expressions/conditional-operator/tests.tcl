tcltest::test 15.25-basic-1 { ?: uses a boolean value to decide which
        of two other expressions should be evaluated } {
    empty_main T1525b1 {String s = true ? "1" : "2";}
} PASS

tcltest::test 15.25-basic-2 { ?: uses a boolean value to decide which
        of two other expressions should be evaluated } {
    empty_main T1525b2 {String s = false ? "1" : "2";}
} PASS

# FIXME: test that ?: is right-associative -> ?b:c?d:e?f:g == a?b:(c?d:(e?f:g))

tcltest::test 15.25-syntax-1 { conditional operator has three operands } {
    empty_main T1525s1 {String s = false ? : "2";}
} FAIL

tcltest::test 15.25-syntax-2 { conditional operator has three operands } {
    empty_main T1525s2 {String s = false ? "1" : ;}
} FAIL

tcltest::test 15.25-syntax-3 { conditional operator has three operands } {
    empty_main T1525s3 {String s = false ? "1" "2" ;}
} FAIL

tcltest::test 15.25-syntax-4 { conditional operator has three operands } {
    empty_main T1525s4 {String s = ? "1" : "2" ;}
} FAIL

tcltest::test 15.25-syntax-5 { conditional operator has three operands } {
    empty_main T1525s5 {String s = true "1" : "2" ;}
} FAIL


tcltest::test 15.25-not-boolean-1 { first expression must be boolean type } {
    empty_main T1525nb1 {String s = (byte) 1 ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-2 { first expression must be boolean type } {
    empty_main T1525nb2 {String s = (short) 1 ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-3 { first expression must be boolean type } {
    empty_main T1525nb3 {String s = '1' ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-4 { first expression must be boolean type } {
    empty_main T1525nb4 {String s = 1 ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-5 { first expression must be boolean type } {
    empty_main T1525nb5 {String s = 1L ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-6 { first expression must be boolean type } {
    empty_main T1525nb6 {String s = 1F ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-7 { first expression must be boolean type } {
    empty_main T1525nb7 {String s = 1D ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-8 { first expression must be boolean type } {
    empty_main T1525nb8 {String s = "true" ? "1" : "2";}
} FAIL

tcltest::test 15.25-not-boolean-9 { first expression must be boolean type } {
    empty_main T1525nb9 {String s = new Object() ? "1" : "2";}
} FAIL



tcltest::test 15.25-operand-type-1 { second and third operands may
        be boolean } {
    empty_main T1525ot1 {boolean b = true ? true : false;}
} PASS

tcltest::test 15.25-operand-type-2 { second and third operands may be null } {
    empty_main T1525ot2 {String s = true ? null : null;}
} PASS

tcltest::test 15.25-operand-type-3 { second or third operands may be null } {
    empty_main T1525ot3 {String s = true ? "" : null;}
} PASS

tcltest::test 15.25-operand-type-4 { second or third operands may be null } {
    empty_main T1525ot4 {String s = true ? null : "";}
} PASS

# void

tcltest::test 15.25-operand-type-void-1 { operand may not be void type } {
    empty_class T1525otv1 {
        void foo() {}
        void bar() {
            String s = true ? foo() : foo();
        }
    }
} FAIL

tcltest::test 15.25-operand-type-void-2 { operand may not be void type } {
    empty_class T1525otv2 {
        void foo() {}
        void bar() {
            String s = true ? foo() : "";
        }
    }
} FAIL

tcltest::test 15.25-operand-type-void-3 { operand may not be void type } {
    empty_class T1525otv3 {
        void foo() {}
        void bar() {
            String s = true ? "" : foo();
        }
    }
} FAIL

tcltest::test 15.25-operand-type-void-4 { operand may not be void type } {
    empty_class T1525otv4 {
        void foo() {}
        void bar() {
            String s = true ? null : foo();
        }
    }
} FAIL

tcltest::test 15.25-operand-type-void-5 { operand may not be void type } {
    empty_class T1525otv5 {
        void foo() {}
        void bar() {
            String s = true ? foo() : null;
        }
    }
} FAIL

# Note: avoid using the true and false in the
# first operand in most tests because of a bug
# in Javac which ignores the other operand.

# byte

tcltest::test 15.25-byte-operand-type-mismatch-1 { if one operand is of
        type byte and the other is a constant expression of type int
        whose value is representable as a byte, then the type of the
        conditional expression is byte } {
    empty_class T1525botm1 {
        void foo(byte b) {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? (byte) 0 : 0);
        }
    }
} PASS

tcltest::test 15.25-byte-operand-type-mismatch-2 { if one operand is of
        type byte and the other is a constant expression of type int
        whose value is representable as a byte, then the type of the
        conditional expression is byte, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525botm2 {
        void foo(byte b) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            int i = 0;
            foo(bool ? (byte) 0 : i);
        }
    }
} PASS

tcltest::test 15.25-byte-operand-type-mismatch-3 { if one operand is of
        type byte and the other is a constant expression of type int
        whose value is representable as a byte, then the type of the
        conditional expression is byte, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525botm3 {
        void foo(byte b) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            foo(bool ? (byte) 0 : 128);
        }
    }
} PASS

tcltest::test 15.25-byte-operand-type-mismatch-4 { if one operand is of
        type byte and the other is a constant expression of type int
        whose value is representable as a byte, then the type of the
        conditional expression is byte, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525botm4 {
        void foo(byte b) throws Exception {}
        void foo(int i) {}

        void bar() {
            foo(true ? (byte) 0 : 128);
        }
    }
} PASS

# short

tcltest::test 15.25-short-operand-type-mismatch-1 { if one operand is of
        type byte and the other is of type short, then the type of the
        conditional expression is short } {
    empty_class T1525sotm1 {
        void foo(short s) {}
        void foo(byte b) throws Exception {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? (byte) 0 : (short) 0);
        }
    }
} PASS

tcltest::test 15.25-short-operand-type-mismatch-2 { if one operand is of
        type short and the other is a constant expression of type int
        whose value is representable as a short, then the type of the
        conditional expression is short } {
    empty_class T1525sotm2 {
        void foo(short s) {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? (short) 0 : 0);
        }
    }
} PASS

tcltest::test 15.25-short-operand-type-mismatch-3 { if one operand is of
        type short and the other is a constant expression of type int
        whose value is representable as a short, then the type of the
        conditional expression is short, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525sotm3 {
        void foo(short s) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            int i = 0;
            foo(bool ? (short) 0 : i);
        }
    }
} PASS

tcltest::test 15.25-short-operand-type-mismatch-4 { if one operand is of
        type short and the other is a constant expression of type int
        whose value is representable as a short, then the type of the
        conditional expression is short, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525sotm4 {
        void foo(short s) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            foo(bool ? (short) 0 : 32768);
        }
    }
} PASS

tcltest::test 15.25-short-operand-type-mismatch-5 { if one operand is of
        type short and the other is a constant expression of type int
        whose value is representable as a short, then the type of the
        conditional expression is short, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525sotm5 {
        void foo(short s) throws Exception {}
        void foo(int i) {}

        void bar() {
            foo(true ? (short) 0 : 32768);
        }
    }
} PASS

# char

tcltest::test 15.25-char-operand-type-mismatch-1 { if one operand is of
        type char and the other is a constant expression of type int
        whose value is representable as a char, then the type of the
        conditional expression is char } {
    empty_class T1525cotm1 {
        void foo(char c) {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? '0' : 0);
        }
    }
} PASS

tcltest::test 15.25-char-operand-type-mismatch-2 { if one operand is of
        type char and the other is a constant expression of type int
        whose value is representable as a char, then the type of the
        conditional expression is char, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525cotm2 {
        void foo(char c) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            int i = 0;
            foo(bool ? '0' : i);
        }
    }
} PASS

tcltest::test 15.25-char-operand-type-mismatch-3 { if one operand is of
        type char and the other is a constant expression of type int
        whose value is representable as a char, then the type of the
        conditional expression is char, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525cotm3 {
        void foo(char c) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            foo(bool ? '0' : -1);
        }
    }
} PASS

tcltest::test 15.25-char-operand-type-mismatch-4 { if one operand is of
        type char and the other is a constant expression of type int
        whose value is representable as a char, then the type of the
        conditional expression is char, Otherwise, binary numeric
        promotion is applied to the operand types } {
    empty_class T1525cotm4 {
        void foo(char c) throws Exception {}
        void foo(int i) {}

        void bar() {
            foo(true ? '0' : -1);
        }
    }
} PASS

# no special case

tcltest::test 15.25-operand-type-mismatch-1 { binary numeric promotion
        is applied to mismatches primitive operand types if no
        special cases apply } {
    empty_main T1525otm1 {float f = true ? 0 : 0F;}
} PASS

tcltest::test 15.25-operand-type-mismatch-2 { binary numeric promotion
        is applied to mismatches primitive operand types if no
        special cases apply } {
    empty_main T1525otm2 {double d = false ? 'c' : 0D;}
} PASS

tcltest::test 15.25-operand-type-mismatch-3 { binary numeric promotion
        is applied to mismatches primitive operand types if no
        special cases apply } {
    empty_main T1525otm3 {long l = true ? (byte) 0 : 0L;}
} PASS

tcltest::test 15.25-operand-type-mismatch-4 { reference operand types
        must match or they must support assignment conversion } {
    empty_main T1525otm4 \
        {Object o = true ? new Integer(0) : new Double(0D);}
} FAIL

tcltest::test 15.25-operand-type-mismatch-5 { reference operand types
        must match or they must support assignment conversion } {
    empty_class T1525otm5 {
        class C1 {}
        class C2 extends C1 {}
        void foo() {
            Object o = true ? new C1() : new C2();
        }
    }
} PASS

tcltest::test 15.25-operand-type-mismatch-6 { primitive and reference
        types don't mix } {
    empty_main T1525otm6 {Object o = true ? "" : 42;}
} FAIL

tcltest::test 15.25-operand-type-mismatch-7 { primitive and reference
        types don't mix } {
    empty_main T1525otm7 {Object o = true ? 42 : "";}
} FAIL

tcltest::test 15.25-operand-type-mismatch-8 { primitive and reference
        types don't mix, even in a method invocation } {
    empty_class T1525otm8 {
        void foo(String s) {}
        void foo(int i) {}
        void bar() {
            foo(true ? "" : 42);
        }
    }
} FAIL


# FIXME: Check that evaluation of operand expressions is done at runtime.
# For example, an expression with a side effect could be used to check
# that a given branch is getting evaluated and that the other is not.
