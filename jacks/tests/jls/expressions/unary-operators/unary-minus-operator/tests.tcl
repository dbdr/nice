tcltest::test 15.15.4-type-1 { - must have numeric operand } {
    is_assignable_to T15154t1 Object -null
} FAIL

tcltest::test 15.15.4-type-2 { - must have numeric operand } {
    is_assignable_to T15154t2 boolean -true
} FAIL

tcltest::test 15.15.4-type-3 { - must have numeric operand } {
    is_assignable_to T15154t3 Object {-(new Object())}
} FAIL

tcltest::test 15.15.4-type-4 { - must have numeric operand } {
    is_assignable_to T15154t4 int -System.out.println()
} FAIL

tcltest::test 15.15.4-type-5 { - performs unary promotion } {
#is_assignable_to creates byte n1, byte n2
    is_assignable_to T15154t5 byte 1 byte -n1
} FAIL

tcltest::test 15.15.4-type-6 { - must have numeric operand } {
    is_assignable_to T15154t6 short -1
} PASS

tcltest::test 15.15.4-type-7 { - must have numeric operand } {
    is_assignable_to T15154t7 char -(-'1')
} PASS

tcltest::test 15.15.4-type-8 { - must have numeric operand } {
    is_assignable_to T15154t8 byte -1
} PASS


tcltest::test 15.15.4-int-1 { -x = ~x+1 for all ints } {
    constant_expression T15154i1 {-0 == ~0+1}
} PASS

tcltest::test 15.15.4-int-2 { -x = ~x+1 for all ints } {
    constant_expression T15154i2 {-0xffffffff == ~0xffffffff+1}
} PASS

tcltest::test 15.15.4-int-3 { -MIN_INT is still MIN_INT } {
    constant_expression T15154i3 {-0x80000000 == 0x80000000}
} PASS

tcltest::test 15.15.4-long-1 { -x = ~x+1 for all longs } {
    constant_expression T15154l1 {-0L == ~0L+1}
} PASS

tcltest::test 15.15.4-long-2 { -x = ~x+1 for all longs } {
    constant_expression T15154l2 {-0xffffffffffffffffL == ~0xffffffffffffffffL+1}
} PASS

tcltest::test 15.15.4-long-3 { -MIN_LONG is still MIN_LONG } {
    constant_expression T15154l3 {-0x8000000000000000L == 0x8000000000000000L}
} PASS


tcltest::test 15.15.4-float-1 { -NaN is still NaN } {
    constant_expression T15154f1 {-Float.NaN != -Float.NaN}
} PASS

tcltest::test 15.15.4-float-2 { - Infinity is Infinity of opposite sign } {
    constant_expression T15154f2 {-Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.15.4-float-3 { - 0.0 is zero of opposite sign } {
    constant_expression T15154f3 {(1/0.0f) != (1/-0.0f)}
} PASS

tcltest::test 15.15.4-double-1 { -NaN is still NaN } {
    constant_expression T15154d1 {-Double.NaN != -Double.NaN}
} PASS

tcltest::test 15.15.4-double-2 { - Infinity is Infinity of opposite sign } {
    constant_expression T15154d2 {-Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.15.4-double-3 { - 0.0 is zero of opposite sign } {
    constant_expression T15154d3 {(1/0.0) != (1/-0.0)}
} PASS
