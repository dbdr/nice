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
    compile [saveas T3710.java "class T3710 {int/**/i;}"]
} PASS

tcltest::test 3.7-11 { // comment cannot appear in literal } {
    compile [saveas T3711.java "class T3711 {float f = 1.//\n0;}"]
} FAIL

tcltest::test 3.7-12 { /**/ comment cannot appear in literal } {
    compile [saveas T3712.java "class T3712 {float f = 1./**/0;}"]
} FAIL

tcltest::test 3.7-13 { // comment cannot appear in literal } {
    compile [saveas T3713.java "class T3713 {char c = 'a//\n';}"]
} FAIL

tcltest::test 3.7-14 { /**/ comment cannot appear in literal } {
    compile [saveas T3714.java "class T3714 {char c = 'a/**/';}"]
} FAIL

tcltest::test 3.7-15 { /* must have matching */ } {
    compile [saveas T3715.java "class T3715 {} /*"]
} FAIL

tcltest::test 3.7-16 { // may end at EOF instad of LineTerminator. This goes
        against JLS 3, but has always been Sun's behavior. Therefore, the
        Java Spec Report argues that this should be legal } {
    compile [saveas T3716.java "class T3716 {} //"]
} PASS

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
