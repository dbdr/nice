proc literal { name literal } {
    set data "class $name {long l = ${literal};}\n"
    return [compile [saveas $name.java $data] ]
}


tcltest::test 3.10.1-invalid-1 { 2147483648 may appear only as the operand of unary - } {
    literal T3101i1 2147483648
} FAIL

tcltest::test 3.10.1-invalid-2 { decimal larger than 2147483648 is not an int } {
    literal T3101i2 12345678901
} FAIL

tcltest::test 3.10.1-invalid-3 { octal larger than 037777777777 is not an int } {
    literal T3101i3 040000000000
} FAIL

tcltest::test 3.10.1-invalid-4 { hex larger than 0xffffffff is not an int } {
    literal T3101i4 0x123456789
} FAIL

tcltest::test 3.10.1-invalid-5 { 9223372036854775808L may appear only as the
        operand of unary - } {
    literal T3101i5 9223372036854775808L
} FAIL

tcltest::test 3.10.1-invalid-6 { decimal larger than 9223372036854775808L is not an int } {
    literal T3101i6 12345678901234567890L
} FAIL

tcltest::test 3.10.1-invalid-7 { octal larger than 01777777777777777777777L is not an int } {
    literal T3101i7 02000000000000000000000L
} FAIL

tcltest::test 3.10.1-invalid-8 { hex larger than 0xffffffffffffffffL is not an int } {
    literal T3101i8 0x123456789abcdef01L
} FAIL

tcltest::test 3.10.1-invalid-9 { octal cannot contain '9' } {
    literal T3101i9 09
} FAIL

tcltest::test 3.10.1-invalid-10 { 2147483648 may appear only as the operand of unary - } {
    literal T3101i10 0-2147483648
} FAIL

tcltest::test 3.10.1-invalid-11 { 9223372036854775808L may appear only as the
        operand of unary - } {
    literal T3101i11 0-9223372036854775808L
} FAIL

tcltest::test 3.10.1-invalid-12 { 0x must be followed by digits } {
    literal T3101i12 0x
} FAIL

tcltest::test 3.10.1-invalid-13 { 0x must be followed by digits } {
    literal T3101i13 0xL
} FAIL

# Valid integer literals

tcltest::test 3.10.1-valid-1 { example decimal int literal } {
    literal T3101v1 0
} PASS

tcltest::test 3.10.1-valid-2 { example octal int literal } {
    literal T3101v2 0372
} PASS

tcltest::test 3.10.1-valid-3 { example hex int literal } {
    literal T3101v3 0xDadaCafe
} PASS

tcltest::test 3.10.1-valid-4 { example hex int literal } {
    literal T3101v4 0X00FF00FF
} PASS

tcltest::test 3.10.1-valid-5 { decimal 0, octal 00, and hex 0x0 are equal } {
    compile [saveas T3101v5.java {
class T3101v5 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case ((0 == 00) ? 1 : 0):
	    case ((0 == 0x0) ? 2 : 0):
	    case ((00 == 0x0) ? 3 : 0):
	}
    }
}
    }]
} PASS

tcltest::test 3.10.1-valid-6 { 2147483648 is valid when negative } {
    literal T3101v6 -2147483648
} PASS

tcltest::test 3.10.1-valid-7 { example long literal, suffix l (ell) == L } {
    literal T3101v7 0l
} PASS

tcltest::test 3.10.1-valid-8 { example long literal } {
    literal T3101v8 0777L
} PASS

tcltest::test 3.10.1-valid-9 { example long literal } {
    literal T3101v9 0X100000000L
} PASS

tcltest::test 3.10.1-valid-10 { example long literal } {
    literal T3101v10 2147483648L
} PASS

tcltest::test 3.10.1-valid-11 { example long literal } {
    literal T3101v11 0xC0B0L
} PASS

tcltest::test 3.10.1-valid-12 { 9223372036854775807L is valid when negative } {
    literal T3101v12 -9223372036854775807L
} PASS

tcltest::test 3.10.1-valid-13 { An octal that fits in 32 bits is legal int } {
    literal T3101v13 0000000000000000000000000001
} PASS

tcltest::test 3.10.1-valid-14 { A hex literal that fits in 64 bits is a legal long } {
    literal T3101v14 0x000012345678CAFEBABEL
} PASS
