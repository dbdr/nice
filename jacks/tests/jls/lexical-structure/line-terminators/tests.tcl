# The following are the single line terminators:
# \r \n \u000d \u000a \u000D \u000A
# \r\n \r\u000a \u000d\n \r\u000A \u000D\n
# \u000d\u000a \u000d\u000A \u000D\u000a \u000D\u000A

# In addition, we test duplicating the u in escapes.

# This group of tests checks that the compiler is recognizing a line
# terminator, necessary to end the // comment so the class will end
tcltest::test 3.4-sanity-1 { If this passes, the remaining tests are shot } {
    compile [saveas T34s1.java "class T34s1 { // }"]
} FAIL

tcltest::test 3.4-valid-1 { CR (\r) is a LineTerminator } {
    set S \r
    compile [saveas T34v1.java "class T34v1 { // $S}"]
} PASS

tcltest::test 3.4-valid-2 { LF (\n) is a LineTerminator } {
    set S \n
    compile [saveas T34v2.java "class T34v2 { // $S}"]
} PASS

tcltest::test 3.4-valid-3 { CR (\u000d) is a LineTerminator } {
    set S \\u000d
    compile [saveas T34v3.java "class T34v3 { // $S}"]
} PASS

tcltest::test 3.4-valid-4 { LF (\u000a) is a LineTerminator } {
    set S \\u000a
    compile [saveas T34v4.java "class T34v4 { // $S}"]
} PASS

tcltest::test 3.4-valid-5 { CR (\u000D) is a LineTerminator } {
    set S \\u000D
    compile [saveas T34v5.java "class T34v5 { // $S}"]
} PASS

tcltest::test 3.4-valid-6 { LF (\u000A) is a LineTerminator } {
    set S \\u000A
    compile [saveas T34v6.java "class T34v6 { // $S}"]
} PASS

tcltest::test 3.4-valid-7 { CRLF (\r\n) is a LineTerminator } {
    set S \r\n
    compile [saveas T34v7.java "class T34v7 { // $S}"]
} PASS

tcltest::test 3.4-valid-8 { CRLF (\r\u000a) is a LineTerminator } {
    set S \r\\u000a
    compile [saveas T34v8.java "class T34v8 { // $S}"]
} PASS

tcltest::test 3.4-valid-9 { CRLF (\u000d\n) is a LineTerminator } {
    set S \\u000d\n
    compile [saveas T34v9.java "class T34v9 { // $S}"]
} PASS

tcltest::test 3.4-valid-10 { CRLF (\r\u000A) is a LineTerminator } {
    set S \r\\u000A
    compile [saveas T34v10.java "class T34v10 { // $S}"]
} PASS

tcltest::test 3.4-valid-11 { CRLF (\u000D\n) is a LineTerminator } {
    set S \\u000D\n
    compile [saveas T34v11.java "class T34v11 { // $S}"]
} PASS

tcltest::test 3.4-valid-12 { CRLF (\u000d\u000a) is a LineTerminator } {
    set S \\u000d\\u000a
    compile [saveas T34v12.java "class T34v12 { // $S}"]
} PASS

tcltest::test 3.4-valid-13 { CRLF (\u000d\u000A) is a LineTerminator } {
    set S \\u000d\\u000A
    compile [saveas T34v13.java "class T34v13 { // $S}"]
} PASS

tcltest::test 3.4-valid-14 { CRLF (\u000D\u000a) is a LineTerminator } {
    set S \\u000D\\u000a
    compile [saveas T34v14.java "class T34v14 { // $S}"]
} PASS

tcltest::test 3.4-valid-15 { CRLF (\u000D\u000A) is a LineTerminator } {
    set S \\u000D\\u000A
    compile [saveas T34v15.java "class T34v15 { // $S}"]
} PASS

tcltest::test 3.4-valid-16 { CR (\uuuuu000d) is a LineTerminator } {
    set S \\uuuuu000d
    compile [saveas T34v16.java "class T34v16 { // $S}"]
} PASS

tcltest::test 3.4-valid-17 { LF (\uuuuu000a) is a LineTerminator } {
    set S \\uuuuu000a
    compile [saveas T34v17.java "class T34v17 { // $S}"]
} PASS

tcltest::test 3.4-valid-18 { CR (\uuuuu000D) is a LineTerminator } {
    set S \\uuuuu000D
    compile [saveas T34v18.java "class T34v18 { // $S}"]
} PASS

