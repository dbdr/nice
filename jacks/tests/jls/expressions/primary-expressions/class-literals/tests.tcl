tcltest::test 15.8.2-type-1 { The class literal may be for any type } {
    empty_main T1582t1 {
        Class c = Object.class;
    }
} PASS

tcltest::test 15.8.2-type-2 { The class literal may be for any type } {
    empty_main T1582t2 {
        Class c = T1582t2.class;
    }
} PASS

tcltest::test 15.8.2-type-3 { The class literal may be for any type } {
    empty_main T1582t3 {
        Class c = Runnable.class;
    }
} PASS

tcltest::test 15.8.2-type-4 { The class literal may be for any type } {
    empty_main T1582t4 {
        Class c = void.class;
    }
} PASS

tcltest::test 15.8.2-type-5 { The class literal may be for any type } {
    empty_main T1582t5 {
        Class c = int.class;
    }
} PASS

tcltest::test 15.8.2-type-6 { The class literal may be for any type } {
    empty_main T1582t6 {
        Class c = int[].class;
    }
} PASS

tcltest::test 15.8.2-type-7 { The class literal may be for any type } {
    empty_main T1582t7 {
        Class c = Object[].class;
    }
} PASS

tcltest::test 15.8.2-type-8 { The class literal may be for any type } {
    empty_main T1582t8 {
        Class c = Runnable[][][][][][].class;
    }
} PASS

tcltest::test 15.8.2-primary-1 { The class literal serves as a primary } {
    empty_main T1582p1 {
        Object.class.getName();
    }
} PASS

tcltest::test 15.8.2-syntax-1 { The class literal does not work on
        expressions } {
    empty_class T1582s1 {
        Class c = this.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-2 { The class literal does not work on
        expressions } {
    empty_class T1582s2 {
        Class c = (Object).class;
    }
} FAIL

tcltest::test 15.8.2-syntax-3 { The class literal does not work on
        expressions } {
    empty_class T1582s3 {
        Class c = 1.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-4 { The class literal does not work on
        expressions } {
    empty_class T1582s4 {
        Class c = "".class;
    }
} FAIL

tcltest::test 15.8.2-syntax-5 { The class literal does not work on
        expressions } {
    empty_class T1582s5 {
        int foo;
        Class c = foo.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-6 { The class literal does not work on
        expressions } {
    empty_class T1582s6 {
        int foo;
        Class c = null.class;
    }
} FAIL

tcltest::test 15.8.2-syntax-7 { The type in the class literal is not
        obscured } {
    empty_class T1582s7 {
        int T1582s7; // obscure the class name from normal expressions
        Class c = T1582s7.class;
    }
} PASS
