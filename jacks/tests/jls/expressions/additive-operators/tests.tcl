tcltest::test 15.18-null-1 { at least one argument of + should be String,
        or else both must be primitive numeric type } {
    # Sun seems to have relaxed this so that null is used as type String
    is_assignable_to T1518n1 String {null + null}
} FAIL

tcltest::test 15.18-null-2 { both arguments to - must be of a
        primitive numeric type } {
    is_assignable_to T1518n2 String {null - null}
} FAIL

tcltest::test 15.18-boolean-1 { at least one argument of + should be String,
        or else both must be primitive numeric type } {
    is_assignable_to T1518b1 boolean {true + false}
} FAIL

tcltest::test 15.18-boolean-2 { both arguments to - must be of a
        primitive numeric type } {
    is_assignable_to T1518b2 boolean {5 - false}
} FAIL
