# See also: 7.6, 8.5.1

tcltest::test 8.1.1-valid-modifier-1 { public is valid ClassModifier } {
    compile [saveas T811vm1.java {
public class T811vm1 {}
    }]
} PASS

tcltest::test 8.1.1-valid-modifier-2 { abstract is valid ClassModifier } {
    compile [saveas T811vm2.java {
abstract class T811vm2 {}
    }]
} PASS

tcltest::test 8.1.1-valid-modifier-3 { final is valid ClassModifier } {
    compile [saveas T811vm3.java {
final class T811vm3 {}
    }]
} PASS

tcltest::test 8.1.1-valid-modifier-4 { strictfp is valid ClassModifier } {
    compile [saveas T811vm4.java {
strictfp class T811vm4 {}
    }]
} PASS

# Duplicate modifiers

tcltest::test 8.1.1-duplicate-modifier-1 { compile time
        error to specify a class modifier twice } {
    compile [saveas T811dm1.java {
public public class T811dm1 {}
    }]
} FAIL

tcltest::test 8.1.1-duplicate-modifier-2 { compile time
        error to specify a class modifier twice } {
    compile [saveas T811dm2.java {
abstract abstract class T811dm2 {}
    }]
} FAIL

tcltest::test 8.1.1-duplicate-modifier-3 { compile time
        error to specify a class modifier twice } {
    compile [saveas T811dm3.java {
final final class T811dm3 {}
    }]
} FAIL

tcltest::test 8.1.1-duplicate-modifier-4 { compile time
        error to specify a class modifier twice } {
    compile [saveas T811dm4.java {
strictfp strictfp class T811dm4 {}
    }]
} FAIL

