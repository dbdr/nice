tcltest::test 3.10.4-jikes-non-ascii-1 { It is a compile-time
        error for the character following the SingleCharacter
        be other than a '. We check the format of the printed
        error message for this case to make sure a valid
        \uXXXX escape sequence appears in the error message. } {jikes} {

    saveas T3104jna1.java "
        class T3104jna1 {
            char c = 'a\xa3';
        }
    "
    list [compile T3104jna1.java] [expr {
	[match_err_or_warn {*\\u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]*}]
	+ [match_err_or_warn {*£*}]}]
} {FAIL 1}

tcltest::test 3.10.4-jikes-non-ascii-2 { It is a compile-time
        error for the character following the EscapeSequence
        be other than a '. We check the format of the printed
        error message for this case to make sure a valid
        \uXXXX escape sequence appears in the error message. } {jikes} {

    saveas T3104jna2.java {
        class T3104jna2 {
            char c = '\u0e0ea';
        }
    }
    list [compile T3104jna2.java]  [match_err_or_warn \
        {*\\u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]*}]
} {FAIL 1}
