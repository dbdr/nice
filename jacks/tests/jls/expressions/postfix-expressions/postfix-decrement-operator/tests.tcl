tcltest::test 15.14.2-argument-1 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a1 {int i = 1--;}
} FAIL

tcltest::test 15.14.2-argument-2 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a2 {boolean b = true--;}
} FAIL

tcltest::test 15.14.2-argument-3 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a3 {Object o = null--;}
} FAIL

tcltest::test 15.14.2-argument-4 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a4 {Object o = (new Object())--;}
} FAIL

tcltest::test 15.14.2-argument-5 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a5 {int i = (System.out.println())--;}
} FAIL

tcltest::test 15.14.2-argument-6 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a6 {boolean b = true, b2 = b--;}
} FAIL

tcltest::test 15.14.2-argument-7 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a7 {Object o = null, o2 = o--;}
} FAIL

tcltest::test 15.14.2-argument-8 { Postfix-- must operate on a numeric variable } {
    empty_class T15142a8 {int i = 1, i2 = (-i)--;}
} FAIL

tcltest::test 15.14.2-argument-9 { Postfix-- cannot operate on final variable } {
    empty_class T15142a9 {final int i = 1, j = i--;}
} FAIL


tcltest::test 15.14.2-type-1 { Postfix-- operates on all numeric types } {
    empty_class T15142t1 {byte b = 1, b2 = b--;}
} PASS

tcltest::test 15.14.2-type-2 { Postfix-- operates on all numeric types } {
    empty_class T15142t2 {short s = 1, s2 = s--;}
} PASS

tcltest::test 15.14.2-type-3 { Postfix-- operates on all numeric types } {
    empty_class T15142t3 {char c = 1, c2 = c--;}
} PASS

tcltest::test 15.14.2-type-4 { Postfix-- operates on all numeric types } {
    empty_class T15142t4 {int i = 1, i2 = i--;}
} PASS

tcltest::test 15.14.2-type-5 { Postfix-- operates on all numeric types } {
    empty_class T15142t5 {long l = 1, l2 = l--;}
} PASS

tcltest::test 15.14.2-type-6 { Postfix-- operates on all numeric types } {
    empty_class T15142t6 {float f = 1, f2 = f--;}
} PASS

tcltest::test 15.14.2-type-7 { Postfix-- operates on all numeric types } {
    empty_class T15142t7 {double d = 1, d2 = d--;}
} PASS


tcltest::test 15.14.2-result-1 { Postfix-- results in a value, not a variable } {
    empty_class T15142r1 {
void foo() {
    int i = 1;
    i-- = 2;
}   }
} FAIL
