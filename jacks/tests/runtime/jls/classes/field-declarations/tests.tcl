tcltest::test 8.3-runtime-1 { Test access to an accessible, but non-inherited,
        private field } {runtime} {
    compile_and_run [saveas T83r1.java {
class T83r1 {
    static int init;
    public int i = ++init;
    class One extends T83r1 {
	private Object i; // does not inherit T83r1.i, but does hide it
    }
    class Two extends One {
	// does not inherit either i
	Two() {
	    T83r1.this.super();
	}
	int j = i; // enclosing access
	int k = ((T83r1) this).i; // access of hidden field
	Object l = super.i; // access of private field
    }
    public static void main(String[] args) {
	Two t = new T83r1().new Two();
	System.out.print(t.j + " " + t.k + " " + t.l);
    }
}
    }]
} {1 2 null}
