# associativity

tcltest::test 15.17.3-assoc-1 { remainder is left-associative } {
    constant_expression T15173assoc1 \
            {10 % 4 % 3 == 2} {10 % (4 % 3) == 0}
} PASS

tcltest::test 15.17.3-assoc-2 { remainder is left-associative } {
    constant_expression T15173assoc2 \
            {10.0 % 4.0 % 3.0 == 2.0} \
            {10.0 % (4.0 % 3.0) == 0.0}
} PASS

# Exceptional cases - rules for overflow, underflow, loss of information

tcltest::test 15.17.3-float-1 { NaN % NaN is NaN } {
    constant_expression T15173f1 \
        {Float.NaN % Float.NaN != Float.NaN % Float.NaN}
} PASS

tcltest::test 15.17.3-float-2 { NaN % anything is NaN } {
    constant_expression T15173f2 \
        {Float.NaN % 1f != Float.NaN % 1f} \
        {1f % Float.NaN != 1f % Float.NaN}
} PASS

tcltest::test 15.17.3-float-3 { the result is positive in floating-point
        remainder if the dividend is positive } {
    constant_expression T15173f3 {3f % -2f > 0f} {3f % 2f > 0f}
} PASS

tcltest::test 15.17.3-float-4 { the result is negative in floating-point
        remainder if the dividend is negative } {
    constant_expression T15173f4 {-3f % 2f < 0f} {-3f % -2f < 0f}
} PASS

tcltest::test 15.17.3-float-5 { 0 % 0 is NaN } {
    constant_expression T15173f5 \
        {0f % 0f != 0f % 0f} \
        {-0f % 0f != -0f % 0f}
} PASS

tcltest::test 15.17.3-float-6 { Infinity % finite is NaN } {
    constant_expression T15173f6 \
        {Float.NEGATIVE_INFINITY % -1f != Float.NEGATIVE_INFINITY % -1f} \
        {Float.POSITIVE_INFINITY % -1f != Float.POSITIVE_INFINITY % -1f}
} PASS

