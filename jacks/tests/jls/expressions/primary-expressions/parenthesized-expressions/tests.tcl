tcltest::test 15.8.5-statement-1 { Parenthesized expression must contain
        expression, not statement } {
    empty_main T1585s1 {
        (int i);
    }
} FAIL

tcltest::test 15.8.5-statement-2 { Parenthesized expression must contain
        expression, not statement } {
    empty_main T1585s2 {
        int[] i = ({});
    }
} FAIL

tcltest::test 15.8.5-statement-3 { Parenthesized expression must contain
        expression, not statement } {
    empty_main T1585s3 {
        (class Inner{})
    }
} FAIL

tcltest::test 15.8.5-statement-4 { Parenthesized expression must contain
        expression, not statement } {
    empty_main T1585s4 {
        (;)
    }
} FAIL

tcltest::test 15.8.5-statement-5 { Parenthesized expression must contain
        expression, not statement } {
    empty_main T1585s5 {
        int i;
        (label: i = 1);
    }
} FAIL

tcltest::test 15.8.5-statement-6 { Parenthesized expression is not a statement } {
    empty_class T1585s6 {
        void foo() {}
        void bar() {
            (foo());
        }
    }
} FAIL

tcltest::test 15.8.5-statement-7 { Parenthesized expression is not a statement } {
    empty_main T1585s7 {
        int i;
        (i = 1);
    }
} FAIL

tcltest::test 15.8.5-primary-expression-1 { Parenthesized expression must
        contain expression } {
    empty_main T1585p31 {
        int i = (1);
        boolean b = (false);
        char c = ('1');
        Object o = (null);
        String s = ("");
    }
} PASS

tcltest::test 15.8.5-primary-expression-2 { Parenthesized expression must
        contain expression } {
    empty_main T1585p32 {
        Class c1 = (int.class),
              c2 = (int[].class),
              c3 = (Object.class),
              c4 = (Object[].class);
    }
} PASS

tcltest::test 15.8.5-primary-expression-3 { Parenthesized expression must
        contain expression } {
    empty_main T1585p33 {
        Class c = (void.class);
    }
} PASS

tcltest::test 15.8.5-primary-expression-4 { Parenthesized expression must
        contain expression } {
    empty_main T1585p34 {
        Class c1 = (int).class;
    }
} FAIL

tcltest::test 15.8.5-primary-expression-5 { Parenthesized expression must
        contain expression } {
    empty_main T1585p35 {
        Class c = (void).class;
    }
} FAIL

tcltest::test 15.8.5-primary-expression-6 { Parenthesized expression must
        contain expression } {
    empty_main T1585p36 {
        Class c1 = int.(class);
    }
} FAIL

tcltest::test 15.8.5-primary-expression-7 { Parenthesized expression must
        contain expression } {
    empty_main T1585p37 {
        Class c = void.(class);
    }
} FAIL

tcltest::test 15.8.5-this-expression-1 { Parenthesized expression must
        contain expression } {
    empty_class T1585te1 {
        int i;
        void foo() {
            (this).i = 1;
        }
    }
} PASS

tcltest::test 15.8.5-this-expression-2 { Parenthesized expression must
        contain expression } {
    empty_class T1585te2 {
        int i;
        class Inner {
            Inner() {
                (T1585te2.this).i = 1;
            }
        }
    }
} PASS

tcltest::test 15.8.5-this-expression-3 { Parenthesized expression must
        contain expression } {
    empty_class T1585te3 {
        int i;
        class Inner {
            Inner() {
                (T1585te3).this.i = 1;
            }
        }
    }
} FAIL

tcltest::test 15.8.5-this-expression-4 { Parenthesized expression must
        contain expression } {
    empty_class T1585te4 {
        int i;
        class Inner {
            Inner() {
                T1585te4.(this).i = 1;
            }
        }
    }
} FAIL

tcltest::test 15.8.5-creation-expression-1 { Parenthesized expression must
        contain expression } {
    empty_class T1585ce1 {
        int i;
        void foo() {
            (new T1585ce1()).i = 1;
        }
    }
} PASS

