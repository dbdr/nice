tcltest::test jikes-1 { jikes should fail but not core dump on
        this dependency } {jikes} {
    saveas BrokenDependency.java {
import a_package_that_can_not_be_found.Two;

public class BrokenDependency {
    void foo() {
        Object o = new Two();
    }
}
    }

    # Cleanup the .u file that is produced
    cleanup BrokenDependency.u

    compile -d . +M BrokenDependency.java
} FAIL

# test jikes-2 removed because of clarification to JLS

tcltest::test jikes-3 { jikes should fail but not core dump on illegal
        import from anonymous package } {jikes} {
    saveas Anon.java {public class Anon {}}
    saveas p/Import.java {
package p;
import Anon; // illegal
class Import {
    Object o = new Anon();
}
    }
    compile +P Anon.java p/Import.java
} FAIL
