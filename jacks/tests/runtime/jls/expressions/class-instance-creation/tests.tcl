tcltest::test 15.9-runtime-numcalls-1 { Check the number
        of times a constructor is called from a class
        instance creation expression } {runtime} {

    compile_and_run [saveas T159rn1.java {
class T159rn1 {

    private static class Inner {
        public Inner() {
            System.err.print("ctor");
        }

        private void foo() {}
    }

    public static void main(String[] args) {
        new Inner().foo();              // broken in Jikes 1.11
        System.err.print(" -> ");
        (new Inner()).foo();            // okay
    }
}
    }]
} {ctor -> ctor}

