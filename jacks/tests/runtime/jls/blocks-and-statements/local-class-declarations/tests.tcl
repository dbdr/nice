tcltest::test 14.3-1-runtime-1 { local classes are never implicitly static,
        even in a static context } {runtime} {
    compile_and_run [saveas T1431r1.java {
import java.lang.reflect.Modifier;
class T1431r1 {
    public static void main(String[] args) {
        class Local {}
        Local l = new Local();
        System.out.print(Modifier.isStatic(l.getClass().getModifiers()));
        System.out.print(' ');
        new T1431r1().foo();
    }
    void foo() {
        class Local1 {}
        Local1 l = new Local1();
        System.out.print(Modifier.isStatic(l.getClass().getModifiers()));
    }        
}
    }]
} {false false}
