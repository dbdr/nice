# associativity

tcltest::test 15.17.2-assoc-1 { division is left-associative } {
    constant_expression T15172assoc1 \
            {10 / 3 / 2 == 1} {10 / (3 / 2) == 10}
} PASS

tcltest::test 15.17.2-assoc-2 { division is left-associative } {
    constant_expression T15172assoc2 \
            {10.0 / 3.0 / 2.0 == 1.6666666666666667} \
            {10.0 / (3.0 / 2.0) == 6.666666666666667}
} PASS

# Exceptional cases - rules for overflow, underflow, loss of information

tcltest::test 15.17.2-float-1 { NaN / NaN is NaN } {
    constant_expression T15172f1 \
        {Float.NaN / Float.NaN != Float.NaN / Float.NaN}
} PASS

tcltest::test 15.17.2-float-2 { NaN / anything is NaN } {
    constant_expression T15172f2 \
        {Float.NaN / 1f != Float.NaN / 1f} \
        {1f / Float.NaN != 1f / Float.NaN}
} PASS

tcltest::test 15.17.2-float-3 { the result is positive in floating-point
        division if both operands have the same sign } {
    constant_expression T15172f3 {-1f / -2f > 0f} {1f / 2f > 0f}
} PASS

tcltest::test 15.17.2-float-4 { the result is negative in floating-point
        division if the operands have opposite sign } {
    constant_expression T15172f4 {-1f / 2f < 0f} {1f / -2f < 0f}
} PASS

tcltest::test 15.17.2-float-5 { 0 / 0 is NaN } {
    constant_expression T15172f5 \
        {0f / 0f != 0f / 0f} \
        {-0f / 0f != -0f / 0f}
} PASS

