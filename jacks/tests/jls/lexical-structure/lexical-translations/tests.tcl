tcltest::test 3.2-invalid-1 { The parser must consume the largest token,
        even if another interpretation is valid } {
    empty_class T32i1 {
        int foo(int a, int b) {
            return a--b;
        }
    }
} FAIL

tcltest::test 3.2-invalid-2 { ensure that unicode escapes are processed
        before tokenization is done } {
    empty_class T32i2 {
        int foo(int a, int b) {
            return \u0061\u002d\u002d\u0062;
        }
    }
} FAIL

tcltest::test 3.2-invalid-3 { The parser must consume the largest token,
        even if another interpretation is valid } {
    compile [saveas T32i3.java {classT32i3 {}}]
} FAIL

tcltest::test 3.2-invalid-4 { The parser must consume the largest token,
        even if another interpretation is valid } {
    empty_main T32i4 {
        int i = 1/// comment starts at first /, not second
        1;
    }
} FAIL

tcltest::test 3.2-invalid-5 { Comments, as tokens, separate other tokens } {
    empty_main T32i5 {
        int i = 1/**/0;
    }
} FAIL

tcltest::test 3.2-invalid-6 { Comments, as tokens, separate other tokens } {
    empty_main T32i6 {
        i/**/nt i = 10;
    }
} FAIL

tcltest::test 3.2-invalid-7 { Comments, as tokens, separate other tokens } {
    empty_main T32i7 {
        int i = 0;
        i +/**/= 10;
    }
} FAIL

tcltest::test 3.2-valid-1 { Compiler should recognize difference in tokens } {
    empty_main T32v1 {
        boolean b1=""+1instanceof String;
        boolean b2=""+1Linstanceof String;
        boolean b3=""+1Dinstanceof String;
        boolean b4=""+1Finstanceof String;
        boolean b5=""+0x1instanceof String;
        boolean b6=""+0xAinstanceof String;
        boolean b7=""+'1'instanceof String;
        boolean b8=""+1.instanceof String;
        boolean b9=""+.1instanceof String;
    }
} PASS

tcltest::test 3.2-valid-2 { The parser must consume the largest token,
        even if another interpretation is valid } {
    empty_main T32v2 {
        int i = 1/* only one comment, last / performs division *//1;
    }
} PASS

