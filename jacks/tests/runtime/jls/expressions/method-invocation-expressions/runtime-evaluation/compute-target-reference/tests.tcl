tcltest::test 15.12.4.1-runtime-static-1 { The Primary qualifier of a static
        method invocation must be evaluated, then discarded } {runtime} {
    compile_and_run [saveas T151241rs1.java {
class T151241rs1 {
    static T151241rs1 a() {
	System.out.print("a ");
	return t;
    }
    static void b() { System.out.print("b "); }
    static T151241rs1 t = new T151241rs1();

    T151241rs1() {}
    T151241rs1(char c) { System.out.print("c "); }
    public String toString() {
	System.out.print("d ");
	return null;
    }
    public static void main(String[] args) {
	a().b(); // method invocation
	a().t.b(); // field access
	(t.a()).b(); // parenthesized expression
	new T151241rs1('c').b(); // instance creation
	((T151241rs1)a()).b(); // safe cast
	("" + t).valueOf(1); // string conversion
	(t = null).b(); // assignment
	if (t != null) System.out.print("Oops ");
	(t == null ? a() : a()).b(); // conditional
	T151241rs1[] ta = {t};
	int i = 0;
	ta[i++].b(); // array access
	if (i != 1) System.out.print("Oops ");
    }
}
    }]
} {a b a b a b c b a b d b a b b }

tcltest::test 15.12.4.1-runtime-static-2 { The Primary qualifier of a static
        method invocation may complete abruptly, before anything else } {runtime} {
    compile_and_run [saveas T151241rs2.java {
class T151241rs2 {
    static int i = 0;
    static T151241rs2 a() { throw new RuntimeException(); }
    static void b(int i) { System.out.print("Oops "); }
    static T151241rs2 t = null;
    T151241rs2 t1 = null;

    T151241rs2() {}
    T151241rs2(int i) { throw new IllegalArgumentException(); }
    public String toString() {
	int i = 0;
	i /= i; // throw ArithmeticException, rather than return
	return null;
    }
    public static void main(String[] args) {
	try {
	    t.t1.b(i++); // instance field access
	} catch (NullPointerException e) {
	    System.out.print("1 ");
	}
	T151241rs2[] ta = {};
	try {
	    ta[0].b(i++); // array access
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.print("2 ");
	}
	try {
	    a().b(i++); // method call
	} catch (RuntimeException e) {
	    System.out.print("3 ");
	}
	Object o = "";
	try {
	    ((T151241rs2)o).b(i++); // unsafe cast
	} catch (ClassCastException e) {
	    System.out.print("4 ");
	}
	try {
	    ("" + new T151241rs2()).valueOf(1); // abrupt binary expression
	    System.out.print("Oops ");
	} catch (ArithmeticException e) {
	    System.out.print("5 ");
	}
	try {
	    new T151241rs2(1).b(i++); // constructor
	} catch (IllegalArgumentException e) {
	    System.out.print("6");
	}
	if (i != 0) System.out.print("Oops ");
    }
}
    }]
} {1 2 3 4 5 6}

tcltest::test 15.12.4.1-runtime-static-3 { The Primary qualifier of a static
        method is discarded } {runtime} {
    compile_and_run [saveas T151241rs3.java {
class T151241rs3 {
    public static void main(String[] args) {
        try {
            Class c = T151241rs3.class.forName("T151241rs3");
            System.out.print((c == T151241rs3.class ? "OK" : "oops"));
        } catch (Exception e) {
            System.out.print("Unexpected: " + e);
        }
    }
}
    }]
} OK
