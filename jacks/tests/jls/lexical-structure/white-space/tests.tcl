proc whitespace { name space } {
    set data "class $name {int${space}i;}\n"
    return [compile [saveas $name.java $data] ]
}


tcltest::test 3.6-invalid-1 { Vertical tab (Ctrl-K) '\v' is not whitespace } {
    whitespace T36i1 \v
} FAIL

tcltest::test 3.6-invalid-2 { Vertical tab (Ctrl-K) '\u000b' is not whitespace } {
    whitespace T36i2 {\u000b}
} FAIL

tcltest::test 3.6-invalid-3 { Backspace (Ctrl-H) '\b' is not whitespace } {
    whitespace T36i3 \b
} FAIL

tcltest::test 3.6-invalid-4 { Backspace (Ctrl-H) '\u0008' is not whitespace } {
    whitespace T36i4 {\u0008}
} FAIL

tcltest::test 3.6-invalid-5 { Alert (Ctrl-G) '\a' is not whitespace } {
    whitespace T36i5 \a
} FAIL

tcltest::test 3.6-invalid-6 { Alert (Ctrl-G) '\u0007' is not whitespace } {
    whitespace T36i6 {\u0007}
} FAIL

tcltest::test 3.6-invalid-7 { Null '\0' is not whitespace } {
    whitespace T36i7 \0
} FAIL

tcltest::test 3.6-invalid-8 { Null '\u0000' is not whitespace } {
    whitespace T36i8 {\u0000}
} FAIL

tcltest::test 3.6-invalid-9 { Sub (Ctrl-Z) '\u001a' is not whitespace } {
    whitespace T36i9 {\u001a}
} FAIL

# valid whitespace

tcltest::test 3.6-valid-1 { Linefeed (Ctrl-J) '\n' is whitespace } {
    whitespace T36v1 \n
} PASS

tcltest::test 3.6-valid-2 { Linefeed (Ctrl-J) '\u000a' is whitespace } {
    whitespace T36v2 {\u000a}
} PASS

tcltest::test 3.6-valid-3 { Carriage Return (Ctrl-M) '\r' is whitespace } {
    whitespace T36v3 \r
} PASS

tcltest::test 3.6-valid-4 { Carriage Return (Ctrl-M) '\u000d' is whitespace } {
    whitespace T36v4 {\u000d}
} PASS

tcltest::test 3.6-valid-5 { CR/LF '\r\n' is whitespace } {
    whitespace T36v5 \r\n
} PASS

tcltest::test 3.6-valid-6 { CR/LF '\u000d\u000a' is whitespace } {
    whitespace T36v6 {\u000d\u000a}
} PASS

tcltest::test 3.6-valid-7 { Space ' ' is whitespace } {
    whitespace T36v7 " "
} PASS

tcltest::test 3.6-valid-8 { Space '\u0020' is whitespace } {
    whitespace T36v8 {\u0020}
} PASS

tcltest::test 3.6-valid-9 { Tab (Ctrl-I) '\t' is whitespace } {
    whitespace T36v9 \t
} PASS

tcltest::test 3.6-valid-10 { Tab (Ctrl-I) '\u0009' is whitespace } {
    whitespace T36v10 {\u0009}
} PASS

tcltest::test 3.6-valid-11 { Form Feed (Ctrl-L) '\f' is whitespace } {
    whitespace T36v11 \f
} PASS

tcltest::test 3.6-valid-12 { Form Feed (Ctrl-L) '\u000c' is whitespace } {
    whitespace T36v12 {\u000c}
} PASS
