proc constant_encoded_expression { class encoding echar uchar } {
    saveas $class.java "
class $class \{
    void foo(int i) \{
        switch (i) \{
            case 0:
            case (('$echar' == '$uchar') ? 1 : 0):
        \}
    \}
\}
"
    compile -encoding $encoding $class.java
}

tcltest::test non-jls-encoding-1 { works with the
        cp437 encoding } {encoding} {
    constant_encoded_expression E1 cp437 \
        \x84 {\u00e4}
} PASS

tcltest::test non-jls-encoding-2 { same character in
        utf-8 } {encoding} {
    constant_encoded_expression E2 utf-8 \
        \xC3\xA4 {\u00e4}
} PASS

tcltest::test non-jls-encoding-3 { 257 is out of the
        ASCII range, but in 8859-1 it is defined as
        lower case a with macron from Latin
        Extended-A  } {encoding} {
    constant_encoded_expression E3 ISO-8859-4 \
        \xE0 {\u0101}
} PASS

tcltest::test non-jls-encoding-4 { same character in
        utf-8 } {encoding} {
    constant_encoded_expression E4 utf-8 \
        \xC4\x81 {\u0101}
} PASS

tcltest::test non-jls-encoding-5 { 65533 (FFFD) is the last
          symbol in "Specials" category } {encoding} {
    constant_encoded_expression E5 utf-8 \
        \xEF\xBF\xBD {\uFFFD}
} PASS

tcltest::test non-jls-encoding-6 { Latin small letter e with
        acute (233) is in the Latin-1 Suppliment } {encoding} {
    constant_encoded_expression E6 iso-8859-1 \
        \xE9 {\u00E9}
} PASS


tcltest::test non-jls-invalid-encoding-1 { Unknown encoding
        } {encoding} {
    saveas invalid-encoding.java {
class invalid-encoding {}
    }

    compile -encoding FooIsNotValidOnAnyCompilerIKnow \
        invalid-encoding.java
} FAIL

