tcltest::test 15.15.3-type-1 { + must have numeric operand } {
    is_assignable_to T15153t1 Object +null
} FAIL

tcltest::test 15.15.3-type-2 { + must have numeric operand } {
    is_assignable_to T15153t2 boolean +true
} FAIL

tcltest::test 15.15.3-type-3 { + must have numeric operand } {
    is_assignable_to T15153t3 Object {+(new Object())}
} FAIL

tcltest::test 15.15.3-type-4 { + must have numeric operand } {
    is_assignable_to T15153t4 int +System.out.println()
} FAIL

tcltest::test 15.15.3-type-5 { + performs unary promotion } {
#is_assignable_to creates byte n1, byte n2
    is_assignable_to T15153t5 byte 1 byte +n1
} FAIL

tcltest::test 15.15.3-type-6 { + must have numeric operand } {
    is_assignable_to T15153t6 int +1
} PASS

tcltest::test 15.15.3-type-7 { + must have numeric operand } {
    is_assignable_to T15153t7 long +1L
} PASS

tcltest::test 15.15.3-type-8 { + must have numeric operand } {
    is_assignable_to T15153t8 float +1f
} PASS

tcltest::test 15.15.3-type-9 { + must have numeric operand } {
    is_assignable_to T15153t9 double +1d
} PASS

tcltest::test 15.15.3-type-10 { + must have numeric operand } {
    is_assignable_to T15153t10 short +1
} PASS

tcltest::test 15.15.3-type-11 { + must have numeric operand } {
    is_assignable_to T15153t11 char +'1'
} PASS

tcltest::test 15.15.3-type-12 { + must have numeric operand } {
    is_assignable_to T15153t12 byte +1
} PASS
