# All this directory tests are the cases where a type name dominates over a
# variable name, since in the ambiguous case, a variable name dominates.
# The remaining classifications are covered in the sections which define the
# meaning of each category of name.

tcltest::test 6.5.1-type-1 { A name in an extends clause is a type name, not
        an ambiguous name } {
    empty_class T651t1 {
        int Super;
        class Super {}
        class Sub extends Super {}
    }
} PASS

tcltest::test 6.5.1-type-2 { A name in an implements clause is a type name, not
        an ambiguous name } {
    empty_class T651t2 {
        int Super;
        interface Super {}
        class Sub implements Super {}
    }
} PASS

tcltest::test 6.5.1-type-3 { A name in an interface extends clause is a type
        name, not an ambiguous name } {
    empty_class T651t3 {
        int Super;
        interface Super {}
        interface Sub extends Super {}
    }
} PASS

tcltest::test 6.5.1-type-4 { A name in a field declaration is a type name, not
        an ambiguous name } {
    empty_class T651t4 {
        int T651t4;
        T651t4 t;
    }
} PASS

tcltest::test 6.5.1-type-5 { A name in the result type of a method is a type
        name, not an ambiguous name } {
    empty_class T651t5 {
        int T651t5;
        T651t5 m() { return null; }
    }
} PASS

tcltest::test 6.5.1-type-6 { A name in a formal parameter is a type name, not
        an ambiguous name } {
    empty_class T651t6 {
        int T651t6;
        void m(T651t6 T651t6) {}
    }
} PASS

tcltest::test 6.5.1-type-7 { A name in a throws clause is a type name, not
        an ambiguous name } {
    empty_class T651t7 {
        int Type;
        class Type extends Exception {}
        void m() throws Type {}
    }
} PASS

tcltest::test 6.5.1-type-8 { A name in the type of a local variable is a type
name, not an ambiguous name } {
    empty_main T651t8 {
        int T651t8;
        T651t8 t;
    }
} PASS

tcltest::test 6.5.1-type-9 { A name in an exception parameter is a type name,
        not an ambiguous name } {
    empty_main T651t9 {
        int Type;
        class Type extends Exception {}
        try {
            throw new Type();
        } catch (Type t) {
        }
    }
} PASS

tcltest::test 6.5.1-type-10 { A name in a class literal is a type name, not
        an ambiguous name } {
    empty_main T651t10 {
        int T651t10;
        T651t10.class.toString();
    }
} PASS

tcltest::test 6.5.1-type-11 { A name qualifying a this expression is a type
        name, not an ambiguous name } {
    empty_class T651t11 {
        void foo() {
            int T651t11;
            T651t11.this.toString();
        }
    }
} PASS

tcltest::test 6.5.1-type-12 { A name in an unqualified class instance creation
        expression is a type name, not an ambiguous name } {
    empty_main T651t12 {
        int T651t12;
        new T651t12();
    }
} PASS

tcltest::test 6.5.1-type-13 { A name in an unqualified anonymous class instance
        creation expression is a type name, not an ambiguous name } {
    empty_main T651t13 {
        int T651t13;
        new T651t13() {};
    }
} PASS

tcltest::test 6.5.1-type-14 { A name as the element type of an arry creation
        expression is a type name, not an ambiguous name } {
    empty_main T651t14 {
        int T651t14;
        Object o = new T651t14[1];
    }
} PASS

tcltest::test 6.5.1-type-15 { A name qualifying a super field access is a
        type name, not an ambiguous name } {
    empty_class T651t15 {
        class Super {
            int i;
        }
        class Sub extends Super {
            int Sub;
            int j = Sub.super.i;
        }
    }
} PASS

tcltest::test 6.5.1-type-16 { A name qualifying a super method access is a
        type name, not an ambiguous name } {
    empty_class T651t16 {
        void foo() {
            int T651t16;
            T651t16.super.toString();
        }
    }
} PASS

tcltest::test 6.5.1-type-17 { A name in a cast operator is a type name, not
        an ambiguous name } {
    empty_class T651t17 {
        int T651t17;
        void foo(Object o) {
            ((T651t17) o).toString();
        }
    }
} PASS

tcltest::test 6.5.1-type-18 { A name following the instanceof operator is a
        type name, not an ambiguous name } {
    empty_class T651t18 {
        int T651t18;
        void foo(Object o) {
            boolean b = o instanceof T651t18;
        }
    }
} PASS
