tcltest::test 15.15-group-1 { Unary operators group right-to-left } {
    constant_expression T15151 {-~0xffffffff == 0} \
                               {~-0xffffffff == 0xfffffffe}
} PASS

tcltest::test 15.15-precedence-1 { Unary operators have higher
        precedence than multiplicative } {
    is_assignable_to T1515p1 float {(float) 1/1.0}
} FAIL

tcltest::test 15.15-precedence-2 { Unary operators have higher
        precedence than multiplicative } {
    is_assignable_to T1515p2 float {(float) (1/1.0)}
} PASS

tcltest::test 15.15-precedence-3 { Unary operators have higher
        precedence than multiplicative } {
    constant_expression T1515p3 {-0x80000000/2 == 0xc0000000} \
                                {-(0x80000000/2) == 0x40000000}
} PASS

tcltest::test 15.15-precedence-4 { Unary operators have higher
        precedence than multiplicative } {
    constant_expression T1515p4 {~0xffffffff*2 == 0} \
                                {~(0xffffffff*2) == 1}
} PASS

tcltest::test 15.15-precedence-5 { Unary operators have higher
        precedence than addition, for boolean } {
    constant_expression T1515p5 {!true + "" == "false"}
} PASS
