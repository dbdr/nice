tcltest::test 8.4.6-runtime-miranda-1 { Abstract classes no longer need Miranda
        methods (generated place-holders in classes which inherit an abstract
        method from an interface without overriding it) } {runtime} {
    compile_and_run [saveas T846rm1c.java {
interface T846rm1a {
    void m();
}
abstract class T846rm1b implements T846rm1a {}
abstract class T846rm1c extends T846rm1b implements T846rm1a {
    public static void main(String[] args) {
        boolean ok = true;
        try {
            T846rm1a.class.getDeclaredMethod("m", null);
        } catch (Exception e) {
            System.out.println("missing m() in class a");
            ok = false;
        }
        try {
            T846rm1b.class.getDeclaredMethod("m", null);
            System.out.println("bogus Miranda m() in class b");
            ok = false;
        } catch (Exception e) {
        }
        try {
            T846rm1c.class.getDeclaredMethod("m", null);
            System.out.println("bogus Miranda m() in class c");
            ok = false;
        } catch (Exception e) {
        }
        if (ok) System.out.print("OK");
    }
}
}]
} OK

tcltest::test 8.4.6.4-multiple-runtime-1 { It is possible to have access to
        multiple methods, when one is not inherited across package boundaries.
        The VM is required to bypass polymorphism from classes that cannot
        access the specified method } {runtime} {
    compile_and_run [saveas p2/T8464mr1b.java {
package p2;
public class T8464mr1b extends p1.T8464mr1a {
    public int m() { return 2; }
}
    }] [saveas p1/T8464mr1a.java {
package p1;
public class T8464mr1a {
    int m() { return 1; }
    public static void main(String[] args) {
	T8464mr1c c = new T8464mr1c();
	T8464mr1a a = c;
	System.out.print(a.m() + " " + c.m());
    }
}
class T8464mr1c extends p2.T8464mr1b {
    // inherits b.m(), but it does not override a.m()
}
    }]
} {1 2}
