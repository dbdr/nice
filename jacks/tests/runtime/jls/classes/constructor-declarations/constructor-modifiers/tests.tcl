tcltest::test 8.8.3-runtime-inner-1 { no modifier for inner class
        constructor } {runtime} {

    compile_and_run [saveas T883ri1.java {
class T883ri1 {
    class Inner {
        Inner() {}
    }
    public static void main(String[] args) {
        new T883ri1().new Inner();
        System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 8.8.3-runtime-inner-2 { modifier for inner class
        constructor } {runtime} {

    compile_and_run [saveas T883ri2.java {
class T883ri2 {
    class Inner {
        public Inner() {}
    }
    public static void main(String[] args) {
        new T883ri2().new Inner();
        System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 8.8.3-runtime-inner-3 { modifier for inner class
        constructor } {runtime} {

    compile_and_run [saveas T883ri3.java {
class T883ri3 {
    class Inner {
        protected Inner() {}
    }
    public static void main(String[] args) {
        new T883ri3().new Inner();
        System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 8.8.3-runtime-inner-4 { modifier for inner class
        constructor } {runtime} {

    compile_and_run [saveas T883ri4.java {
class T883ri4 {
    class Inner {
        private Inner() {}
    }
    public static void main(String[] args) {
        new T883ri4().new Inner();
        System.out.print("OK");
    }
}
    }]
} OK
