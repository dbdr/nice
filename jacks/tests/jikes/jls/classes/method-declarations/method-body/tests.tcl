tcltest::test 8.4.5-jikes-1 { see if jikes output is mangled
        by a typo in src/error.cpp } {jikes} {
    saveas MangleJikesOutputNoReturn.java {
class MangleJikesOutputNoReturn {
    boolean processCommand(String s) {}
}
    }

    list [compile MangleJikesOutputNoReturn.java] \
         [match_err_or_warn \
    "*Error:*contain a return statement with an expression compatible*"]
} {FAIL 1}
