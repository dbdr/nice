tcltest::test 3.7-1 { /* anything */ is a comment } {
    compile [saveas T371.java "class T371 {/* / * // * / exit */}"]
} PASS

tcltest::test 3.7-2 { /* */ comment can span multiple lines } {
    compile [saveas T372.java "class T372 {/* hello\n bye \n*/}"]
} PASS

tcltest::test 3.7-3 { // comment inside /* */ comment is ignored } {
    compile [saveas T373.java "class T373 {/* hello\n // */}"]
} PASS

tcltest::test 3.7-4 { // comment inside // comment is ignored } {
    compile [saveas T374.java "class T374 {// //\n}"]
} PASS

tcltest::test 3.7-5 { /* */ comment inside // comment is ignored } {
    compile [saveas T375.java "class T375 {// /*\n//*/\n}"]
} PASS

tcltest::test 3.7-6 { /* or // inside /* */ comment is ignored } {
    compile [saveas T376.java "class T376 {/* comment /* // /* */}"]
} PASS

tcltest::test 3.7-7 { /* or // inside /** */ comment is ignored } {
    compile [saveas T377.java "class T377 {/** comment /* // /* */}"]
} PASS

tcltest::test 3.7-8 { /**/ is a documentaiton comment } {
    compile [saveas T378.java "class T378 {/**/}"]
} PASS

tcltest::test 3.7-9 { // comment separates tokens } {
    compile [saveas T379.java "class T379 {int//\ni;}"]
} PASS

tcltest::test 3.7-10 { /**/ comment separates tokens } {
    compile [saveas T3710.java "class T3710 {int/* */i;}"]
} PASS

tcltest::test 3.7-11 { // comment cannot appear in literal } {
    compile [saveas T3711.java "class T3711 {float f = 1.//\n0;}"]
} FAIL

tcltest::test 3.7-12 { /**/ comment cannot appear in literal } {
    compile [saveas T3712.java "class T3712 {float f = 1./* */0;}"]
} FAIL

tcltest::test 3.7-13 { // comment cannot appear in literal } {
    compile [saveas T3713.java "class T3713 {char c = 'a//\n';}"]
} FAIL

tcltest::test 3.7-14 { /**/ comment cannot appear in literal } {
    compile [saveas T3714.java "class T3714 {char c = 'a/* */';}"]
} FAIL

tcltest::test 3.7-15 { /* must have matching */ } {
    compile [saveas T3715.java "class T3715 {} /*"]
} FAIL

tcltest::test 3.7-16 { // may not end at EOF, only LineTerminator. Until Sun
        updates the JLS to permit it, this test must fail - see jikes bug
        2879 } {
    compile [saveas T3716.java "class T3716 {} //"]
} FAIL

tcltest::test 3.7-17 { /**/ comment can contain any characters, including \0,
        unterminated quotes } {
    empty_class T3717 "/*\0'\"*/"
} PASS

tcltest::test 3.7-18 { /**/ comment can contain any characters, including \0,
        unterminated quotes } {
    empty_class T3718 {/*\u0000\u0027\u0022*/}
} PASS

tcltest::test 3.7-19 { // comment can contain any characters, including \0,
        unterminated quotes } {
    empty_class T3719 "//\0'\""
} PASS

