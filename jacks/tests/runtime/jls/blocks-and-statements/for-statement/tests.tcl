tcltest::test 14.13-runtime-1 { Test optimization of for loops } {runtime} {
    compile_and_run [saveas T1413r1.java {
class T1413r1 {
    public static void main(String[] args) {
	int i = 0;
	for ( ; i < 100000; i += 12345)
	    if (i < 100000)
	        continue;
	System.out.print(i);
    }
}
    }]
} 111105
