tcltest::test 15.11.1-runtime-static-1 { The Primary qualifier of a static
        field reference must be evaluated, then discarded } {runtime} {
    compile_and_run [saveas T15111rs1.java {
class T15111rs1 {
    static T15111rs1 a() {
	System.out.print("a ");
	return t;
    }
    static int i = 0;
    static T15111rs1 t = new T15111rs1();

    T15111rs1() {}
    T15111rs1(char c) { System.out.print("c "); }
    public String toString() {
	System.out.print("d ");
	return null;
    }
    public static void main(String[] args) {
	a().i++; // method invocation
	a().t.i++; // field access
	(t.a()).i++; // parenthesized expression
	new T15111rs1('c').i++; // instance creation
	((T15111rs1)a()).i++; // safe cast
	("" + t).CASE_INSENSITIVE_ORDER.toString(); // string conversion
	(t = null).i++; // assignment
	if (t != null) System.out.print("Oops ");
	(t == null ? a() : a()).i++; // conditional
	if (i != 7) System.out.print("Oops ");
	i = 0;
	T15111rs1[] ta = {t};
	ta[i++].i++; // array access
	if (i != 2) System.out.print("Oops ");
    }
}
    }]
} {a a a c a d a }

tcltest::test 15.11.1-runtime-static-2 { The Primary qualifier of a static
        field reference may complete abruptly, before anything else } {runtime} {
    compile_and_run [saveas T15111rs2.java {
class T15111rs2 {
    static int i = 0;
    static T15111rs2 a() { throw new RuntimeException(); }
    static T15111rs2 t = null;
    T15111rs2 t1 = null;

    T15111rs2() {}
    T15111rs2(int i) { throw new IllegalArgumentException(); }
    public String toString() {
	int i = 0;
	i /= i; // throw ArithmeticException, rather than return
	return null;
    }
    public static void main(String[] args) {
	try {
	    t.t1.i++; // instance field access
	} catch (NullPointerException e) {
	    System.out.print("1 ");
	}
	T15111rs2[] ta = {};
	try {
	    ta[0].i++; // array access
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.print("2 ");
	}
	try {
	    a().i++; // method call
	} catch (RuntimeException e) {
	    System.out.print("3 ");
	}
	Object o = "";
	try {
	    ((T15111rs2)o).i++; // unsafe cast
	} catch (ClassCastException e) {
	    System.out.print("4 ");
	}
	try {
	    new T15111rs2(1).i++; // constructor
	} catch (IllegalArgumentException e) {
	    System.out.print("5");
	}
	if (i != 0) System.out.print("Oops ");
    }
}
    }]
} {1 2 3 4 5}

tcltest::test 15.11.1-runtime-static-3 { The Primary qualifier of a static
        field reference may complete abruptly, before anything else.
        This test only works against JDK 1.2 or higher } {runtime} {
    compile_and_run [saveas T15111rs3.java {
class T15111rs3 {
    public String toString() { throw new IllegalArgumentException(); }
    public static void main(String[] args) {
	try { // string conversion will fail
	    java.util.Comparator c =
	        ("foo" + new T15111rs3()).CASE_INSENSITIVE_ORDER;
	    System.out.print("Oops ");
	} catch (IllegalArgumentException e) {
	    System.out.print("OK");
	}
    }
}
    }]
} OK
