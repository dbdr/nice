tcltest::test 14.19.1-runtime-1 { Test evaluation of try-catch blocks, with
        synchronized statement } {runtime} {
    compile_and_run [saveas T14191r1.java {
class T14191r1 {
    public static void main(String[] o) {
	synchronized (o) {
	    try {
		throw new Error();
	    } catch (Error e) {
		System.out.print("OK");
		return;
	    }
	}
    }
}
    }]
} OK

tcltest::test 14.19.1-runtime-2 { Test evaluation of try-catch blocks, with
        synchronized statement } {runtime} {
    compile_and_run [saveas T14191r2.java {
class T14191r2 {
    public static void main(final String[] args) {
	RuntimeException e = new RuntimeException();
	try {
	    foo(e);
	    System.out.print("exception not thrown");
	} catch (Exception ex) {
	    System.out.print(ex == e ? "OK" : "wrong exception thrown");
	}
    }
    static int foo(RuntimeException o) {
	synchronized (o) {
	    try {
		return bar();
	    } catch (RuntimeException e) {
		throw o;
	    }
	}
    }
    static int bar() { throw new RuntimeException(); }
}
    }]
} OK

tcltest::test 14.19.1-runtime-3 { Evaluation of empty try-block is legal,
        even though it is a no-op } {runtime} {
    compile_and_run [saveas T14191r3.java {
class T14191r3 {
    public static void main(String[] args) {
	System.out.print(1);
	try {
	} catch (Error e) {
	}
	System.out.print(2);
	try {
	} catch (Error e) {
	}
	System.out.print(3);
    }
}
    }]
} 123
