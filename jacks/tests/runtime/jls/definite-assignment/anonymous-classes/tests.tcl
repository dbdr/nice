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
