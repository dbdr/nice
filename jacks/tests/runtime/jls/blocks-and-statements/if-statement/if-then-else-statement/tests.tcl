tcltest::test 14.9.2-runtime-1 { Test optimization of if-then-else
        statements } {runtime} {
    compile_and_run [saveas T1492r1.java {
class T1492r1 {
    public static void main(String[] args) {
	if (true) {
	    System.out.print(1);
	} else {
	    System.out.print('a');
	}
	if (true) {
	} else {
	    System.out.print('b');
	}
	if (true) {
	    System.out.print(2);
	} else {
	}
	if (false) {
	    System.out.print('c');
	} else {
	    System.out.print(3);
	}
	if (false) {
	    System.out.print('d');
	} else {
	}
	if (false) {
	} else {
	    System.out.print(4);
	}
	boolean b = true;
	if (b) {
	    System.out.print(5);
	} else {
	    System.out.print('e');
	}
	if (b) {
	} else {
	    System.out.print('f');
	}
	if (b) {
	    System.out.print(6);
	} else {
	}
	b = false;
	if (b) {
	    System.out.print('g');
	} else {
	    System.out.print(7);
	}
	if (b) {
	    System.out.print('h');
	} else {
	}
	if (b) {
	} else {
	    System.out.print(8);
	}
    }
}
    }]
} 12345678
