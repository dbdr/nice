tcltest::test 3.1-jikes-comments-non-ascii-1 { a comment can be
        formed from non-ASCII characters, Jikes should convert
        an 8 bit character to unicode without an encoding } {jikes} {
    empty_class T31jcna1 "\n // A comment \xa3 \n"
} PASS

tcltest::test 3.1-jikes-identifier-non-ascii-1 { an identifier can
        be formed from non-ASCII characters, Jikes should convert
        an 8 bit character to unicode without an encoding } {jikes} {
    empty_main T31jina1 "int \xa3 = 0; \n \xa3 = 1;"
} PASS

tcltest::test 3.1-jikes-character-literal-non-ascii-1 { a character
        literal can be formed from non-ASCII characters, Jikes should
        convert an 8 bit character to unicode without an encoding } {jikes} {
    empty_class T31jclna1 "char c = '\xa3';"
} PASS

tcltest::test 3.1-jikes-string-literal-non-ascii-1 { a string
        literal can be formed from non-ASCII characters, Jikes should
        convert an 8 bit character to unicode without an encoding } {jikes} {
    empty_class T31jslna1 "String s = \"\xa3\";"
} PASS

tcltest::test 3.1-jikes-non-ascii-1 { 0xF7 returns false for
        Character.isJavaIdentifierStart() so it is not a
        valid identifier } {jikes} {
    compile [saveas T31jna1.java "class T31na2 { \xF7 v; }\n"]
} FAIL

