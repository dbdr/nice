tcltest::test 15.14.1-argument-1 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a1 {int i = 1++;}
} FAIL

tcltest::test 15.14.1-argument-2 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a2 {boolean b = true++;}
} FAIL

tcltest::test 15.14.1-argument-3 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a3 {Object o = null++;}
} FAIL

tcltest::test 15.14.1-argument-4 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a4 {Object o = (new Object())++;}
} FAIL

tcltest::test 15.14.1-argument-5 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a5 {int i = (System.out.println())++;}
} FAIL

tcltest::test 15.14.1-argument-6 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a6 {boolean b = true, b2 = b++;}
} FAIL

tcltest::test 15.14.1-argument-7 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a7 {Object o = null, o2 = o++;}
} FAIL

tcltest::test 15.14.1-argument-8 { Postfix++ must operate on a numeric variable } {
    empty_class T15141a8 {int i = 1, i2 = (-i)++;}
} FAIL

tcltest::test 15.14.1-argument-9 { Postfix++ cannot operate on final variable } {
    empty_class T15141a9 {final int i = 1, j = i++;}
} FAIL


tcltest::test 15.14.1-type-1 { Postfix++ operates on all numeric types } {
    empty_class T15141t1 {byte b = 1, b2 = b++;}
} PASS

tcltest::test 15.14.1-type-2 { Postfix++ operates on all numeric types } {
    empty_class T15141t2 {short s = 1, s2 = s++;}
} PASS

tcltest::test 15.14.1-type-3 { Postfix++ operates on all numeric types } {
    empty_class T15141t3 {char c = 1, c2 = c++;}
} PASS

tcltest::test 15.14.1-type-4 { Postfix++ operates on all numeric types } {
    empty_class T15141t4 {int i = 1, i2 = i++;}
} PASS

tcltest::test 15.14.1-type-5 { Postfix++ operates on all numeric types } {
    empty_class T15141t5 {long l = 1, l2 = l++;}
} PASS

tcltest::test 15.14.1-type-6 { Postfix++ operates on all numeric types } {
    empty_class T15141t6 {float f = 1, f2 = f++;}
} PASS

tcltest::test 15.14.1-type-7 { Postfix++ operates on all numeric types } {
    empty_class T15141t7 {double d = 1, d2 = d++;}
} PASS


tcltest::test 15.14.1-result-1 { Postfix++ results in a value, not a variable } {
    empty_class T15141r1 {
void foo() {
    int i = 1;
    i++ = 2;
}   }
} FAIL