tcltest::test 15.17.2-float-6 { Infinity / finite == Infinity of correct sign } {
    constant_expression T15172f6 \
        {Float.NEGATIVE_INFINITY / -1f == Float.POSITIVE_INFINITY} \
        {Float.POSITIVE_INFINITY / -1f == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-7 { Infinity / Infinity is NaN } {
    constant_expression T15172f7 \
        {Float.NEGATIVE_INFINITY / Float.NEGATIVE_INFINITY !=
            Float.NEGATIVE_INFINITY / Float.NEGATIVE_INFINITY} \
        {Float.POSITIVE_INFINITY / Float.NEGATIVE_INFINITY !=
            Float.POSITIVE_INFINITY / Float.NEGATIVE_INFINITY} \
        {Float.NEGATIVE_INFINITY / Float.POSITIVE_INFINITY !=
            Float.NEGATIVE_INFINITY / Float.POSITIVE_INFINITY} \
        {Float.POSITIVE_INFINITY / Float.POSITIVE_INFINITY !=
            Float.POSITIVE_INFINITY / Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-8 { Finite / Infinity == Signed zero
        (check 1/0 to determine sign) } {
    constant_expression T15172f8 \
        {1f / Float.NEGATIVE_INFINITY == 0} \
        {1/(1f / Float.NEGATIVE_INFINITY) == Float.NEGATIVE_INFINITY} \
        {1f / Float.POSITIVE_INFINITY == 0} \
        {1/(1f / Float.POSITIVE_INFINITY) == Float.POSITIVE_INFINITY} \
        {-1f / Float.NEGATIVE_INFINITY == 0} \
        {1/(-1f / Float.NEGATIVE_INFINITY) == Float.POSITIVE_INFINITY} \
        {-1f / Float.POSITIVE_INFINITY == 0} \
        {1/(-1f / Float.POSITIVE_INFINITY) == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-9 { Finite / 0 == Signed infinity } {
    constant_expression T15172f9 \
        {1f / 0f == Float.POSITIVE_INFINITY} \
        {-1f / 0f == Float.NEGATIVE_INFINITY} \
        {1f / -0f == Float.NEGATIVE_INFINITY} \
        {-1f / -0f == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-10 { 0 / non-zero Finite == Signed zero
        (check 1/0 to determine sign) } {
    constant_expression T15172f10 \
        {0f / 1f == 0} {1/(0f / 1f) == Float.POSITIVE_INFINITY} \
        {-0f / 1f == 0} {1/(-0f / 1f) == Float.NEGATIVE_INFINITY} \
        {0f / -1f == 0} {1/(0f / -1f) == Float.NEGATIVE_INFINITY} \
        {-0f / -1f == 0} {1/(-0f / -1f) == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-11 { Floating-point / produces Infinity for overflow } {
    constant_expression T15172f11 \
            {1.0e30f / 1.0e-30f == Float.POSITIVE_INFINITY} \
            {1.0e30f / -1.0e-30f == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-12 { Floating-point / supports gradual underflow } {
    constant_expression T15172f12 \
            {1e-22f / 1e22f == 1e-44f} \
            {1e-22f / -1e22f == -1e-44f}
} PASS

tcltest::test 15.17.2-float-13 { Floating-point / produces 0 for underflow 
        (check 1/0 to determine sign) } {
    constant_expression T15172f13 \
            {1e-30f / 1e30f == 0} \
            {1/(1e-30f / 1e30f) == Float.POSITIVE_INFINITY} \
            {1e-30f / -1e30f == 0} \
            {1/(1e-30f / -1e30f) == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-float-14 { Floating-point / works even with denorms } {
    constant_expression T15172f14 \
            {1e-10f / 1e-40f == 1.0000054E30f} \
            {1e-10f / -1e-40f == -1.0000054E30f} \
            {1e-40f / -1e-10f == -9.999946E-31f} \
            {-1e-40f / -1e-10f == 9.999946E-31f}
} PASS

tcltest::test 15.17.2-float-15 { Floating-point / follows round-to-nearest rules } {
    constant_expression T15172f15 \
            {0x1800004 / 3f == 0x800001} \
            {0x1800008 / 3f == 0x800003} \
            {7e-45f / 2f == 2.8e-45f} \
            {9.8e-45f / 2f == 5.6e-45f}
} PASS


tcltest::test 15.17.2-double-1 { NaN / NaN is NaN } {
    constant_expression T15172d1 \
        {Double.NaN / Double.NaN != Double.NaN / Double.NaN}
} PASS

tcltest::test 15.17.2-double-2 { NaN / anything is NaN } {
    constant_expression T15172d2 \
            {Double.NaN / 1d != Double.NaN / 1d} \
            {1d / Double.NaN != 1d / Double.NaN}
} PASS

tcltest::test 15.17.2-double-3 { the result is positive in floating-point
        division if both operands have the same sign } {
    constant_expression T15172d3 {-1d / -2d > 0d} {1d / 2d > 0d}
} PASS

tcltest::test 15.17.2-double-4 { the result is negative in floating-point
        division if the operands have opposite sign } {
    constant_expression T15172d4 {-1.0 / 2.0 < 0.0} {1.0 / -2.0 < 0.0}
} PASS

tcltest::test 15.17.2-double-5 { 0 / 0 is NaN } {
    constant_expression T15172d5 \
        {0d / 0d != 0d / 0d} \
        {-0d / 0d != -0d / 0d}
} PASS

tcltest::test 15.17.2-double-6 { Infinity / finite == Infinity of correct sign } {
    constant_expression T15172d6 \
        {Double.NEGATIVE_INFINITY / -1d == Double.POSITIVE_INFINITY} \
        {Double.POSITIVE_INFINITY / -1d == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-7 { Infinity / Infinity is NaN } {
    constant_expression T15172d7 \
        {Double.NEGATIVE_INFINITY / Double.NEGATIVE_INFINITY !=
            Double.NEGATIVE_INFINITY / Double.NEGATIVE_INFINITY} \
        {Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY !=
            Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY} \
        {Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY !=
            Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY} \
        {Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY !=
            Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-8 { Finite / Infinity == Signed zero
        (check 1/0 to determine sign) } {
    constant_expression T15172d8 \
        {1d / Double.NEGATIVE_INFINITY == 0} \
        {1/(1d / Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY} \
        {1d / Double.POSITIVE_INFINITY == 0} \
        {1/(1d / Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY} \
        {-1d / Double.NEGATIVE_INFINITY == 0} \
        {1/(-1d / Double.NEGATIVE_INFINITY) == Double.POSITIVE_INFINITY} \
        {-1d / Double.POSITIVE_INFINITY == 0} \
        {1/(-1d / Double.POSITIVE_INFINITY) == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-9 { Finite / 0 == Signed infinity } {
    constant_expression T15172d9 \
        {1d / 0d == Double.POSITIVE_INFINITY} \
        {-1d / 0d == Double.NEGATIVE_INFINITY} \
        {1d / -0d == Double.NEGATIVE_INFINITY} \
        {-1d / -0d == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-10 { 0 / non-zero Finite == Signed zero
        (check 1/0 to determine sign) } {
    constant_expression T15172d10 \
        {0d / 1d == 0} {1/(0d / 1d) == Double.POSITIVE_INFINITY} \
        {-0d / 1d == 0} {1/(-0d / 1d) == Double.NEGATIVE_INFINITY} \
        {0d / -1d == 0} {1/(0d / -1d) == Double.NEGATIVE_INFINITY} \
        {-0d / -1d == 0} {1/(-0d / -1d) == Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-11 { Floating-point / produces Infinity for overflow } {
    constant_expression T15172d11 \
            {1.0e160 / 1.0e-160 == Double.POSITIVE_INFINITY} \
            {1.0e160 / -1.0e-160 == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-12 { Floating-point / supports gradual underflow } {
    constant_expression T15172d12 \
            {1e-160 / 1e160 == 1e-320} \
            {1e-160 / -1e160 == -1e-320}
} PASS

tcltest::test 15.17.2-double-13 { Floating-point / produces 0 for underflow 
        (check 1/0 to determine sign) } {
    constant_expression T15172d13 \
            {1e-170 / 1e170 == 0} \
            {1/(1e-170 / 1e170) == Double.POSITIVE_INFINITY} \
            {1e-170 / -1e170 == 0} \
            {1/(1e-170 / -1e170) == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.2-double-14 { Floating-point / works even with denorms } {
    constant_expression T15172d14 \
            {1e-20 / 1e-320 == 1.0000111329412579e300} \
            {1e-20 / -1e-320 == -1.0000111329412579e300} \
            {1e-320 / -1e-20 == -9.999888671826831E-301} \
            {-1e-320 / -1e-20 == 9.999888671826831E-301}
} PASS

tcltest::test 15.17.2-double-15 { Floating-point / follows round-to-nearest rules } {
    constant_expression T15172d15 \
            {0x30000000000004L / 3d == 0x10000000000001L} \
            {0x30000000000008L / 3d == 0x10000000000003L} \
            {2.5e-323 / 2d == 1e-323} \
            {3.5e-323 / 2d == 2e-323}
} PASS

# integer tests

tcltest::test 15.17.2-int-1 { integer division rounds to 0 } {
    constant_expression T15172i1 \
            {5 / 3 == 1} \
            {5 / -3 == -1} \
            {-5 / 3 == -1} \
            {-5 / -3 == 1}
} PASS

tcltest::test 15.17.2-int-2 { MIN_INT / -1 overflows } {
    constant_expression T15172i2 {0x80000000 / -1 == 0x80000000}
} PASS

tcltest::test 15.17.2-int-3 { Compile-time error to divide by constant 0 } {
    empty_class T15172i3 {int i = 1 / 0;}
} FAIL

tcltest::test 15.17.2-long-1 { integer division rounds to 0 } {
    constant_expression T15172l1 \
            {5L / 3L == 1L} \
            {5L / -3L == -1L} \
            {-5L / 3L == -1L} \
            {-5L / -3L == 1L}
} PASS

tcltest::test 15.17.2-long-2 { MIN_LONG / -1 overflows } {
    constant_expression T15172l2 {0x8000000000000000L / -1L == 0x8000000000000000L}
} PASS

tcltest::test 15.17.2-long-3 { Compile-time error to divide by constant 0 } {
    empty_class T15172l3 {long l = 1L / 0L;}
} FAIL

