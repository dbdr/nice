tcltest::test 15.13.2-runtime-example-1 { JLS example } {runtime} {
    compile_and_run [saveas T15132re1.java {
class T15132re1 {
    public static void main(String[] args) {
	int[] a = { 11, 12, 13, 14 };
	int[] b = { 0, 1, 2, 3 };
	System.out.print(a[(a=b)[3]]);
    }
}
    }]
} 14

tcltest::test 15.13.2-runtime-example-2 { JLS example } {runtime} {
    compile_and_run [saveas T15132re2.java {
class T15132re2 {
    public static void main(String[] args) {
	int index = 1;
	try {
	    skedaddle()[index = 2]++;
	} catch (Exception e) {
	    System.out.print(index);
	}
    }
    static int[] skedaddle() throws Exception {
	throw new Exception("Ciao");
    }
}
    }]
} 1

tcltest::test 15.13.2-runtime-example-3 { JLS example } {runtime} {
    compile_and_run [saveas T15132re3.java {
class T15132re3 {
    public static void main(String[] args) {
	int index = 1;
	try {
	    nada()[index = 2]++;
	} catch (Exception e) {
	    System.out.print(index);
	}
    }
    static int[] nada() { return null; }
}
    }]
} 2

tcltest::test 15.13.2-runtime-example-4 { JLS example } {runtime} {
    compile_and_run [saveas T15132re4.java {
class T15132re4 {
    public static void main(String[] args) {
	int[] a = null;
	try {
	    int i = a[vamoose()];
	    System.out.print(i);
	} catch (NullPointerException e) {
	} catch (Exception e) {
	    System.out.print("OK");
	}
    }
    static int vamoose() throws Exception {
	throw new Exception("Twenty-three skidoo!");
    }
}
    }]
} OK
