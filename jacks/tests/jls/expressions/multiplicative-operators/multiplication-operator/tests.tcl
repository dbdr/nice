# Commutivity, associativity

tcltest::test 15.17.1-commute-1 { side-effect-free multiplication is commutative } {
    constant_expression T15171commute1 {1 * 2 == 2 * 1}
} PASS

tcltest::test 15.17.1-commute-2 { side-effect-free multiplication is commutative } {
    constant_expression T15171commute2 {1L * 2L == 2L * 1L}
} PASS

tcltest::test 15.17.1-commute-3 { side-effect-free multiplication is commutative } {
    constant_expression T15171commute3 {1f * 2f == 2f * 1f}
} PASS

tcltest::test 15.17.1-commute-4 { side-effect-free multiplication is commutative } {
    constant_expression T15171commute4 {1d * 2d == 2d * 1d}
} PASS

tcltest::test 15.17.1-assoc-1 { integer multiplication is associative when
        all arguments are of the same type } {
    constant_expression T15171assoc1 {(1 * 2) * 3 == 1 * (2 * 3)}
} PASS

tcltest::test 15.17.1-assoc-2 { integer multiplication is not associative
        when arguments are of different types } {
    constant_expression T15171assoc2 \
        {(1L * 0x70000000) * 20 == 1L * (0x70000000 * 20)}
} FAIL

tcltest::test 15.17.1-assoc-3 { floating-point multiplication is not associative } {
    constant_expression T15171assoc3 \
        {(1e308 * 100.0) * .01 == 1e308 * (100.0 * .01)}
} FAIL

tcltest::test 15.17.1-assoc-4 { multiplication is left-associative } {
    constant_expression T15171assoc4 \
        {1e308 * 100.0 * .01 == (1e308 * 100.0) * .01}
} PASS

# Exceptional cases - rules for overflow, underflow, loss of information

tcltest::test 15.17.1-float-1 { NaN * NaN is NaN } {
    constant_expression T15171f1 \
        {Float.NaN * Float.NaN != Float.NaN * Float.NaN}
} PASS

tcltest::test 15.17.1-float-2 { NaN * anything is NaN } {
    constant_expression T15171f2 {Float.NaN * 1f != Float.NaN * 1f}
} PASS

tcltest::test 15.17.1-float-3 { the result is positive in floating-point
        multiplication if both operands have the same sign } {
    constant_expression T15171f3 {-1f * -2f > 0f} {1f * 2f > 0f}
} PASS

tcltest::test 15.17.1-float-4 { the result is negative in floating-point
        multiplication if the operands have opposite sign } {
    constant_expression T15171f4 {-1f * 2f < 0f} {1f * -2f < 0f}
} PASS

tcltest::test 15.17.1-float-5 { Infinity * 0 is NaN } {
    constant_expression T15171f5 \
	{Float.POSITIVE_INFINITY * 0f != Float.POSITIVE_INFINITY * 0f}
} PASS