tcltest::test 15.17.3-float-7 { Infinity % Infinity is NaN } {
    constant_expression T15173f7 \
        {Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY !=
            Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY} \
        {Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY !=
            Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY} \
        {Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY !=
            Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY} \
        {Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY !=
            Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.3-float-8 { Finite % Infinity == dividend } {
    constant_expression T15173f8 \
        {1f % Float.NEGATIVE_INFINITY == 1f} \
        {1f % Float.POSITIVE_INFINITY == 1f} \
        {-1f % Float.NEGATIVE_INFINITY == -1f} \
        {-1f % Float.POSITIVE_INFINITY == -1f}
} PASS

tcltest::test 15.17.3-float-9 { Finite % 0 is NaN } {
    constant_expression T15173f9 \
        {1f % 0f != 1f % 0f} \
        {-1f % 0f != -1f % 0f} \
        {1f % -0f != 1f % -0f} \
        {-1f % -0f != -1f % -0f}
} PASS

tcltest::test 15.17.3-float-10 { 0 % non-zero Finite == Dividend
        (check 1/0 to determine sign) } {
    constant_expression T15173f10 \
        {0f % 1f == 0} {1/(0f % 1f) == Float.POSITIVE_INFINITY} \
        {-0f % 1f == 0} {1/(-0f % 1f) == Float.NEGATIVE_INFINITY} \
        {0f % -1f == 0} {1/(0f % -1f) == Float.POSITIVE_INFINITY} \
        {-0f % -1f == 0} {1/(-0f % -1f) == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.3-float-11 { 0 % Infinity == Dividend
        (check 1/0 to determine sign) } {
    constant_expression T15173f11 \
        {0f % Float.POSITIVE_INFINITY == 0} \
        {1/(0f % Float.POSITIVE_INFINITY) == Float.POSITIVE_INFINITY} \
        {-0f % Float.POSITIVE_INFINITY == 0} \
        {1/(-0f % Float.POSITIVE_INFINITY) == Float.NEGATIVE_INFINITY} \
        {0f % Float.NEGATIVE_INFINITY == 0} \
        {1/(0f % Float.NEGATIVE_INFINITY) == Float.POSITIVE_INFINITY} \
        {-0f % Float.NEGATIVE_INFINITY == 0} \
        {1/(-0f % Float.NEGATIVE_INFINITY) == Float.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.3-float-12 { Floating-point % cannot overflow } {
    constant_expression T15173f12 \
            {1e30f % 1e-30f == 8.166816e-31f} \
            {-1e30f % -1e-30f == -8.166816e-31f}
} PASS

tcltest::test 15.17.3-float-13 { Floating-point % works even with denorms } {
    constant_expression T15173f13 \
            {1e30f % 1e-40f == 7.121e-42f} \
            {1e30f % -1e-40f == 7.121e-42f} \
            {-1e-40f % 1e30f == -1e-40f} \
            {-1e-40f % -1e30f == -1e-40f}
} PASS

tcltest::test 15.17.3-float-14 { Example cases } {
    constant_expression T15173f14 \
            {5.0f % 3.0f == 2.0f} \
            {5.0f % -3.0f == 2.0f} \
            {-5.0f % 3.0f == -2.0f} \
            {-5.0f % -3.0f == -2.0f}
} PASS

tcltest::test 15.17.3-double-1 { NaN % NaN is NaN } {
    constant_expression T15173d1 \
        {Double.NaN % Double.NaN != Double.NaN % Double.NaN}
} PASS

tcltest::test 15.17.3-double-2 { NaN % anything is NaN } {
    constant_expression T15173d2 \
        {Double.NaN % 1d != Double.NaN % 1d} \
        {1d % Double.NaN != 1d % Double.NaN}
} PASS

tcltest::test 15.17.3-double-3 { the result is positive in floating-point
        remainder if the dividend is positive } {
    constant_expression T15173d3 {3d % -2d > 0d} {3d % 2d > 0d}
} PASS

tcltest::test 15.17.3-double-4 { the result is negative in floating-point
        remainder if the dividend is negative } {
    constant_expression T15173d4 {-3d % 2d < 0d} {-3d % -2d < 0d}
} PASS

tcltest::test 15.17.3-double-5 { 0 % 0 is NaN } {
    constant_expression T15173d5 \
        {0d % 0d != 0d % 0d} \
        {-0d % 0d != -0d % 0d}
} PASS

tcltest::test 15.17.3-double-6 { Infinity % finite is NaN } {
    constant_expression T15173d6 \
        {Double.NEGATIVE_INFINITY % -1d != Double.NEGATIVE_INFINITY % -1d} \
        {Double.POSITIVE_INFINITY % -1d != Double.POSITIVE_INFINITY % -1d}
} PASS

tcltest::test 15.17.3-double-7 { Infinity % Infinity is NaN } {
    constant_expression T15173d7 \
        {Double.NEGATIVE_INFINITY % Double.NEGATIVE_INFINITY !=
            Double.NEGATIVE_INFINITY % Double.NEGATIVE_INFINITY} \
        {Double.POSITIVE_INFINITY % Double.NEGATIVE_INFINITY !=
            Double.POSITIVE_INFINITY % Double.NEGATIVE_INFINITY} \
        {Double.NEGATIVE_INFINITY % Double.POSITIVE_INFINITY !=
            Double.NEGATIVE_INFINITY % Double.POSITIVE_INFINITY} \
        {Double.POSITIVE_INFINITY % Double.POSITIVE_INFINITY !=
            Double.POSITIVE_INFINITY % Double.POSITIVE_INFINITY}
} PASS

tcltest::test 15.17.3-double-8 { Finite % Infinity == dividend } {
    constant_expression T15173d8 \
        {1d % Double.NEGATIVE_INFINITY == 1d} \
        {1d % Double.POSITIVE_INFINITY == 1d} \
        {-1d % Double.NEGATIVE_INFINITY == -1d} \
        {-1d % Double.POSITIVE_INFINITY == -1d}
} PASS

tcltest::test 15.17.3-double-9 { Finite % 0 is NaN } {
    constant_expression T15173d9 \
        {1d % 0d != 1d % 0d} \
        {-1d % 0d != -1d % 0d} \
        {1d % -0d != 1d % -0d} \
        {-1d % -0d != -1d % -0d}
} PASS

tcltest::test 15.17.3-double-10 { 0 % non-zero Finite == Dividend
        (check 1/0 to determine sign) } {
    constant_expression T15173d10 \
        {0d % 1d == 0} {1/(0d % 1d) == Double.POSITIVE_INFINITY} \
        {-0d % 1d == 0} {1/(-0d % 1d) == Double.NEGATIVE_INFINITY} \
        {0d % -1d == 0} {1/(0d % -1d) == Double.POSITIVE_INFINITY} \
        {-0d % -1d == 0} {1/(-0d % -1d) == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.3-double-11 { 0 % Infinity == Dividend
        (check 1/0 to determine sign) } {
    constant_expression T15173d11 \
        {0d % Double.POSITIVE_INFINITY == 0} \
        {1/(0d % Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY} \
        {-0d % Double.POSITIVE_INFINITY == 0} \
        {1/(-0d % Double.POSITIVE_INFINITY) == Double.NEGATIVE_INFINITY} \
        {0d % Double.NEGATIVE_INFINITY == 0} \
        {1/(0d % Double.NEGATIVE_INFINITY) == Double.POSITIVE_INFINITY} \
        {-0d % Double.NEGATIVE_INFINITY == 0} \
        {1/(-0d % Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY}
} PASS

tcltest::test 15.17.3-double-12 { Floating-point % cannot overflow } {
    constant_expression T15173d12 \
            {1e300 % 1e-300 == 4.891554850853602e-301} \
            {-1e300 % -1e-300 == -4.891554850853602e-301}
} PASS

tcltest::test 15.17.3-double-13 { Floating-point % works even with denorms } {
    constant_expression T15173d13 \
            {1e300 % 1e-320 == 3.16e-321} \
            {1e300 % -1e-320 == 3.16e-321} \
            {-1e-320 % 1e300 == -1e-320} \
            {-1e-320 % -1e300 == -1e-320}
} PASS

tcltest::test 15.17.3-double-14 { Example cases } {
    constant_expression T15173d14 \
            {5.0 % 3.0 == 2.0} \
            {5.0 % -3.0 == 2.0} \
            {-5.0 % 3.0 == -2.0} \
            {-5.0 % -3.0 == -2.0}
} PASS

# integer tests

tcltest::test 15.17.3-int-1 { Example cases } {
    constant_expression T15173i1 \
            {5 % 3 == 2} \
            {5 % -3 == 2} \
            {-5 % 3 == -2} \
            {-5 % -3 == -2}
} PASS

tcltest::test 15.17.3-int-2 { (a/b)*b+(a%b) == a } {
    constant_expression T15173i2 \
            {(0x80000000 / -1) * -1 + (0x80000000 % -1) == 0x80000000} \
            {(5 / 3) * 3 + (5 % 3) == 5} \
            {(5 / -3) * -3 + (5 % -3) == 5} \
            {(-5 / 3) * 3 + (-5 % 3) == -5} \
            {(-5 / -3) * -3 + (-5 % -3) == -5}
} PASS

tcltest::test 15.17.3-int-3 { Compile-time error to divide by constant 0 } {
    empty_class T15173i3 {int i = 1 / 0;}
} FAIL

tcltest::test 15.17.3-int-4 { MIN_INT % -1 == 0 } {
    constant_expression T15173i4 {0x80000000 % -1 == 0}
} PASS

tcltest::test 15.17.3-long-1 { Example cases } {
    constant_expression T15173l1 \
            {5L % 3L == 2L} \
            {5L % -3L == 2L} \
            {-5L % 3L == -2L} \
            {-5L % -3L == -2L}
} PASS

tcltest::test 15.17.3-long-2 { (a/b)*b+(a%b) == a } {
    constant_expression T15173l2 \
        {(0x8000000000000000L / -1L) * -1L + (0x8000000000000000L % -1L)
            == 0x8000000000000000L} \
        {(5L / 3L) * 3L + (5L % 3L) == 5L} \
        {(5L / -3L) * -3L + (5L % -3L) == 5L} \
        {(-5L / 3L) * 3L + (-5L % 3L) == -5L} \
        {(-5L / -3L) * -3L + (-5L % -3L) == -5L}
} PASS

tcltest::test 15.17.3-long-3 { Compile-time error to divide by constant 0 } {
    empty_class T15173l3 {long l = 1L % 0L;}
} FAIL

tcltest::test 15.17.3-long-4 { MIN_LONG % -1 == 0 } {
    constant_expression T15173l4 {0x8000000000000000L % -1L == 0L}
} PASS

