tcltest::test non-jls-jsr41.3-runtime-1 { Asserts must be enabled before
        class is initialized } {assert && runtime} {
    compile_and_run [saveas T413r1.java {
class T413r1 {
    public static void main(String args[]) {
        T413r1.class.getClassLoader().setDefaultAssertionStatus(false);
        Baz.testAsserts(); // Will execute after Baz is initialized.
    }
}
class Bar {
    static {
        Baz.testAsserts(); // Will execute before Baz is initialized!
    }
}
class Baz extends Bar {
    static void testAsserts(){
        boolean enabled = false;
        assert enabled = true;
        System.out.print(enabled + " ");
    }
}
    }]
} {true false }