tcltest::test 15.17.1-float-6 { Infinity * finite == Infinity of correct sign } {
    constant_expression T15171f6 \
        {Float.NEGATIVE_INFINITY * -1f == Float.POSITIVE_INFINITY} \
        {Float.POSITIVE_INFINITY * -1f == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-float-7 { Infinity * Infinity == Infinity of correct sign
        (Specified in JVMS errata) } {
    constant_expression T15171f7 \
	{Float.NEGATIVE_INFINITY * Float.NEGATIVE_INFINITY == Float.POSITIVE_INFINITY} \
	{Float.POSITIVE_INFINITY * Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.1-float-8 { Infinity * Infinity == Infinity of correct sign
        (Specified in JVMS errata) } {
    constant_expression T15171f8 \
	{Float.NEGATIVE_INFINITY * Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY} \
	{Float.POSITIVE_INFINITY * Float.NEGATIVE_INFINITY == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-float-9 { Floating-point * produces Infinity for overflow } {
    constant_expression T15171f9 \
            {1.0e30f * 1.0e30f == Float.POSITIVE_INFINITY} \
            {1.0e30f * -1.0e30f == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-float-10 { Floating-point * supports gradual underflow } {
    constant_expression T15171f10 \
            {1e-22f * 1e-22f == 1e-44f} \
            {1e-22f * -1e-22f == -1e-44f}
} PASS

tcltest::test 15.17.1-float-11 { Floating-point * produces 0 for underflow 
        (check 1/0 to determine sign) } {
    constant_expression T15171f11 \
            {1e-30f * 1e-30f == 0} \
            {1/(1e-30f * 1e-30f) == Float.POSITIVE_INFINITY} \
            {1e-30f * -1e-30f == 0} \
            {1/(1e-30f * -1e-30f) == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-float-12 { Floating-point * works even with denorms } {
    constant_expression T15171f12 \
            {1e30f * 1e-40f == 9.999946e-11f} \
            {1e30f * -1e-40f == -9.999946e-11f} \
            {1e-40f * -1e30f == -9.999946e-11f} \
            {-1e-40f * -1e30f == 9.999946e-11f}
} PASS

tcltest::test 15.17.1-float-13 { Floating-point * follows round-to-nearest rules } {
    constant_expression T15171f13 \
            {0x800001 * 3f == 0x1800004} \
            {0x800001 * 5f == 0x2800004} \
            {0x800003 * 5f == 0x2800010} \
            {0x800002 * 5f == 0x2800008}
} PASS

tcltest::test 15.17.1-float-14 { Floating-point * follows round-to-nearest,
        even with denorms } {
    constant_expression T15171f14 \
            {2.1302879E-19f * 5.4316195E-20f == 1.1570912E-38f}
} PASS


tcltest::test 15.17.1-double-1 { NaN * NaN is NaN } {
    constant_expression T15171d1 \
        {Double.NaN * Double.NaN != Double.NaN * Double.NaN}
} PASS

tcltest::test 15.17.1-double-2 { NaN * anything is NaN } {
    constant_expression T15171d2 {Double.NaN * 1d != Double.NaN * 1d}
} PASS

tcltest::test 15.17.1-double-3 { the result is positive in floating-point
        multiplication if both operands have the same sign } {
    constant_expression T15171d3 {-1d * -2d > 0d} {1d * 2d > 0d}
} PASS

tcltest::test 15.17.1-double-4 { the result is negative in floating-point
        multiplication if the operands have opposite sign } {
    constant_expression T15171d4 {-1.0 * 2.0 < 0.0} {1.0 * -2.0 < 0.0}
} PASS

tcltest::test 15.17.1-double-5 { Infinity * 0 is NaN } {
    constant_expression T15171d5 \
	{Double.POSITIVE_INFINITY * 0d != Double.POSITIVE_INFINITY * 0d}
} PASS

tcltest::test 15.17.1-double-6 { Infinity * finite == Infinity of correct sign } {
    constant_expression T15171d6 \
        {Double.NEGATIVE_INFINITY * -1d == Double.POSITIVE_INFINITY} \
        {Double.POSITIVE_INFINITY * -1d == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-double-7 { Infinity * Infinity == Infinity of correct sign
        (Specified in JVMS errata) } {
    constant_expression T15171d7 \
	{Double.NEGATIVE_INFINITY * Double.NEGATIVE_INFINITY == Double.POSITIVE_INFINITY} \
	{Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.1-double-8 { Infinity * Infinity == Infinity of correct sign
        (Specified in JVMS errata) } {
    constant_expression T15171d8 \
	{Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY} \
	{Double.POSITIVE_INFINITY * Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-double-9 { Floating-point * produces Infinity for overflow } {
    constant_expression T15171d9 \
            {1.0e160 * 1.0e160 == Double.POSITIVE_INFINITY} \
            {1.0e160 * -1.0e160 == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-double-10 { Floating-point * supports gradual underflow } {
    constant_expression T15171d10 \
            {1e-160 * 1e-160 == 1e-320} \
            {1e-160 * -1e-160 == -1e-320}
} PASS

tcltest::test 15.17.1-double-11 { Floating-point * produces 0 for underflow 
        (check 1/0 to determine sign) } {
    constant_expression T15171d11 \
            {1e-170 * 1e-170 == 0} \
            {1/(1e-170 * 1e-170) == Double.POSITIVE_INFINITY} \
            {1e-170 * -1e-170 == 0} \
            {1/(1e-170 * -1e-170) == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.1-double-12 { Floating-point * works even with denorms } {
    constant_expression T15171d12 \
            {1e300 * 1e-320 == 9.99988867182683E-21} \
            {1e300 * -1e-320 == -9.99988867182683E-21} \
            {1e-320 * -1e300 == -9.99988867182683E-21} \
            {-1e-320 * -1e300 == 9.99988867182683E-21}
} PASS

tcltest::test 15.17.1-double-13 { Floating-point * follows round-to-nearest rules } {
    constant_expression T15171d13 \
            {0x10000000000001L * 3d == 0x30000000000004L} \
            {0x10000000000001L * 5d == 0x50000000000004L} \
            {0x10000000000003L * 5d == 0x50000000000010L} \
            {0x10000000000002L * 5d == 0x50000000000008L}
} PASS

tcltest::test 15.17.1-double-14 { Floating-point * follows round-to-nearest,
        even with denorms, known bug in Sun's javac } {
    constant_expression T15171d14 \
        {8.578459548793971E-162 * 2.512418001798401E-147
            == 2.155267619808936E-308} \
        {6.946121092140867E-162 * 2.669444126910801E-147
            == 1.8542282154226677E-308}
} PASS


#integer tests

tcltest::test 15.17.1-int-1 { overflow in integer multiplication, simply 
        selects low-order bits, possibly changing sign } {
    constant_expression T15171i1 {1234567890 * 10 == -539222988}
} PASS

tcltest::test 15.17.1-int-2 { constant expression with int multiplication } {
    constant_expression T15171i2 \
        {-12300000 == 100000 * -123} \
        {-50000000 == 500 * -100000}
} PASS

tcltest::test 15.17.1-long-1 { constant expression with long multiplication } {
    constant_expression T15171l1 \
        {-12192592592745L == 987654321L * -12345} \
        {-12192592592745L == 12345 * -987654321L} \
        {12192592592745L == 987654321L * 12345} \
        {12192592592745L == -987654321L * -12345}
} PASS

tcltest::test 15.17.1-long-2 { constant expression with long multiplication } {
    constant_expression T15171l2 {-2L*(1L<<28) == 0xffffffffe0000000L}
} PASS
