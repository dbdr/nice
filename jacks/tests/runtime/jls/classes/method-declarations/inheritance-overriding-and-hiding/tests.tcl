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
