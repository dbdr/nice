tcltest::test 15.21.1-promote-1 { Binary promotion is done for numeric == } {
    # Note the same bit patterns
    constant_expression T15211p1 {(char)0xffff != (short)0xffff}
} PASS

tcltest::test 15.21.1-promote-2 { Binary promotion is done for numeric == } {
    # Note the different bit patterns
    constant_expression T15211p2 {(short)0xffff == (byte)0xff}
} PASS

tcltest::test 15.21.1-promote-3 { Binary promotion is done for numeric == } {
    # Note the same decimal starting point
    constant_expression T15211p3 {.1f != .1}
} PASS

tcltest::test 15.21.1-floating-1 { In floating point, -0 == 0 } {
    constant_expression T15211f1 {0. == -0.} {!(0. != -0.)}
} PASS

tcltest::test 15.21.1-floating-2 { In floating point, -0 == 0 } {
    constant_expression T15211f2 {0f == -0f} {!(0f != -0f)}
} PASS

tcltest::test 15.21.1-floating-3 { In floating point, NaN is never == } {
    constant_expression T15211f3 {Double.NaN != Double.NaN} \
	    {!(Double.NaN == Double.NaN)} \
	    {Float.NaN != Float.NaN} \
	    {!(Float.NaN == Float.NaN)}
} PASS

tcltest::test 15.21.1-floating-4 { In floating point, NaN is never == } {
    constant_expression T15211f4 {Double.NaN != 0} \
	    {!(Double.NaN == 0)} \
	    {Double.NaN != -0.} \
	    {!(Double.NaN == -0.)}
} PASS

tcltest::test 15.21.1-floating-5 { In floating point, NaN is never == } {
    constant_expression T15211f5 {Float.NaN != 0} \
	    {!(Float.NaN == 0)} \
	    {Float.NaN != -0f} \
	    {!(Float.NaN == -0f)}
} PASS

tcltest::test 15.21.1-floating-6 { In floating point, NaN is never == } {
    constant_expression T15211f6 {Double.NaN != Double.POSITIVE_INFINITY} \
	    {!(Double.NaN == Double.POSITIVE_INFINITY)} \
	    {Double.NaN != Double.NEGATIVE_INFINITY} \
	    {!(Double.NaN == Double.NEGATIVE_INFINITY)}
} PASS

tcltest::test 15.21.1-floating-7 { In floating point, NaN is never == } {
    constant_expression T15211f7 {Float.NaN != Float.POSITIVE_INFINITY} \
	    {!(Float.NaN == Float.POSITIVE_INFINITY)} \
	    {Float.NaN != Float.NEGATIVE_INFINITY} \
	    {!(Float.NaN == Float.NEGATIVE_INFINITY)}
} PASS

tcltest::test 15.21.1-floating-8 { In floating point, infinities compare only
        to themselves } {
    constant_expression T15211f8 \
	    {Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY} \
	    {Double.POSITIVE_INFINITY != Double.NEGATIVE_INFINITY} \
	    {Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY} \
	    {Double.POSITIVE_INFINITY != 0} \
	    {Double.NEGATIVE_INFINITY != -0.}
} PASS

tcltest::test 15.21.1-floating-9 { In floating point, infinities compare only
        to themselves } {
    constant_expression T15211f9 \
	    {Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY} \
	    {Float.POSITIVE_INFINITY != Float.NEGATIVE_INFINITY} \
	    {Float.NEGATIVE_INFINITY == Float.NEGATIVE_INFINITY} \
	    {Float.POSITIVE_INFINITY != 0} \
	    {Float.NEGATIVE_INFINITY != -0f}
} PASS

