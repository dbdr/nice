tcltest::test 15.15.5-type-1 { ~ must have integral operand } {
    is_assignable_to T15155t1 Object ~null
} FAIL

tcltest::test 15.15.5-type-2 { ~ must have integral operand } {
    is_assignable_to T15155t2 boolean ~true
} FAIL

tcltest::test 15.15.5-type-3 { ~ must have integral operand } {
    is_assignable_to T15155t3 Object {~(new Object())}
} FAIL

tcltest::test 15.15.5-type-4 { ~ must have integral operand } {
    is_assignable_to T15155t4 int ~System.out.println()
} FAIL

tcltest::test 15.15.5-type-5 { ~ must have integral operand } {
    is_assignable_to T15155t5 int ~0.0f
} FAIL

tcltest::test 15.15.5-type-6 { ~ must have integral operand } {
    is_assignable_to T15155t6 int ~0.0d
} FAIL

tcltest::test 15.15.5-type-7 { ~ performs unary promotion } {
#is_assignable_to creates byte n1, byte n2
    is_assignable_to T15155t7 byte 1 byte ~n1
} FAIL

tcltest::test 15.15.5-type-8 { ~ must have integral operand } {
    is_assignable_to T15155t8 short ~1
} PASS

tcltest::test 15.15.5-type-9 { ~ must have integral operand } {
    is_assignable_to T15155t9 char ~(~'1')
} PASS

tcltest::test 15.15.5-type-10 { ~ must have integral operand } {
    is_assignable_to T15155t10 byte ~1
} PASS


tcltest::test 15.15.5-int-1 { ~x = -x-1 for all ints } {
    constant_expression T15155i1 {~0 == -0-1}
} PASS

tcltest::test 15.15.5-int-2 { ~x = -x-1 for all ints } {
    constant_expression T15155i2 {~0xffffffff == -0xffffffff-1}
} PASS

tcltest::test 15.15.5-long-1 { ~x = -x-1 for all longs } {
    constant_expression T15155l1 {~0L == -0L-1}
} PASS

tcltest::test 15.15.5-long-2 { ~x = -x-1 for all longs } {
    constant_expression T15155l2 {~0xffffffffffffffffL == -0xffffffffffffffffL-1}
} PASS
