tcltest::test 8.8.5.1-jikes-before-block-statements-1 {
        An ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {jikes} {
    list [empty_class T8851jbb1 {
    T8851jbb1() {}
    T8851jbb1(int i) {
        i = 0;
        this();
    }
    }] [match_err_or_warn  "*Error:* first statement in constructor*"]
} {FAIL 1}

tcltest::test 8.8.5.1-jikes-before-block-statements-2 {
        An ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {jikes} {
    list [empty_class T8851jbb2 {
    T8851jbb2() {}
    T8851jbb2(int i) {
        i = 0;
        super();
    }
    }] [match_err_or_warn  "*Error:* first statement in constructor*"]
} {FAIL 1}
