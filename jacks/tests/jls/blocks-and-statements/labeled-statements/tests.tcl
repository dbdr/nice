tcltest::test 14.7-scope-1 { The scope of a label is its contained statement } {
    empty_main T147scope1 {
        int i, j;
        label: i = 1;
        label: j = 1;
    }
} PASS

tcltest::test 14.7-shadow-1 { Labels cannot shadow another label in the
        same enclosing method, constructor, or initializer } {
    empty_main T147shadow1 {
        int i;
        test: {
            test: i = 1;
        }
    }
} FAIL

tcltest::test 14.7-shadow-2 { Labels cannot shadow another label in the
        same enclosing method, constructor, or initializer } {
    empty_main T147shadow2 {
        int i;
        test: test: i = 1;
    }
} FAIL

tcltest::test 14.7-shadow-3 { Labels cannot shadow another label in the same
        immediately enclosing method, constructor, or initializer. Here, the
        two labels are in different immediately enclosing methods.} {
    empty_main T147shadow3 {
        test: new Object() {
            void foo() {
                int i;
                test: i = 1;
            }
        };
    }
} PASS

tcltest::test 14.7-same-1 { Labels can share same identifier as package } {
    compile [saveas test/T147same1.java {
package test;
class T147same1 {
    void foo() {
        int i;
        test: i = 1;
        new test.T147same1();
    }
}
    }]
} PASS

tcltest::test 14.7-same-2 { Labels can share same identifier as class } {
    empty_main T147same2 {
        int i;
        T147same2: i = 1;
        new T147same2();
    }
} PASS

tcltest::test 14.7-same-3 { Labels can share same identifier as interface } {
    empty_main T147same3 {
        int i;
        Cloneable: i = 1;
        new Cloneable() {};
    }
} PASS

tcltest::test 14.7-same-4 { Labels can share same identifier as method } {
    empty_class T147same4 {
        void test() {}
        void foo() {
            int i;
            test: i = 1;
            test();
        }
    }
} PASS

tcltest::test 14.7-same-5 { Labels can share same identifier as field } {
    empty_class T147same5 {
        int test;
        void foo() {
            int i;
            test: i = 1;
            test = 1;
        }
    }
} PASS

tcltest::test 14.7-same-6 { Labels can share same identifier as parameter } {
    empty_class T147same6 {
        void foo(String[] args) {
            int i;
            args: i = 1;
            i = args.length;
        }
    }
} PASS

tcltest::test 14.7-same-7 { Labels can share same identifier as
        local variable } {
    empty_main T147same7 {
        int test, i;
        test: i = 1;
        test = 1;
    }
} PASS

tcltest::test 14.7-enclosed-1 { Labels must have an enclosed statement } {
    empty_main T147enclosed1 {
        test:
    }
} FAIL

tcltest::test 14.7-enclosed-2 { Labels must have an enclosed statement } {
    empty_main T147enclosed2 {
        test: int i;
    }
} FAIL

tcltest::test 14.7-enclosed-3 { Labels must have an enclosed statement } {
    empty_main T147enclosed3 {
        test: class Foo {}
    }
} FAIL

tcltest::test 14.7-enclosed-4 { Labels must have an enclosed statement } {
    empty_main T147enclosed4 {
        test: {}
    }
} PASS

tcltest::test 14.7-enclosed-5 { Labels must have an enclosed statement } {
    empty_main T147enclosed5 {
        test: ;
    }
} PASS

tcltest::test 14.7-ident-1 { Labels must be identifiers } {
    empty_main T147ident1 {
        int i;
        1: i = 1;
    }
} FAIL

tcltest::test 14.7-ident-2 { Labels must be identifiers } {
    empty_main T147ident2 {
        int i;
        "label": i = 1;
    }
} FAIL

tcltest::test 14.7-ident-3 { Labels must be identifiers } {
    empty_main T147ident3 {
        int i;
        (label): i = 1;
    }
} FAIL

tcltest::test 14.7-ident-4 { Labels must be identifiers } {
    empty_main T147ident4 {
        int i;
        label.a: i = 1;
    }
} FAIL

tcltest::test 14.7-ident-5 { Labels must be identifiers } {
    empty_main T147ident5 {
        int i;
        int: i = 1;
    }
} FAIL

tcltest::test 14.7-ident-6 { Labels must be identifiers, including unicode } {
    empty_main T147ident6 {
        int i;
        \u0061\u003a break a;
        b: break \u0062;
    }
} PASS
