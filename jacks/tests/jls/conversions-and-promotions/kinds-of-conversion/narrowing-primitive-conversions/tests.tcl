tcltest::test 5.1.3-nan-1 { narrowing from float NaN to int produces 0 } {
    constant_expression T513nan1 {(int) Float.NaN == 0} {(int) -Float.NaN == 0}
} PASS

tcltest::test 5.1.3-nan-2 { narrowing from double NaN to int produces 0 } {
    constant_expression T513nan2 {(int) Double.NaN == 0} {(int) -Double.NaN == 0}
} PASS

tcltest::test 5.1.3-nan-3 { narrowing from float NaN to long produces 0 } {
    constant_expression T513nan3 {(long) Float.NaN == 0L} {(long) -Float.NaN == 0L}
} PASS

tcltest::test 5.1.3-nan-4 { narrowing from double NaN to long produces 0 } {
    constant_expression T513nan4 {(long) Double.NaN == 0L} {(long) -Double.NaN == 0L}
} PASS

tcltest::test 5.1.3-nan-5 { narrowing from double NaN to float produces NaN } {
    constant_expression T513nan5 {(float) Double.NaN != (float) Double.NaN}
} PASS

tcltest::test 5.1.3-dtf-1 { narrowing from double to float can overflow } {
    constant_expression T513dtf1 {(float) 1e100 == Float.POSITIVE_INFINITY}
} PASS

tcltest::test 5.1.3-dtf-2 { narrowing from double to float can underflow } {
    constant_expression T513dtf2 {(float) 1e-100 == 0.0f}
} PASS

tcltest::test 5.1.3-dtf-3 { narrowing from double to float rounds down if
        remainder < .5 } {
    constant_expression T513dtf3 {(float)(double)0x8000ff7ffffL == (float)0x8000ff00000L}
} PASS

tcltest::test 5.1.3-dtf-4 { narrowing from double to float rounds up if
        remainder > .5 } {
    constant_expression T513dtf4 {(float)(double)0x8000ff80001L == (float)0x80010000000L}
} PASS

tcltest::test 5.1.3-dtf-5 { narrowing from double to float rounds to lsb 0 if
        remainder == .5 } {
    constant_expression T513dtf5 {(float)(double)0x8000ff80000L == (float)0x80010000000L}
} PASS

tcltest::test 5.1.3-dtf-6 { narrowing from double to float rounds to lsb 0 if
        remainder == .5 } {
    constant_expression T513dtf6 {(float)(double)0x8000f080000L == (float)0x8000f000000L}
} PASS

tcltest::test 5.1.3-dtf-7 { narrowing from double to denorm float rounds to nearest } {
    constant_expression T513dtf7 \
            {(float)5.877475257357598E-39 == 5.877475e-39f} \
            {(float)5.8774752573576E-39 == 5.877476e-39f} \
            {(float)5.87747595800683E-39 == 5.877476e-39f} \
            {(float)5.877476658656061E-39 == 5.877476e-39f} \
            {(float)5.877476658656063E-39 == 5.877477e-39f}
} PASS

tcltest::test 5.1.3-dtl-1 { narrowing from double to long truncates } {
    constant_expression T513dtl1 \
            {(long)Double.POSITIVE_INFINITY == 0x7fffffffffffffffL} \
            {(long)Double.NEGATIVE_INFINITY == 0x8000000000000000L}
} PASS

tcltest::test 5.1.3-dtl-2 { narrowing from double to long truncates } {
    constant_expression T513dtl2 {(long)1e40 == 0x7fffffffffffffffL} \
            {(long)-1e40 == 0x8000000000000000L}
} PASS

tcltest::test 5.1.3-dtl-3 { narrowing from double to long truncates } {
    constant_expression T513dtl3 {(long)0.0 == 0L} \
            {(long)-0.0 == 0L}
} PASS

tcltest::test 5.1.3-dtl-4 { narrowing from double to long truncates } {
    constant_expression T513dtl4 {(long)1.999 == 1L} \
            {(long)-1.999 == -1L}
} PASS

tcltest::test 5.1.3-dti-1 { narrowing from double to int truncates } {
    constant_expression T513dti1 \
            {(int)Double.POSITIVE_INFINITY == 0x7fffffff} \
            {(int)Double.NEGATIVE_INFINITY == 0x80000000}
} PASS

tcltest::test 5.1.3-dti-2 { narrowing from double to int truncates } {
    constant_expression T513dti2 {(int)1e40 == 0x7fffffff} \
            {(int)-1e40 == 0x80000000}
} PASS

tcltest::test 5.1.3-dti-3 { narrowing from double to int truncates } {
    constant_expression T513dti3 {(int)0.0 == 0L} \
            {(int)-0.0 == 0L}
} PASS

tcltest::test 5.1.3-dti-4 { narrowing from double to int truncates } {
    constant_expression T513dti4 {(int)1.999 == 1L} \
            {(int)-1.999 == -1L}
} PASS

tcltest::test 5.1.3-ftl-1 { narrowing from float to long truncates } {
    constant_expression T513ftl1 \
            {(long)Float.POSITIVE_INFINITY == 0x7fffffffffffffffL} \
            {(long)Float.NEGATIVE_INFINITY == 0x8000000000000000L}
} PASS

tcltest::test 5.1.3-ftl-2 { narrowing from float to long truncates } {
    constant_expression T513ftl2 {(long)1e30f == 0x7fffffffffffffffL} \
            {(long)-1e30f == 0x8000000000000000L}
} PASS

tcltest::test 5.1.3-ftl-3 { narrowing from float to long truncates } {
    constant_expression T513ftl3 {(long)0.0f == 0L} \
            {(long)-0.0f == 0L}
} PASS

tcltest::test 5.1.3-ftl-4 { narrowing from float to long truncates } {
    constant_expression T513ftl4 {(long)1.999f == 1L} \
            {(long)-1.999f == -1L}
} PASS

tcltest::test 5.1.3-fti-1 { narrowing from float to int truncates } {
    constant_expression T513fti1 \
            {(int)Float.POSITIVE_INFINITY == 0x7fffffff} \
            {(int)Float.NEGATIVE_INFINITY == 0x80000000}
} PASS

tcltest::test 5.1.3-fti-2 { narrowing from float to int truncates } {
    constant_expression T513fti2 {(int)1e30f == 0x7fffffff} \
            {(int)-1e30f == 0x80000000}
} PASS

tcltest::test 5.1.3-fti-3 { narrowing from float to int truncates } {
    constant_expression T513fti3 {(int)0.0f == 0} \
            {(int)-0.0f == 0}
} PASS

tcltest::test 5.1.3-fti-4 { narrowing from float to int truncates } {
    constant_expression T513fti4 {(int)1.999f == 1} \
            {(int)-1.999f == -1}
} PASS

