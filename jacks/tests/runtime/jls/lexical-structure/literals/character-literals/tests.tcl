tcltest::test 3.10.4-runtime-coalescense-1 { Ensure that characters are
        correctly coalesced into strings, even when the compiler can
        apply optimizations on constant values. } {runtime} {
    compile_and_run [saveas T3104rc1.java {
class T3104rc1 {
    public static void main(String [] args) {
	String s = " ";
	System.out.print(s + 'O' + "K");
	System.out.print(s + 'O' + 1);
    }
}
    }]
} { OK O1}