tcltest::test 3.7-20 { // comment can contain any characters, including \0,
        unterminated quotes } {
    empty_class T3720 {//\u0000\u0027\u0022}
} PASS

tcltest::test 3.7-21 { /**/ comment can contain any characters, including ASCII SUB } {
    empty_class T3721 "/*\x1a*/"
} PASS

tcltest::test 3.7-22 { /**/ comment can contain any characters, including ASCII SUB } {
    empty_class T3722 {/*\u001a*/}
} PASS

tcltest::test 3.7-23 { // comment can contain any characters, including ASCII SUB } {
    empty_class T3723 "//\x1a"
} PASS

tcltest::test 3.7-24 { // comment can contain any characters, including ASCII SUB } {
    empty_class T3724 {//\u001a}
} PASS

tcltest::test 3.7-25 { comments may be unicode } {
    empty_main T3725 {
        \u002f\u002f This is a comment\u000a int i;
        \u002f\u002a So's this \u002a\u002f
        i = 1;
    }
} PASS

tcltest::test 3.7-26 { /*/ is not a comment, but an opening for /* */ } {
    empty_class T3726 "/*/"
} FAIL

tcltest::test 3.7-27 { /*/ is not a comment, but an opening for /* */ } {
    empty_class T3727 {
        /*/
        */
    }
} PASS

tcltest::test 3.7-28 { /** */ doc comments are legal, in spite of a bug in the
        grammar which doesn't allow them } {
    empty_class T3728 "/** */"
} PASS

tcltest::test 3.7-29 { /**/ is a legal, degenerate doc comment, in spite of
        a bug in the grammar which doesn't allow it } {
    empty_class T3729 "/**/"
} PASS

tcltest::test 3.7-30 { /* */ comments don't nest } {
    empty_class T3730 "/* /* */ */"
} FAIL

tcltest::test 3.7-31 { /* */ comments don't nest in // } {
    empty_class T3731 "// /* \n */"
} FAIL

tcltest::test 3.7-32 { /** */ comment separates tokens } {
    compile [saveas T3732.java "class T3732 {int/** */i;}"]
} PASS

tcltest::test 3.7-33 { /** */ comment separates tokens } {
    compile [saveas T3733.java "class T3733 {float f = 1./** */0;}"]
} FAIL

tcltest::test 3.7-34 { /**/ comment cannot appear in literal } {
    compile [saveas T3734.java "class T3734 {char c = 'a/** */';}"]
} FAIL

tcltest::test 3.7-35 { /** must have matching */ } {
    compile [saveas T3735.java "class T3735 {} /**"]
} FAIL

tcltest::test 3.7-36 { /* must have matching */ } {
    compile [saveas T3736.java "class T3736 {} /* "]
} FAIL

tcltest::test 3.7-37 { /** must have matching */ } {
    compile [saveas T3737.java "class T3737 {} /** "]
} FAIL

tcltest::test 3.7-38 { /* must have matching */ } {
    compile [saveas T3738.java "class T3738 {} /* *"]
} FAIL

tcltest::test 3.7-39 { /** must have matching */ } {
    compile [saveas T3739.java "class T3739 {} /** *"]
} FAIL

# These next tests rely on the compiler to output the file name, followed
# by the line number of the error. Note that the bad token is conveniently
# placed so that any column numbers in the message avoid false positives.
tcltest::test 3.7-line-number-1 { test line counts in comments } {
    compile [saveas T37ln1.java "class T37ln1 { // \n }   oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-2 { test line counts in comments } {
    compile [saveas T37ln2.java "class T37ln2 { // \\u000a }   oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-3 { test line counts in comments } {
    compile [saveas T37ln3.java "class T37ln3 { /* \n */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-4 { test line counts in comments } {
    compile [saveas T37ln4.java "class T37ln4 { /* \\u000a */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-5 { test line counts in comments } {
    compile [saveas T37ln5.java "class T37ln5 { /*\n */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-6 { test line counts in comments } {
    compile [saveas T37ln6.java "class T37ln6 { /*\\u000a */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-7 { test line counts in comments } {
    compile [saveas T37ln7.java "class T37ln7 { /* *\n */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-8 { test line counts in comments } {
    compile [saveas T37ln8.java "class T37ln8 { /* *\\u000a */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-9 { test line counts in comments } {
    compile [saveas T37ln9.java "class T37ln9 { /** \n */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-10 { test line counts in comments } {
    compile [saveas T37ln10.java "class T37ln10 { /** \\u000a */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-11 { test line counts in comments } {
    compile [saveas T37ln11.java "class T37ln11 { /**\n */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-12 { test line counts in comments } {
    compile [saveas T37ln12.java "class T37ln12 { /**\\u000a */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-13 { test line counts in comments } {
    compile [saveas T37ln13.java "class T37ln13 { /** *\n */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

tcltest::test 3.7-line-number-14 { test line counts in comments } {
    compile [saveas T37ln14.java "class T37ln14 { /** *\\u000a */ }oops"]
    match_err_or_warn {*.java*2*}
} 1