tcltest::test 15.8.5-creation-expression-2 { Parenthesized expression must
        contain expression } {
    empty_class T1585ce2 {
        void foo() {
            (new T1585ce2() {
                int i;
            }).i = 1;
        }
    }
} PASS

tcltest::test 15.8.5-creation-expression-3 { Parenthesized expression must
        contain expression } {
    empty_main T1585ce3 {
        int[] i = (new int[1]);
        Object[] o = (new Object[1]);
    }
} PASS

tcltest::test 15.8.5-creation-expression-4 { Parenthesized expression must
        contain expression } {
    empty_main T1585ce4 {
        int[] i = (new int[] {1});
        Object[] o = (new Object[] {new Object()});
    }
} PASS

tcltest::test 15.8.5-creation-expression-5 { Parenthesized expression must
        contain expression } {
    empty_main T1585ce5 {
        int[] i = new (int[1]);
    }
} FAIL

tcltest::test 15.8.5-creation-expression-6 { Parenthesized expression must
        contain expression } {
    empty_main T1585ce6 {
        int[] i = new (int[] {1});
    }
} FAIL

tcltest::test 15.8.5-creation-expression-7 { Parenthesized expression must
        contain expression } {
    empty_main T1585ce7 {
        int[] i = new int[] ({1});
    }
} FAIL

tcltest::test 15.8.5-creation-expression-8 { Parenthesized expression must
        contain expression } {
    empty_main T1585ce8 {
        int[][] i = new int[][] { ({}) };
    }
} FAIL

tcltest::test 15.8.5-field-expression-1 { Parenthesized expression must
        contain expression } {
    empty_class T1585fe1 {
        int i;
        T1585fe1 t;
        void foo() {
            int j = (t.i);
        }
    }
} PASS

tcltest::test 15.8.5-field-expression-2 { Parenthesized expression must
        contain expression } {
    empty_class T1585fe2 {
        int i;
        T1585fe2 t;
        void foo() {
            t.(i) = 1;
        }
    }
} FAIL

tcltest::test 15.8.5-field-expression-3 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe3.java {
class T1585fe3 {
    int i;
    T1585fe3 t;
}
class Sub extends T1585fe3 {
    void foo() {
        (super.t).i = 1;
    }
}
    }]
} PASS

tcltest::test 15.8.5-field-expression-4 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe4.java {
class T1585fe4 {
    int i;
    T1585fe4 t;
}
class Sub extends T1585fe4 {
    void foo() {
        (super).t.i = 1;
    }
}
    }]
} FAIL

tcltest::test 15.8.5-field-expression-5 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe5.java {
class T1585fe5 {
    int i;
    T1585fe5 t;
}
class Sub extends T1585fe5 {
    void foo() {
        super.(t).i = 1;
    }
}
    }]
} FAIL

tcltest::test 15.8.5-field-expression-6 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe6.java {
class T1585fe6 {
    int i;
    T1585fe6 t;
}
class Sub extends T1585fe6 {
    class Inner {
        void foo() {
            (Sub.super.t).i = 1;
        }
    }
}
    }]
} PASS

