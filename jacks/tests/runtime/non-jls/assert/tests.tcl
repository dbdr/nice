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

tcltest::test non-jls-jsr41.3-runtime-2 { Asserts do not cause class
        initialization } {assert && runtime} {
    compile_and_run [saveas T413r2b.java {
class T413r2a {
    static { System.out.print(" oops "); }
    static class Inner {
	static { System.out.print('O'); }
	static void m() {
	    boolean b = true;
	    assert b;
	}
    }
}
class T413r2b {
    public static void main(String[] args) {
	System.out.print('A');
	T413r2a.Inner.m();
	System.out.print('K');
    }
}
    }]
} AOK