tcltest::test 3.4-valid-19 { LF (\uuuuu000A) is a LineTerminator } {
    set S \\uuuuu000A
    compile [saveas T34v19.java "class T34v19 { // $S}"]
} PASS

tcltest::test 3.4-valid-20 { CRLF (\r\uuuuu000a) is a LineTerminator } {
    set S \r\\uuuuu000a
    compile [saveas T34v20.java "class T34v20 { // $S}"]
} PASS

tcltest::test 3.4-valid-21 { CRLF (\uuuuu000d\n) is a LineTerminator } {
    set S \\uuuuu000d\n
    compile [saveas T34v21.java "class T34v21 { // $S}"]
} PASS

tcltest::test 3.4-valid-22 { CRLF (\r\uuuuu000A) is a LineTerminator } {
    set S \r\\uuuuu000A
    compile [saveas T34v22.java "class T34v22 { // $S}"]
} PASS

tcltest::test 3.4-valid-23 { CRLF (\uuuuu000D\n) is a LineTerminator } {
    set S \\uuuuu000D\n
    compile [saveas T34v23.java "class T34v23 { // $S}"]
} PASS

tcltest::test 3.4-valid-24 { CRLF (\uuuuu000d\u000a) is a LineTerminator } {
    set S \\uuuuu000d\\u000a
    compile [saveas T34v24.java "class T34v24 { // $S}"]
} PASS

tcltest::test 3.4-valid-25 { CRLF (\uuuuu000d\u000A) is a LineTerminator } {
    set S \\uuuuu000d\\u000A
    compile [saveas T34v25.java "class T34v25 { // $S}"]
} PASS

tcltest::test 3.4-valid-26 { CRLF (\uuuuu000D\u000a) is a LineTerminator } {
    set S \\uuuuu000D\\u000a
    compile [saveas T34v26.java "class T34v26 { // $S}"]
} PASS

tcltest::test 3.4-valid-27 { CRLF (\uuuuu000D\u000A) is a LineTerminator } {
    set S \\uuuuu000D\\u000A
    compile [saveas T34v27.java "class T34v27 { // $S}"]
} PASS

tcltest::test 3.4-valid-28 { CRLF (\u000d\uuuuu000a) is a LineTerminator } {
    set S \\u000d\\uuuuu000a
    compile [saveas T34v28.java "class T34v28 { // $S}"]
} PASS

tcltest::test 3.4-valid-29 { CRLF (\u000d\uuuuu000A) is a LineTerminator } {
    set S \\u000d\\uuuuu000A
    compile [saveas T34v29.java "class T34v29 { // $S}"]
} PASS

tcltest::test 3.4-valid-30 { CRLF (\u000D\uuuuu000a) is a LineTerminator } {
    set S \\u000D\\uuuuu000a
    compile [saveas T34v30.java "class T34v30 { // $S}"]
} PASS

tcltest::test 3.4-valid-31 { CRLF (\u000D\uuuuu000A) is a LineTerminator } {
    set S \\u000D\\uuuuu000A
    compile [saveas T34v31.java "class T34v31 { // $S}"]
} PASS

tcltest::test 3.4-valid-32 { CRLF (\uuuuu000d\uuuuu000a) is a LineTerminator } {
    set S \\uuuuu000d\\uuuuu000a
    compile [saveas T34v32.java "class T34v32 { // $S}"]
} PASS

tcltest::test 3.4-valid-33 { CRLF (\uuuuu000d\uuuuu000A) is a LineTerminator } {
    set S \\uuuuu000d\\uuuuu000A
    compile [saveas T34v33.java "class T34v33 { // $S}"]
} PASS

tcltest::test 3.4-valid-34 { CRLF (\uuuuu000D\uuuuu000a) is a LineTerminator } {
    set S \\uuuuu000D\\uuuuu000a
    compile [saveas T34v34.java "class T34v34 { // $S}"]
} PASS

tcltest::test 3.4-valid-35 { CRLF (\uuuuu000D\uuuuu000A) is a LineTerminator } {
    set S \\uuuuu000D\\uuuuu000A
    compile [saveas T34v35.java "class T34v35 { // $S}"]
} PASS

