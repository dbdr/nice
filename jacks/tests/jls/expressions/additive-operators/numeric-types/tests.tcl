# Commutivity, associativity

tcltest::test 15.18.2-commute-1 { side-effect-free addition is commutative } {
    constant_expression T15182commute1 {1 + 2 == 2 + 1}
} PASS

tcltest::test 15.18.2-commute-2 { side-effect-free addition is commutative } {
    constant_expression T15182commute2 {1L + 2L == 2L + 1L}
} PASS

tcltest::test 15.18.2-commute-3 { side-effect-free addition is commutative } {
    constant_expression T15182commute3 {1f + 2f == 2f + 1f}
} PASS

tcltest::test 15.18.2-commute-4 { side-effect-free addition is commutative } {
    constant_expression T15182commute4 {1d + 2d == 2d + 1d}
} PASS

tcltest::test 15.18.2-assoc-1 { integer addition is associative when
        all arguments are of the same type } {
    constant_expression T15182assoc1 {(1 + 2) + 3 == 1 + (2 + 3)}
} PASS

tcltest::test 15.18.2-assoc-2 { integer addition is not associative
        when arguments are of different types } {
    constant_expression T15182assoc2 \
        {(1L + 0x70000000) + 0x70000000 == 1L + (0x70000000 + 0x70000000)}
} FAIL

tcltest::test 15.18.2-assoc-3 { floating-point addition is not associative } {
    constant_expression T15182assoc3 \
        {(1e10f + 256f) + 256f == 1e10f + (256f + 256f)}
} FAIL

tcltest::test 15.18.2-assoc-4 { addition is left-associative } {
    constant_expression T15182assoc3 \
        {1e10f + 256f + 256f == (1e10f + 256f) + 256f}
} PASS

# Exceptional cases - rules for overflow, underflow, loss of information

tcltest::test 15.18.2-int-1 { overflow in integer addition simply 
        selects low-order bits, changing sign } {
    constant_expression T15182i1 {1073741824 + 1073741824 == -2147483648}
} PASS

tcltest::test 15.18.2-float-1 { NaN + NaN is NaN } {
    constant_expression T15182f1 \
        {Float.NaN + Float.NaN != Float.NaN + Float.NaN}
} PASS

tcltest::test 15.18.2-float-2 { NaN + anything is NaN } {
    constant_expression T15182f2 {Float.NaN + 1f != Float.NaN + 1f}
} PASS

tcltest::test 15.18.2-float-3 { the result of floating-point addition follows
        arithmetic sign rules } {
    constant_expression T15182f3 {-1f + -2f < 0f}
} PASS

tcltest::test 15.18.2-float-4 { the result of floating-point addition follows
        arithmetic sign rules } {
    constant_expression T15182f4 {-1.0f + 2.0f > 0f}
} PASS

