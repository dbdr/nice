tcltest::test 8.8.5-runtime-inner-1 { invoke ctor of inner class } {runtime} {

    compile_and_run [saveas T885ri1.java {
class T885ri1 {
    class Inner {
        Inner() {}
    }
    public static void main(String[] args) {
        new T885ri1().new Inner();
        System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 8.8.5-runtime-inner-2 { invoke ctor of inner class } {runtime} {

    compile_and_run [saveas T885ri2.java {
class T885ri2 {
    class Inner {
        Inner(int i) {}
    }
    public static void main(String[] args) {
        new T885ri2().new Inner(1);
        System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 8.8.5-runtime-inner-3 { invoke ctor of inner class } {runtime} {

    compile_and_run [saveas T885ri3.java {
class T885ri3 {
    class Inner {
        Inner() {}
        Inner(int i) {}
    }
    public static void main(String[] args) {
        new T885ri3().new Inner(1);
        System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 8.8.5-runtime-inner-4 { invoke ctor of inner class } {runtime} {

    compile_and_run [saveas T885ri4.java {
class T885ri4 {
    class Inner {
        Inner() {}
        Inner(int i) { this(); }
    }
    public static void main(String[] args) {
        new T885ri4().new Inner(1);
        System.out.print("OK");
    }
}
    }]
} OK
