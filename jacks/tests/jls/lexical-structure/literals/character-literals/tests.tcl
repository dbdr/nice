proc literal { name literal } {
    set data "main(args) {char c = '${literal}';}\n"
    return [compile [saveas $name.java $data]]
}


tcltest::test 3.10.4-invalid-1 { A SingleCharacter cannot be ' } {
    literal T3104i1 '
} FAIL

tcltest::test 3.10.4-invalid-2 { A SingleCharacter cannot be ' } {
    literal T3104i2 {\u0027}
} FAIL

tcltest::test 3.10.4-invalid-3 { A SingleCharacter cannot be \ } {
    literal T3104i3 \\
} FAIL

tcltest::test 3.10.4-invalid-4 { A SingleCharacter cannot be \ } {
    literal T3104i4 {\u005c}
} FAIL

tcltest::test 3.10.4-invalid-5 { A LineTerminator is not an InputCharacter } {
    literal T3104i5 \r
} FAIL

tcltest::test 3.10.4-invalid-6 { A LineTerminator is not an InputCharacter } {
    literal T3104i6 \n
} FAIL

tcltest::test 3.10.4-invalid-7 { A LineTerminator is not an InputCharacter } {
    literal T3104i7 \r\n
} FAIL

tcltest::test 3.10.4-invalid-8 { A LineTerminator is not an InputCharacter } {
    literal T3104i8 {\u000d}
} FAIL

tcltest::test 3.10.4-invalid-9 { A LineTerminator is not an InputCharacter } {
    literal T3104i9 {\u000a}
} FAIL

tcltest::test 3.10.4-invalid-10 { A LineTerminator is not an InputCharacter } {
    literal T3104i10 {\u000d\u000a}
} FAIL

tcltest::test 3.10.4-invalid-11 { ' must be terminated with ' } {
    literal T3104i11 AB
} FAIL

tcltest::test 3.10.4-invalid-12 { empty character literal is not allowed } {
    literal T3104i12 ""
} FAIL

tcltest::test 3.10.4-invalid-13 { \r\n is two character literals } {
    literal T3104i13 {\r\n}
} FAIL

tcltest::test 3.10.4-invalid-14 { ' must be terminated with ' } {
    compile [saveas T3104i14.java "class T3104i14 {char c = 'a;}"]
} FAIL

tcltest::test 3.10.4-invalid-15 { EOF inside '' } {
    compile [saveas T3104i15.java "class T3104i15 \{char c = '"]
} FAIL

tcltest::test 3.10.4-invalid-16 { EOF inside '' } {
    compile [saveas T3104i16.java "class T3104i16 \{char c = 'a"]
} FAIL

tcltest::test 3.10.4-invalid-17 { EOF inside '' } {
    compile [saveas T3104i17.java "class T3104i17 \{char c = '\\"]
} FAIL

# Valid character literals

tcltest::test 3.10.4-valid-1 { example character literal } {
    literal T3104v1 a
} PASS

tcltest::test 3.10.4-valid-2 { example character literal } {
    literal T3104v2 %
} PASS

tcltest::test 3.10.4-valid-3 { example character literal } {
    literal T3104v3 {\t}
} PASS

tcltest::test 3.10.4-valid-4 { example character literal } {
    literal T3104v4 {\\}
} PASS

tcltest::test 3.10.4-valid-5 { example character literal } {
    literal T3104v5 {\'}
} PASS

tcltest::test 3.10.4-valid-6 { example character literal } {
    literal T3104v6 {\u03a9}
} PASS

tcltest::test 3.10.4-valid-7 { example character literal } {
    literal T3104v7 {\uFFFF}
} PASS

tcltest::test 3.10.4-valid-8 { example character literal } {
    literal T3104v8 {\177}
} PASS

tcltest::test 3.10.4-valid-9 { example character literal } {
    literal T3104v9 {\r}
} PASS

tcltest::test 3.10.4-valid-10 { example character literal } {
    literal T3104v10 {\n}
} PASS

tcltest::test 3.10.4-valid-11 { valid character literal } {
    literal T3104v11 {\"}
} PASS

tcltest::test 3.10.4-valid-12 { valid character literal } {
    literal T3104v12 {"}
} PASS

tcltest::test 3.10.4-valid-13 { valid character literal } {
    literal T3104v13 {\u001a}
} PASS

tcltest::test 3.10.4-valid-14 { valid character literal } {
    literal T3104v14 \x1a
} PASS