# This group of tests checks that the compiler is correctly counting line
# numbers. Java files begin on line one, all forms of \r\n must be collapsed
# into a single line, and unicode escapes contribute to the line count (even
# though this practice makes editing source files based on line numbers a pain).
# The compiler must not over-collapse sequences like \r\r\n\n. These tests rely
# on the compiler to output the file name, followed by the line number of the
# error. Note that the bad token is conveniently placed so that any column
# numbers in the message avoid false positives.
tcltest::test 3.4-line-number-1 { CR (\r) is a LineTerminator } {
    set S \r
    compile [saveas T34ln1.java "${S}class T34ln1 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-2 { LF (\n) is a LineTerminator } {
    set S \n
    compile [saveas T34ln2.java "${S}class T34ln2 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-3 { CR (\u000d) is a LineTerminator } {
    set S \\u000d
    compile [saveas T34ln3.java "${S}class T34ln3 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-4 { LF (\u000a) is a LineTerminator } {
    set S \\u000a
    compile [saveas T34ln4.java "${S}class T34ln4 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-5 { CR (\u000D) is a LineTerminator } {
    set S \\u000D
    compile [saveas T34ln5.java "${S}class T34ln5 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-6 { LF (\u000A) is a LineTerminator } {
    set S \\u000A
    compile [saveas T34ln6.java "${S}class T34ln6 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-7 { CRLF (\r\n) is a LineTerminator } {
    set S \r\n
    compile [saveas T34ln7.java "${S}class T34ln7 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-8 { CRLF (\r\u000a) is a LineTerminator } {
    set S \r\\u000a
    compile [saveas T34ln8.java "${S}class T34ln8 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-9 { CRLF (\u000d\n) is a LineTerminator } {
    set S \\u000d\n
    compile [saveas T34ln9.java "${S}class T34ln9 {}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-10 { CRLF (\r\u000A) is a LineTerminator } {
    set S \r\\u000A
    compile [saveas T34ln10.java "${S}class T34ln10{}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-11 { CRLF (\u000D\n) is a LineTerminator } {
    set S \\u000D\n
    compile [saveas T34ln11.java "${S}class T34ln11{}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-12 { CRLF (\u000d\u000a) is a LineTerminator } {
    set S \\u000d\\u000a
    compile [saveas T34ln12.java "${S}class T34ln12{}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-13 { CRLF (\u000d\u000A) is a LineTerminator } {
    set S \\u000d\\u000A
    compile [saveas T34ln13.java "${S}class T34ln13{}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-14 { CRLF (\u000D\u000a) is a LineTerminator } {
    set S \\u000D\\u000a
    compile [saveas T34ln14.java "${S}class T34ln14{}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-15 { CRLF (\u000D\u000A) is a LineTerminator } {
    set S \\u000D\\u000A
    compile [saveas T34ln15.java "${S}class T34ln15{}oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.4-line-number-16 { CRCR (\r\r) is two LineTerminators } {
    set S \r\r
    compile [saveas T34ln16.java "${S}class T34ln16{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-17 { CRCR (\u000d\u000d) is two LineTerminators } {
    set S \\u000d\\u000d
    compile [saveas T34ln17.java "${S}class T34ln17{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-18 { LFLF (\n\n) is two LineTerminators } {
    set S \n\n
    compile [saveas T34ln18.java "${S}class T34ln18{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-19 { LFLF (\u000a\u000a) is two LineTerminators } {
    set S \\u000a\\u000a
    compile [saveas T34ln19.java "${S}class T34ln19{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-20 { LFCR (\n\r) is two LineTerminators } {
    set S \n\r
    compile [saveas T34ln20.java "${S}class T34ln20{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-21 { LFCR (\u000a\u000d) is two LineTerminators } {
    set S \\u000a\\u000d
    compile [saveas T34ln21.java "${S}class T34ln21{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-22 { CRLFCRLF (\r\n\r\n) is two LineTerminators } {
    set S \r\n\r\n
    compile [saveas T34ln22.java "${S}class T34ln22{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-23 { CRLFCRLF (\u000d\u000a\u000d\u000a) is two LineTerminators } {
    set S \\u000d\\u000a\\u000d\\u000a
    compile [saveas T34ln23.java "${S}class T34ln23{}oops"]
    match_err_or_warn {*.java*3*}
} 1

tcltest::test 3.4-line-number-24 { CRCRLFLF (\r\r\n\n) is three LineTerminators } {
    set S \r\r\n\n
    compile [saveas T34ln24.java "${S}class T34ln24{}oops"]
    match_err_or_warn {*.java*4*}
} 1

tcltest::test 3.4-line-number-25 { CRCRLFLF (\u000d\u000d\u000a\u000a) is three LineTerminators } {
    set S \\u000d\\u000d\\u000a\\u000a
    compile [saveas T34ln25.java "${S}class T34ln25{}oops"]
    match_err_or_warn {*.java*4*}
} 1

