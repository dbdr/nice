tcltest::test 15.25-runtime-1 { Test optimization in ?: } {runtime} {
    compile_and_run [saveas T1525r1.java {
class T1525r1 {
    public static void main(String[] args) {
	boolean b = false;
	System.out.print(((b = true) ? null : null) + " " + b);
    }
}
    }]
} {null true}

tcltest::test 15.25-runtime-2 { Test optimization in ?: } {runtime} {
    compile_and_run [saveas T1525r2.java {
class T1525r2 {
    public static void main(String[] args) {
	boolean b = false;
	System.out.print(((b = true) ? 1 : 1) + " " + b);
    }
}
    }]
} {1 true}

tcltest::test 15.25-runtime-3 { Test optimization in ?: } {runtime} {
    compile_and_run [saveas T1525r3.java {
class T1525r3 {
    public static void main(String[] args) {
	boolean b = false;
	System.out.print(((b = true) ? "a" : "a") + " " + b);
    }
}
    }]
} {a true}

tcltest::test 15.25-runtime-4 { Test optimization in ?: } {runtime} {
    compile_and_run [saveas T1525r4.java {
class T1525r4 {
    public static void main(String[] args) {
	boolean b = true;
	System.out.print(((b = false) ? -0. : 0) + " " + b);
    }
}
    }]
} {0.0 false}

tcltest::test 15.25-runtime-5 { Test optimization in ?: } {runtime} {
    compile_and_run [saveas T1525r5.java {
class T1525r5 {
    public static void main(String[] args) {
	boolean b = false;
	if ((b = true) ? true : true)
            System.out.print(b);
    }
}
    }]
} true

tcltest::test 15.25-runtime-6 { Test side effects in ?: } {runtime} {
    compile_and_run [saveas T1525r6.java {
class T1525r6 {
    static boolean a() {
	System.out.print('a');
	return false;
    }
    static boolean b() {
	System.out.print('b');
	return false;
    }
    public static void main(String[] args) {
	boolean b = false;
	if (true | (b ? a() : b()))
            System.out.print(1);
	b = true;
	if (false & (b ? a() : b())) {
	    b = false;
	} else System.out.print(2);
    }
}
    }]
} b1a2
