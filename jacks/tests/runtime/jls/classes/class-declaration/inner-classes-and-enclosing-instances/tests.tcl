tcltest::test 8.1.2-runtime-1 { Stress test of accessing a final local variable
        through 2 layers of nested local/anonymous classes } {runtime} {
    compile_and_run [saveas T812r1.java {
class T812r1 {
    public static void main(String[] args) {
        new T812r1().foo(1);
    }
    void foo(final int i) {
        class Local {
            Local() {}
            Local(int i) { this(); }
            int foo() {
                return new Local(0) {
                    int j = i;
                }.j;
            }
        }
        System.out.print(new Local().foo());
    }
}
}]
} 1
