tcltest::test 15.15.2-argument-1 { --Prefix must operate on a numeric variable } {
    empty_class T15152a1 {int i = --1;}
} FAIL

tcltest::test 15.15.2-argument-2 { --Prefix must operate on a numeric variable } {
    empty_class T15152a2 {boolean b = --true;}
} FAIL

tcltest::test 15.15.2-argument-3 { --Prefix must operate on a numeric variable } {
    empty_class T15152a3 {Object o = --null;}
} FAIL

tcltest::test 15.15.2-argument-4 { --Prefix must operate on a numeric variable } {
    empty_class T15152a4 {Object o = --(new Object());}
} FAIL

tcltest::test 15.15.2-argument-5 { --Prefix must operate on a numeric variable } {
    empty_class T15152a5 {int i = --(System.out.println());}
} FAIL

tcltest::test 15.15.2-argument-6 { --Prefix must operate on a numeric variable } {
    empty_class T15152a6 {boolean b = true, b2 = --b;}
} FAIL

tcltest::test 15.15.2-argument-7 { --Prefix must operate on a numeric variable } {
    empty_class T15152a7 {Object o = null, o2 = --o;}
} FAIL

tcltest::test 15.15.2-argument-8 { --Prefix must operate on a numeric variable } {
    empty_class T15152a8 {int i = 1, i2 = --(-i);}
} FAIL

tcltest::test 15.15.2-argument-9 { --Prefix cannot operate on final variable } {
    empty_class T15152a9 {final int i = 1, j = --i;}
} FAIL


tcltest::test 15.15.2-type-1 { --Prefix operates on all numeric types } {
    empty_class T15152t1 {byte b = 1, b2 = --b;}
} PASS

tcltest::test 15.15.2-type-2 { --Prefix operates on all numeric types } {
    empty_class T15152t2 {short s = 1, s2 = --s;}
} PASS

tcltest::test 15.15.2-type-3 { --Prefix operates on all numeric types } {
    empty_class T15152t3 {char c = 1, c2 = --c;}
} PASS

tcltest::test 15.15.2-type-4 { --Prefix operates on all numeric types } {
    empty_class T15152t4 {int i = 1, i2 = --i;}
} PASS

tcltest::test 15.15.2-type-5 { --Prefix operates on all numeric types } {
    empty_class T15152t5 {long l = 1, l2 = --l;}
} PASS

tcltest::test 15.15.2-type-6 { --Prefix operates on all numeric types } {
    empty_class T15152t6 {float f = 1, f2 = --f;}
} PASS

tcltest::test 15.15.2-type-7 { --Prefix operates on all numeric types } {
    empty_class T15152t7 {double d = 1, d2 = --d;}
} PASS


tcltest::test 15.15.2-result-1 { --Prefix results in a value, not a variable } {
    empty_class T15152r1 {
void foo() {
    int i = 1;
    --i = 2;
}   }
} FAIL


