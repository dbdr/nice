proc literal { name literal } {
    set data "class $name {String s = \"${literal}\";}\n"
    return [compile [saveas $name.java $data]]
}


tcltest::test 3.10.5-invalid-1 { A StringCharacter cannot be " } {
    literal T3105i1 {"}
} FAIL

tcltest::test 3.10.5-invalid-2 { A StringCharacter cannot be " } {
    literal T3105i2 {\u0022}
} FAIL

tcltest::test 3.10.5-invalid-3 { A StringCharacter cannot be \ } {
    literal T3105i3 \\
} FAIL

tcltest::test 3.10.5-invalid-4 { A StringCharacter cannot be \ } {
    literal T3105i4 {\u005c}
} FAIL

tcltest::test 3.10.5-invalid-5 { A LineTerminator is not an InputCharacter } {
    literal T3105i5 \r
} FAIL

tcltest::test 3.10.5-invalid-6 { A LineTerminator is not an InputCharacter } {
    literal T3105i6 \n
} FAIL

tcltest::test 3.10.5-invalid-7 { A LineTerminator is not an InputCharacter } {
    literal T3105i7 \r\n
} FAIL

tcltest::test 3.10.5-invalid-8 { A LineTerminator is not an InputCharacter } {
    literal T3105i8 {\u000d}
} FAIL

tcltest::test 3.10.5-invalid-9 { A LineTerminator is not an InputCharacter } {
    literal T3105i9 {\u000a}
} FAIL

tcltest::test 3.10.5-invalid-10 { A LineTerminator is not an InputCharacter } {
    literal T3105i10 {\u000d\u000a}
} FAIL

tcltest::test 3.10.5-invalid-11 { " must be terminated with " } {
    compile [saveas T3105i11.java {class T3105i11 {String s = ";}}]
} FAIL

# Valid string literals

tcltest::test 3.10.5-valid-1 { example string literal } {
    literal T3105v1 ""
} PASS

tcltest::test 3.10.5-valid-2 { example string literal } {
    literal T3105v2 {This is a string}
} PASS

tcltest::test 3.10.5-valid-3 { example string literal } {
    literal T3105v3 {This is a " +
                     "two-line string}
} PASS

tcltest::test 3.10.5-valid-4 { valid string literal } {
    literal T3105v4 {\\}
} PASS

tcltest::test 3.10.5-valid-5 { valid string literal } {
    literal T3105v5 {\'}
} PASS

tcltest::test 3.10.5-valid-6 { valid string literal } {
    literal T3105v6 {\u03a9}
} PASS

tcltest::test 3.10.5-valid-7 { valid string literal } {
    literal T3105v7 {\uFFFF}
} PASS

tcltest::test 3.10.5-valid-8 { valid string literal } {
    literal T3105v8 {\177}
} PASS

tcltest::test 3.10.5-valid-9 { valid string literal } {
    literal T3105v9 {\r}
} PASS

tcltest::test 3.10.5-valid-10 { valid string literal } {
    literal T3105v10 {\n}
} PASS

tcltest::test 3.10.5-valid-11 { valid string literal } {
    literal T3105v11 {\"}
} PASS

tcltest::test 3.10.5-valid-12 { valid string literal } {
    literal T3105v12 '
} PASS

tcltest::test 3.10.5-valid-13 { valid string literal } {
    literal T3105v13 {\u001a}
} PASS

tcltest::test 3.10.5-valid-14 { valid string literal } {
    literal T3105v14 \x1a
} PASS
