tcltest::test 15.15.6-type-1 { ! must have boolean operand } {
    is_assignable_to T15156t1 Object !null
} FAIL

tcltest::test 15.15.6-type-2 { ! must have boolean operand } {
    is_assignable_to T15156t2 Object {!(new Object())}
} FAIL

tcltest::test 15.15.6-type-3 { ! must have boolean operand } {
    is_assignable_to T15156t3 int !System.out.println()
} FAIL

tcltest::test 15.15.6-type-4 { ! must have boolean operand } {
    is_assignable_to T15156t4 int !0.0f
} FAIL

tcltest::test 15.15.6-type-5 { ! must have boolean operand } {
    is_assignable_to T15156t5 int !0.0d
} FAIL

tcltest::test 15.15.6-type-6 { ! must have boolean operand } {
    is_assignable_to T15156t6 short !1
} FAIL

tcltest::test 15.15.6-type-7 { ! must have boolean operand } {
    is_assignable_to T15156t7 char !'1'
} FAIL

tcltest::test 15.15.6-type-8 { ! must have boolean operand } {
    is_assignable_to T15156t8 byte !1
} FAIL

tcltest::test 15.15.6-boolean-1 { ! true is false } {
    constant_expression T15156b1 {!true == false}
} PASS

tcltest::test 15.15.6-boolean-2 { ! false is true } {
    constant_expression T15156b2 {!false == true}
} PASS

