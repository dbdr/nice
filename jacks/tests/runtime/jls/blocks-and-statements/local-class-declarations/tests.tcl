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

tcltest::test 14.3-1-runtime-2 { local class names must not flatten to the
        same name as an anonymous class } {runtime} {
   compile_and_run [saveas T1431r2.java {
class T1431r2 {
    boolean x1, x2;
    T1431r2() {
        class Local {
            { x1 = true; }
        }
        new Local();
        new Object() {
            class Local {
                { x2 = true; }
            }
        }.new Local();
    }
    public static void main(String[] args) {
        T1431r2 t = new T1431r2();
        System.out.print(t.x1 + " " + t.x2);
    }
}
   }]
} {true true}

tcltest::test 14.3.1-runtime-3 { test use of shadowed local variables in
        local class } {runtime} {
    compile_and_run [saveas T1431r3.java {
class T1431r3 {
    static class Super {
	Object shared;
	Super(Object o) { shared = o; }
    }
    public static void main(String[] args) {
	final Object o = new Object();
	class Local extends Super {
	    Local() {
		super(o);
	    }
	}
	Local a = new Local();
	Local b = new Local();
	System.out.print(a.shared == o && b.shared == o);
    }
}
    }]
} true
