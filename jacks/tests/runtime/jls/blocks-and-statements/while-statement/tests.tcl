tcltest::test 14.11-runtime-1 { Test optimization of loops, from jikes bug
        3084 } {runtime} {
    compile_and_run [saveas T1411r1.java {
class T1411r1 {
    public static void main(String[] args) {
	int i = 0;
        while (i < 10) {
            try {
                i++;
		if (i == 5)
                    throw new Exception();
            } catch (Exception ex) {
		System.out.print("a ");
                continue;
            }
        }
	System.out.print(i);
    }
}
    }]
} {a 10}