tcltest::test 15.8.5-field-expression-7 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe7.java {
class T1585fe7 {
    int i;
    T1585fe7 t;
}
class Sub extends T1585fe7 {
    class Inner {
        void foo() {
            (Sub).super.t.i = 1;
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-field-expression-8 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe8.java {
class T1585fe8 {
    int i;
    T1585fe8 t;
}
class Sub extends T1585fe8 {
    class Inner {
        void foo() {
            Sub.(super).t.i = 1;
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-field-expression-9 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe9.java {
class T1585fe9 {
    int i;
    T1585fe9 t;
}
class Sub extends T1585fe9 {
    class Inner {
        void foo() {
            Sub.super.(t).i = 1;
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-field-expression-10 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe10.java {
class T1585fe10 {
    int i;
    T1585fe10 t;
}
class Sub extends T1585fe10 {
    class Inner {
        void foo() {
            (Sub.super).t.i = 1;
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-field-expression-11 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585fe11.java {
class T1585fe11 {
    int i;
    T1585fe11 t;
}
class Sub extends T1585fe11 {
    class Inner {
        void foo() {
            Sub.(super.t).i = 1;
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression-1 { Parenthesized expression must
        contain expression } {
    empty_class T1585me1 {
        int bar() { return 1; }
        int i = (bar());
    }
} PASS

tcltest::test 15.8.5-method-expression-2 { Parenthesized expression must
        contain expression } {
    empty_class T1585me2 {
        int bar() { return 1; }
        int i = (bar)();
    }
} FAIL

tcltest::test 15.8.5-method-expression-3 { Parenthesized expression must
        contain expression } {
    empty_class T1585me3 {
        int bar() { return 1; }
        T1585me3 t;
        int i = (t.bar());
    }
} PASS

tcltest::test 15.8.5-method-expression-4 { Parenthesized expression must
        contain expression } {
    empty_class T1585me4 {
        int bar() { return 1; }
        T1585me4 t;
        int i = t.(bar());
    }
} FAIL

tcltest::test 15.8.5-method-expression-5 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me5.java {
class T1585me5 {
    int bar() { return 1; }
}
class Sub extends T1585me5 {
    int i = (super.bar());
}
    }]
} PASS

tcltest::test 15.8.5-method-expression-6 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me6.java {
class T1585me6 {
    int bar() { return 1; }
}
class Sub extends T1585me6 {
    int i = (super).bar();
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression-7 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me7.java {
class T1585me7 {
    int bar() { return 1; }
}
class Sub extends T1585me7 {
    int i = super.(bar());
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression-8 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me8.java {
class T1585me8 {
    int bar() { return 1; }
}
class Sub extends T1585me8 {
    class Inner {
        Inner() {
            int i = (Sub.super.bar());
        }
    }
}
    }]
} PASS

tcltest::test 15.8.5-method-expression-9 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me9.java {
class T1585me9 {
    int bar() { return 1; }
}
class Sub extends T1585me9 {
    class Inner {
        Inner() {
            int i = (Sub).super.bar();
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression-10 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me10.java {
class T1585me10 {
    int bar() { return 1; }
}
class Sub extends T1585me10 {
    class Inner {
        Inner() {
            int i = Sub.(super).bar();
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression11 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me11.java {
class T1585me11 {
    int bar() { return 1; }
}
class Sub extends T1585me11 {
    class Inner {
        Inner() {
            int i = Sub.super.(bar());
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression-12 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me12.java {
class T1585me12 {
    int bar() { return 1; }
}
class Sub extends T1585me12 {
    class Inner {
        Inner() {
            int i = (Sub.super).bar();
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-method-expression-13 { Parenthesized expression must
        contain expression } {
    compile [saveas T1585me13.java {
class T1585me13 {
    int bar() { return 1; }
}
class Sub extends T1585me13 {
    class Inner {
        Inner() {
            int i = Sub.(super.bar());
        }
    }
}
    }]
} FAIL

tcltest::test 15.8.5-array-expression-1 { Parenthesized expression must
        contain expression } {
    empty_main T1585ae1 {
        int[] ia = {1};
        int i = (ia[0]);
    }
} PASS

tcltest::test 15.8.5-array-expression-2 { Parenthesized expression must
        contain expression } {
    empty_main T1585ae2 {
        int[] ia = {1};
        int i = (ia)[0];
    }
} PASS

tcltest::test 15.8.5-array-expression-3 { Parenthesized expression must
        contain expression } {
    empty_main T1585ae3 {
        int[] ia = {1};
        int i = ((ia)[0]);
    }
} PASS

tcltest::test 15.8.5-array-expression-4 { Parenthesized expression must
        contain expression } {
    empty_main T1585ae4 {
        int[] ia = {1};
        int i = ia([0]);
    }
} FAIL

tcltest::test 15.8.5-variable-1 { Parenthesized expression may now
        represent a variable } {
    ok_pass_or_warn [empty_class T1585v1 {
        static int i1 = 1, j1 = (i1)++;
        int i2 = 1, j2 = (i2)++;
        private static int i3 = 1;
        private int i4 = 1;
        class Inner {
            Inner() {
                int j3 = (i3)++;
                int j4 = (i4)++;
            }
        }
        void foo() {
            int i5 = 1, j5 = (i5)++;
            int[] i6 = {1};
            int j6 = (i6[0])++;
        }
    }]
} OK

tcltest::test 15.8.5-variable-2 { Parenthesized expression may now
        represent a variable } {
    ok_pass_or_warn [empty_class T1585v2 {
        static int i1 = 1, j1 = (i1)--;
        int i2 = 1, j2 = (i2)--;
        private static int i3 = 1;
        private int i4 = 1;
        class Inner {
            Inner() {
                int j3 = (i3)--;
                int j4 = (i4)--;
            }
        }
        void foo() {
            int i5 = 1, j5 = (i5)--;
            int[] i6 = {1};
            int j6 = (i6[0])--;
        }
    }]
} OK

tcltest::test 15.8.5-variable-3 { Parenthesized expression may now
        represent a variable } {
    ok_pass_or_warn [empty_class T1585v3 {
        static int i1 = 1, j1 = ++(i1);
        int i2 = 1, j2 = ++(i2);
        private static int i3 = 1;
        private int i4 = 1;
        class Inner {
            Inner() {
                int j3 = ++(i3);
                int j4 = ++(i4);
            }
        }
        void foo() {
            int i5 = 1, j5 = ++(i5);
            int[] i6 = {1};
            int j6 = ++(i6[0]);
        }
    }]
} OK

tcltest::test 15.8.5-variable-4 { Parenthesized expression may now
        represent a variable } {
    ok_pass_or_warn [empty_class T1585v4 {
        static int i1 = 1, j1 = --(i1);
        int i2 = 1, j2 = --(i2);
        private static int i3 = 1;
        private int i4 = 1;
        class Inner {
            Inner() {
                int j3 = --(i3);
                int j4 = --(i4);
            }
        }
        void foo() {
            int i5 = 1, j5 = --(i5);
            int[] i6 = {1};
            int j6 = --(i6[0]);
        }
    }]
} OK

tcltest::test 15.8.5-variable-5 { Parenthesized expression may now represent
        variable in assignment } {
    ok_pass_or_warn [empty_main T1585v5 {
        int i = 0;
        (i) = 1;
    }]
} OK

tcltest::test 15.8.5-variable-6 { Parenthesized expression may now represent
        variable in assignment } {
    ok_pass_or_warn [empty_main T1585v6 {
        int[] i = {0};
        (i[0]) = 1;
    }]
} OK

tcltest::test 15.8.5-variable-7 { Parenthesized expression may now represent
        variable in assignment } {
    ok_pass_or_warn [empty_main T1585v7 {
        int i = 0;
        (i) += 1;
    }]
} OK

tcltest::test 15.8.5-variable-8 { Parenthesized expression may now represent
        variable in assignment } {
    ok_pass_or_warn [empty_main T1585v8 {
        int[] i = {0};
        (i[0]) += 1;
    }]
} OK

tcltest::test 15.8.5-cast-1 { Parenthesized expression must contain expression } {
    empty_main T1585c1 {
        int i = ((int) 1L);
    }
} PASS

tcltest::test 15.8.5-cast-2 { Parenthesized expression must contain expression } {
    empty_main T1585c2 {
        int i = (int) (1L);
    }
} PASS

tcltest::test 15.8.5-cast-3 { Parenthesized expression must contain expression } {
    empty_main T1585c3 {
        int i = ((int)) 1L;
    }
} FAIL

tcltest::test 15.8.5-instanceof-1 { Parenthesized expression must
        contain expression } {
    empty_main T1585i1 {
        boolean b = (new T1585i1() instanceof Object);
    }
} PASS

tcltest::test 15.8.5-instanceof-2 { Parenthesized expression must
        contain expression } {
    empty_main T1585i2 {
        boolean b = (new T1585i2()) instanceof Object;
    }
} PASS

tcltest::test 15.8.5-instanceof-3 { Parenthesized expression must
        contain expression } {
    empty_main T1585i3 {
        boolean b = new T1585i3() instanceof (Object);
    }
} FAIL

tcltest::test 15.8.5-precedence-1 { Parenthesized expression has highest precedence } {
    constant_expression T1585p1 {1+2*3 == 7} {(1+2)*3 == 9} {1+(2*3) == 7}
} PASS
