tcltest::test 12.4.1-runtime-1 { Class initialization does not occur from
        instance field access, or from constant variable access, even when
        the access is not a constant expression } {runtime} {
    compile_and_run [saveas T1241r1b.java {
class T1241r1a {
    final int i = 1;
    static final int j = 1;
    static { System.out.print("3 "); }
}
class T1241r1b {
    public static void main(String[] args) {
	T1241r1a a = null;
	System.out.print(a.j + " ");
	try {
	    System.out.print(a.i);
	} catch (Exception e) {
	    System.out.print("2 ");
	    a = new T1241r1a();
	    System.out.print(a.i + 3);
	}
    }
}
    }]
} {1 2 3 4}

tcltest::test 12.4.1-runtime-2 { Class literals may not cause class
        initialization. Note that this is debatable, as it is not specified
        in the JLS, but Sun bug 4419673, as clarified by Neal Gafter in jacks
        bug 3109, is arguing that the JLS is correct as stands. } {runtime} {
    compile_and_run [saveas T1241r2b.java {
class T1241r2a {
    static { System.out.print(" oops "); }
}
class T1241r2b {
    public static void main(String[] args) {
	System.out.print('O');
	T1241r2a.class.toString();
	System.out.print('K');
    }
}
    }]
} OK

tcltest::test 12.4.1-runtime-3 { Class literals may not cause class
        initialization. Note that this is debatable, as it is not specified
        in the JLS, but Sun bug 4419673, as clarified by Neal Gafter in jacks
        bug 3109, is arguing that the JLS is correct as stands. } {runtime} {
    compile_and_run [saveas T1241r3b.java {
class T1241r3a {
    static { System.out.print(" oops "); }
    static class Inner {
	static Class c = Inner.class;
    }
}
class T1241r3b {
    public static void main(String[] args) {
	System.out.print('O');
	T1241r3a.Inner.c = null;
	System.out.print('K');
	T1241r3a.Inner.c = T1241r3a.class;
    }
}
    }]
} OK
	
tcltest::test 12.4.1-runtime-4 { Class literals may not cause class
        initialization. Note that this is debatable, as it is not specified
        in the JLS, but Sun bug 4419673, as clarified by Neal Gafter in jacks
        bug 3109, is arguing that the JLS is correct as stands. } {runtime} {
    compile_and_run [saveas T1241r4b.java {
interface T1241r4a {
    int i = new Object() {
	{ System.out.print(" oops "); }
	int i = 1;
    }.i;
    class Inner {
	static Class c = Inner.class;
    }
}
class T1241r4b {
    public static void main(String[] args) {
	System.out.print('O');
	T1241r4a.Inner.c = null;
	System.out.print('K');
	T1241r4a.Inner.c = T1241r4a.class;
    }
}
    }]
} OK
	
