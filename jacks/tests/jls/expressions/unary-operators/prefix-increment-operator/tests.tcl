tcltest::test 15.15.1-argument-1 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a1 {int i = ++1;}
} FAIL

tcltest::test 15.15.1-argument-2 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a2 {boolean b = ++true;}
} FAIL

tcltest::test 15.15.1-argument-3 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a3 {Object o = ++null;}
} FAIL

tcltest::test 15.15.1-argument-4 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a4 {Object o = ++(new Object());}
} FAIL

tcltest::test 15.15.1-argument-5 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a5 {int i = ++(System.out.println());}
} FAIL

tcltest::test 15.15.1-argument-6 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a6 {boolean b = true, b2 = ++b;}
} FAIL

tcltest::test 15.15.1-argument-7 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a7 {Object o = null, o2 = ++o;}
} FAIL

tcltest::test 15.15.1-argument-8 { ++Prefix must operate on a numeric variable } {
    empty_class T15151a8 {int i = 1, i2 = ++(-i);}
} FAIL

tcltest::test 15.15.1-argument-9 { ++Prefix cannot operate on final variable } {
    empty_class T15151a9 {final int i = 1, j = ++i;}
} FAIL


tcltest::test 15.15.1-type-1 { ++Prefix operates on all numeric types } {
    empty_class T15151t1 {byte b = 1, b2 = ++b;}
} PASS

tcltest::test 15.15.1-type-2 { ++Prefix operates on all numeric types } {
    empty_class T15151t2 {short s = 1, s2 = ++s;}
} PASS

tcltest::test 15.15.1-type-3 { ++Prefix operates on all numeric types } {
    empty_class T15151t3 {char c = 1, c2 = ++c;}
} PASS

tcltest::test 15.15.1-type-4 { ++Prefix operates on all numeric types } {
    empty_class T15151t4 {int i = 1, i2 = ++i;}
} PASS

tcltest::test 15.15.1-type-5 { ++Prefix operates on all numeric types } {
    empty_class T15151t5 {long l = 1, l2 = ++l;}
} PASS

tcltest::test 15.15.1-type-6 { ++Prefix operates on all numeric types } {
    empty_class T15151t6 {float f = 1, f2 = ++f;}
} PASS

tcltest::test 15.15.1-type-7 { ++Prefix operates on all numeric types } {
    empty_class T15151t7 {double d = 1, d2 = ++d;}
} PASS


tcltest::test 15.15.1-result-1 { ++Prefix results in a value, not a variable } {
    empty_class T15151r1 {
void foo() {
    int i = 1;
    ++i = 2;
}   }
} FAIL
