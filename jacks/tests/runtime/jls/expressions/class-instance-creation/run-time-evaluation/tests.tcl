tcltest::test 15.9.4-runtime-creation-1 { A null qualifier must throw a
        NullPointerException before anything else happens } {runtime} {
    compile_and_run [saveas T1594rc1.java {
class T1594rc1 {
    class Inner {
        Inner(int i) {}
    }
    public static void main(String[] args) {
        int j = 1;
        T1594rc1 t = null;
        try {
            t.new Inner(j = 2);
        } catch (NullPointerException npe) {
            if (j == 2)
                System.out.println("NullPointer must precede argument evaluation");
            else
                System.out.print("OK");
            return;
        }
        System.out.println("Qualified instance creation must throw NullPointerException");
    }
}
    }]
} OK

tcltest::test 15.9.4-runtime-creation-2 { OutOfMemoryError precedes argument
        evaluation - example in 15.9.6 } {runtime} {
    compile_and_run [saveas T1594rc2.java {
class List {
    int value;
    List next;
    static List head = new List(0);
    List(int n) { value = n; next = head; head = this; }
}
class T1594rc2 {
    public static void main(String[] args) {
        int id = 0, oldid = 0;
        try {
            for (;;) {
                ++id;
                new List(oldid = id);
            }
        } catch (OutOfMemoryError e) {
            List.head = null; // try to reclaim memory
            System.out.print(oldid==id);
        }
    }
}
    }]
} false

tcltest::test 15.9.4-runtime-creation-3 { Check that all (non-constant) fields
        have default value, arguments are processed left to right, and
        instance initializer is called only after superconstructor } {runtime} {
    compile_and_run [saveas T1594rc3.java {
class T1594rc3 {
    int i = getInt(); // will not be called
    T1594rc3(int a, int b, int c) {}
    static int getInt() {
        System.out.print("Oops ");
        return 1;
    }
    { System.out.print("Bad "); } // will not be called
    static int choke() { throw new RuntimeException(); }
    public static void main(String[] args) {
        int a = 0, b = 0, c = 0;
        try {
            new T1594rc3(a = 1, b = choke(), c = 1);
        } catch (RuntimeException re) {
            if (a == 1 && b == 0 && c == 0)
                System.out.print("OK");
            else
                System.out.print("Out of order");
            return;
        }
        System.out.println("Should not get here");
    }
}
    }]
} OK

tcltest::test 15.9.4-runtime-creation-4 { Instance initializers are called
        in textual order after superconstructor } {runtime} {
    compile_and_run [saveas T1594rc4.java {
class Super {
    Super() {
        System.out.print("2 ");
    }
    { System.out.print("1 "); }
}
class T1594rc4 extends Super {
    { System.out.print("3 "); }
    T1594rc4() {
        System.out.print("5");
    }
    public static void main(String[] args) {
        new T1594rc4();
    }
    { System.out.print("4 "); }
}
    }]
} {1 2 3 4 5}

tcltest::test 15.9.4-runtime-creation-5 { A null qualifier must throw a
        NullPointerException before anything else happens } {runtime} {
    compile_and_run [saveas T1594rc5.java {
class T1594rc5 {
    class Inner {
	Inner(int i) {}
    }
    public static void main(String[] args) {
	int i = 1;
	try {
	    T1594rc5 t = null;
	    t.new Inner(i++) {};
	} catch (NullPointerException e) {
	    System.out.print(i + " ");
	}
	try {
	    ((T1594rc5) null).new Inner(i++) {};
	} catch (NullPointerException e) {
	    System.out.print(i);
	}
    }
}
    }]
} {1 1}
