# Note that reachability of statements in if (false)
# blocks is detailed in 14.20.


tcltest::test 16.2.7-final-1 { A blank final
        initialized inside an if (true) block is
        definitely assigned. } {

    compile [saveas T1627f1.java {
public class T1627f1 {
    final int val;

    T1627f1() {
        if (true) {
            val = 1;
        }
    }
}
}]
} PASS

tcltest::test 16.2.7-final-2 { A blank final
        initialized inside an if (false) block is
        not definitely assigned. } {

    compile [saveas T1627f2.java {
public class T1627f2 {
    final int val;

    T1627f2() {
        if (false) {
            val = 0;
        }
    }
}
}]
} FAIL


tcltest::test 16.2.7-final-3 { A final variable
        must be definitely unassigned if it
        is to be assigned inside an if (true) block. } {

    compile [saveas T1627f3.java {

public class T1627f3 {
    final int val = 0;

    T1627f3() {
        if (true) {
            val = 1;
        }
    }
}
}]
} FAIL

tcltest::test 16.2.7-final-4 { A final variable
        must be definitely unassigned if it
        is to be assigned inside an if (false) block. } {

    compile [saveas T1627f4.java {
public class T1627f4 {
    final int val = 0;

    T1627f4() {
        if (false) {
            val = 1;
        }
    }
}
}]
} FAIL


tcltest::test 16.2.7-final-5 { A final variable
        must be definitely unassigned when an
        assignment to it occurs. A blank
        final assigned inside an if (true)
        block is definitely assigned, so
        a second assignment fails. } {

    compile [saveas T1627f5.java {
public class T1627f5 {
    final int val;

    T1627f5() {
        if (true) {
            val = 1;
        }

        val = 0;
    }
}
}]
} FAIL

tcltest::test 16.2.7-final-6 { A final variable
        must be definitely unassigned when an
        assignment to it occurs. A blank
        final assigned inside an if (false)
        block is not definitely assigned but
        it is also not definitely unassigned. } {

    compile [saveas T1627f6.java {
public class T1627f6 {
    final int val;

    T1627f6() {
        if (false) {
            val = 1;
        }

        val = 0;
    }
}
}]
} FAIL


# FIXME : add tests for if (e) S else T blocks.
# FIXME : add tests for definite assignment before access.



tcltest::test 16.2.7-scope-1 { A final variable in
        one scope should not effect a final variable
        in the enclosing scope } {

    empty_main T1627s1 {
        if (true) {
          final int i = 1;
        }
        final int j;
        j = 2;
    }
} PASS

tcltest::test 16.2.7-scope-2 { A final variable in
        one scope should not effect a final variable
        in the enclosing scope } {

    empty_main T1627s2 {
        if (true) {
          final int i = 1;
        }
        final int i;
        i = 2;
    }
} PASS
