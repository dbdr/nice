tcltest::test 7.5.2-jikes-package-not-found-1 { Check the error
        message printed by Jikes when a package of a given
        given name can not be found } {jikes} {

    saveas T752pnf1.java {
import pkg.not.found.*;
class NotFound {}
    }

    list [compile -classpath /notadir T752pnf1.java] \
        [match_err_or_warn \
        "*Error:*Package \"pkg/not/found\" could not be found in:*"]
} {FAIL 1}
