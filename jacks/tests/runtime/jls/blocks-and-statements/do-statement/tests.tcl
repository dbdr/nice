tcltest::test 14.12-runtime-1 { Test optimization of do-while
        loops } {runtime} {
    compile_and_run [saveas T1412r1.java {
class T1412r1 {
    static int i = -2;
    public static void main(String[] args) {
	do
            test();
	while (false);
	System.out.print(i);
    }
    static boolean test() {
	try {
	    do {
		if (i++ == 0)
		    return true;
	    } while (true);
        } finally {
	    i++;
	}
    }
}
    }]
} 2
