tcltest::test 12.4.2-runtime-1 { A compiler must emit an initializer of a
        non-constant class variable, even if the initializer is known to
        be the default value of the variable } {runtime} {
    compile_and_run [saveas T1242r3b.java {
class T1242r3a {
    static {
        T1242r3b.i = 1;
        T1242r3b.s = "oops";
    }
}
class T1242r3b extends T1242r3a {
    static int i = 0;
    static String s = null;
    public static void main(String[] args) {
        System.out.print(i + " " + s);
    }
}
}]
} {0 null}
