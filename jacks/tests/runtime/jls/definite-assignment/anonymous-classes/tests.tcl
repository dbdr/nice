tcltest::test 16.5-runtime-1 { Test access of definitely assigned local
        variable in an anonymous class } {runtime} {
    compile_and_run [saveas T165r1.java {
class T165r1 {
    T165r1(int j) {}
    public static void main(String[] args) {
        final int i;
        new T165r1(i = 1) {
            { System.out.print(i); }
        };
    }
}
    }]
} 1

tcltest::test 16.5-runtime-2 { Test access of definitely assigned local
        variable in an anonymous class } {runtime} {
    compile_and_run [saveas T165r2.java {
class T165r2 {
    public static void main(String[] args) {
        final int i;
	i = 1;
	class Local { int j = i; }
	new Local() {
	    int i = j + 1;
	    { System.out.print(i); }
	};
    }
}
    }]
} 2

tcltest::test 16.5-runtime-3 { Test access of definitely assigned local
        variable in an anonymous class } {runtime} {
    compile_and_run [saveas T165r3.java {
class T165r3 {
    public static void main(String[] args) {
	new T165r3(1);
    }
    T165r3(final int i) {
	class L {
	    L() {
		System.out.print("a ");
	    }
	    L(int j) {
		this(new L() {
		    { System.out.print("b" + i + ' '); }
		});
		System.out.print("d" + i + ' ');
            }
	    L(Object o) {
		System.out.print("c ");
	    }
	}
	new L(i) {
	    { System.out.print("e" + i); }
	};
    }
}
    }]
} {a b1 c d1 e1}

tcltest::test 16.5-runtime-4 { Test access of definitely assigned local
        variable in an anonymous class } {runtime} {
    compile_and_run [saveas T165r4.java {
class T165r4 {
    public static void main(String[] args) {
	new T165r4(1);
    }
    T165r4(final int i) {
	class L {
	    private L() {
		System.out.print("a ");
	    }
	    private L(int j) {
		this(new L() {
		    { System.out.print("b "); }
		});
		System.out.print("d" + i + ' ');
            }
	    private L(Object o) {
		System.out.print("c ");
	    }
	}
	new L(i) {
	    { System.out.print("e" + i); }
	};
    }
}
    }]
} {a b c d1 e1}


tcltest::test 16.5-runtime-5 { Test access of definitely assigned local
        variable in an anonymous class } {runtime} {
    compile_and_run [saveas T165r5.java {
class T165r5 {
    public static void main(String[] args) {
	final int i;
	i = 1;
	class A {
	    class B {
		class C {
		    {
			new A() {
			    { System.out.print(i); }
			};
		    }
		}
	    }
	}
	new A(){}.new B(){}.new C(){};
    }
}
    }]
} 1
