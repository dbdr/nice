tcltest::test 12.5-runtime-1 { Variable initializers are executed after the
        explicit or implicit superclass constructor, but before all
        other statements of the constructor } {runtime} {
    compile_and_run [saveas T125r1.java {
class Super {
    Super(int x) {
        method(x);
    }

    void method(int x) { }
}
class T125r1 extends Super {
    private int i1 = 1;
    private Integer i2 = new Integer(1);
    void method(int x) {
        i1 = x;
        i2 = new Integer(x);
        System.out.print("A ");
    }
    T125r1() {
        super(2); // superclass calls method()
    }
    public static void main(String[] args) {
        T125r1 t = new T125r1();
        System.out.print(t.i1 + " " + t.i2);
    }
}
    }]
} {A 1 1}        

tcltest::test 12.5-runtime-2 { Example in JLS } {runtime} {
    compile_and_run [saveas T125r2.java {
class Super {
    Super() { printThree(); }
    void printThree() { System.out.println("three"); }
}
class T125r2 extends Super {
    int three = (int)Math.PI;  // That is, 3
    public static void main(String[] args) {
        T125r2 t = new T125r2();
        t.printThree();
    }
    void printThree() { System.out.print(three); }
}
    }]
} {03}

tcltest::test 12.5-runtime-3 { A compiler must emit an initializer of a
        non-constant instance variable, even if the initializer is known to
        be the default value of the variable } {runtime} {
    compile_and_run [saveas T125r3b.java {
class T125r3a {
    T125r3a() { m(); }
    void m() {}
}
class T125r3b extends T125r3a {
    int i = 0;
    String s = null;
    void m() {
        i = 1;
        s = "oops";
    }
    public static void main(String[] args) {
        T125r3b t = new T125r3b();
        System.out.print(t.i + " " + t.s);
    }
}
}]
} {0 null}
