tcltest::test 6.6.1-runtime-privateconstructor-1 { Private constructors
        are accessible from within the body of
        the top level class that encloses the
        constructor  } {runtime} {

    saveas T661rpc1.java {
public class T661rpc1 {
class T661rpc1_innersuper {
    private T661rpc1_innersuper() { }
}
class T661rpc1_inner extends T661rpc1_innersuper {
    T661rpc1_inner() { super(); }
}
public static void main(String[] args) {
    new T661rpc1().new T661rpc1_inner();
    System.out.print("OK");
}
}
    }
    compile_and_run T661rpc1.java
} {OK}

