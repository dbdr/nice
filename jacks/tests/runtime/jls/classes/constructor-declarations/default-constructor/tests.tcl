tcltest::test 8.8.7-runtime-default-1 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters and invokes the superclass
        constructor with no arguments is provided } {runtime} {

    saveas T887rd1.java {
class  T887rd1_super {
    T887rd1_super() { System.out.print("super "); }
}
public class T887rd1 extends T887rd1_super {
    public static void main(String[] argv) {
        new T887rd1();
        System.out.print("main");
    }
}
    }

    compile_and_run T887rd1.java
} {super main}


tcltest::test 8.8.7-runtime-default-2 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters and invokes the superclass
        constructor with no arguments is provided } {runtime} {

    saveas T887rd2.java {
class  T887rd2_super {
    T887rd2_super() { System.out.print("super "); }
}
public class T887rd2 extends T887rd2_super {
    public static void main(String[] argv) {
        new T887rd2().toString();
        (new T887rd2()).toString();
        System.out.print("main");
    }
}
    }

    compile_and_run T887rd2.java
} {super super main}


tcltest::test 8.8.7-runtime-default-3 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters and invokes the superclass
        constructor with no arguments is provided } {runtime} {

    saveas T887rd3.java {
class  T887rd3_super {
    T887rd3_super() { System.out.print("super "); }
}
public class T887rd3 {

    private static class Inner extends T887rd3_super {}

    public static void main(String[] argv) {
        new Inner().toString();
        System.out.print("+ ");
        (new Inner()).toString();
        System.out.print("main");
    }
}
    }

    compile_and_run T887rd3.java
} {super + super main}


tcltest::test 8.8.7-runtime-accessible-default-1 { The private
        superconstructor is accessible by default constructors
        contained in the same class } {runtime} {
    saveas T887rad1.java {
class T887rad1 {
    private T887rad1() {
        System.out.print("super ");
    }
    static class Static extends T887rad1 {}
    class Inner extends T887rad1 {}
    void foo() {
        class InnerLocal extends T887rad1 {}
        new InnerLocal();
        new T887rad1() {};
    }
    static void bar() {
        class StaticLocal extends T887rad1 {}
        new StaticLocal();
        new T887rad1() {};
    }
    public static void main(String[] args) {
        T887rad1 t = new Static();
        t.new Inner();
        t.foo();
        bar();
        System.out.print("main");
    }
}
    }
    compile_and_run T887rad1.java
} {super super super super super super main}