tcltest::test 15.18.2-float-5 { Infinity + 0 = Infinity } {
    constant_expression T15182f5 \
        {Float.POSITIVE_INFINITY + -0.0 == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-6 { Infinity + finite = Same sign Infinity } {
    constant_expression T15182f6 \
        {Float.NEGATIVE_INFINITY + -1f == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-7 { Sum of Infinities of same sign is Infinity } {
    constant_expression T15182f7 \
	{Float.NEGATIVE_INFINITY + Float.NEGATIVE_INFINITY ==
	    Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-8 { Sum of Infinities of opposite sign is NaN } {
    constant_expression T15182f8 \
	{Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY !=
	    Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-9 { Sum of zeroes of same sign is same zero } {
    constant_expression T15182f9 {1.0f / (-0.0f + -0.0f) == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-10 { Sum of zeroes of opposite sign is +0 } {
    constant_expression T15182f10 {1.0f / (-0.0f + 0.0f) == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-11 { Sum of oppositely signed floating-point is +0 } {
    constant_expression T15182f11 {1.0f / (1.0f + -1.0f) == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-12 { 0.0 - x is not negation, for x = 0.0 } {
    constant_expression T15182f12 {1.0f / (0.0f - 0.0f) != Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-float-13 { a - b == a + (-b) } {
    constant_expression T15182f13 {1.0f - 2.0f == 1.0f + -2.0f}
} PASS

tcltest::test 15.18.2-float-14 { Floating-point addition takes place with
        infinite accuracy, then rounds to nearest } {
    constant_expression T15182f14 {0xfffff00 + 8.03125f == 0xfffff10}
} PASS

tcltest::test 15.18.2-float-15 { Floating-point addition takes place with
        infinite accuracy, then rounds to nearest } {
    constant_expression T15182f15 {-0xfffff00 - -8.03125f == -0xffffef0}
} PASS

tcltest::test 15.18.2-float-16 { Floating-point addition takes place with
        infinite accuracy, then rounds to nearest } {
    constant_expression T15182f16 {5.960465e-8f + 1 == 1.0000001f}
} PASS


tcltest::test 15.18.2-double-1 { NaN + NaN is NaN } {
    constant_expression T15182d1 \
        {Double.NaN + Double.NaN != Double.NaN + Double.NaN}
} PASS

tcltest::test 15.18.2-double-2 { NaN + anything is NaN } {
    constant_expression T15182d2 {Double.NaN + 1d != Double.NaN + 1d}
} PASS

tcltest::test 15.18.2-double-3 { the result of floating-point addition follows
        arithmetic sign rules } {
    constant_expression T15182d3 {-1d + -2d < 0d}
} PASS

tcltest::test 15.18.2-double-4 { the result of floating-point addition follows
        arithmetic sign rules } {
    constant_expression T15182d4 {-1.0 + 2.0 > 0.0}
} PASS

tcltest::test 15.18.2-double-5 { Infinity + 0 = Infinity } {
    constant_expression T15182d5 \
	{Double.POSITIVE_INFINITY + 0d == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-6 { Infinity + finite = Same sign Infinity } {
    constant_expression T15182d6 \
        {Double.NEGATIVE_INFINITY + -1d == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-7 { Sum of Infinities of same sign is Infinity } {
    constant_expression T15182d7 \
	{Double.NEGATIVE_INFINITY + Double.NEGATIVE_INFINITY ==
	    Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-8 { Sum of Infinities of opposite sign is NaN } {
    constant_expression T15182d8 \
	{Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY !=
	    Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-9 { Sum of zeroes of same sign is same zero } {
    constant_expression T15182d9 {1.0 / (-0.0 + -0.0) == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-10 { Sum of zeroes of opposite sign is +0 } {
    constant_expression T15182d10 {1.0 / (-0.0 + 0.0) == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-11 { Sum of oppositely signed floating-point is +0 } {
    constant_expression T15182d11 {1.0 / (1.0 + -1.0) == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-12 { 0.0 - x is not negation, for x = 0.0 } {
    constant_expression T15182d12 {1.0 / (0.0 - 0.0) != Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.18.2-double-13 { a - b == a + (-b) } {
    constant_expression T15182d13 {1.0 - 2.0 == 1.0 + -2.0}
} PASS

tcltest::test 15.18.2-double-14 { Floating-point addition takes place with
        infinite accuracy, then rounds to nearest } {
    constant_expression T15182d14 {0x1ffffffffffff00L + 8.03125 ==
        0x1ffffffffffff10L }
} PASS

tcltest::test 15.18.2-double-15 { Floating-point addition takes place with
        infinite accuracy, then rounds to nearest } {
    constant_expression T15182d15 {-0x1ffffffffffff00L - -8.03125 ==
        -0x1fffffffffffef0L }
} PASS

tcltest::test 15.18.2-double-16 { Floating-point addition takes place with
        infinite accuracy, then rounds to nearest, use of 80-bit
        extended precision instead of 64-bit double precision will fail
        for this test case } {
    constant_expression T15182d16 {1.1107651257113993e-16 + 1 ==
        1.0000000000000002}
} PASS

