tcltest::test 14.7-jikes-message-1 { Some versions of jikes grabbed the wrong
        label in the error message } {jikes} {
    list [empty_main T147jm1 {
        int i = 1;
        a: labelone:
        a: labeltwo:
        while (i-- > 0) break a;
    }] [match_err_or_warn "*a: labeltwo*"]
} {FAIL 1}
