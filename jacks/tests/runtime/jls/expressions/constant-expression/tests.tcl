tcltest::test 15.28-runtime-constant-1 { Constant string expressions must be
        compiled inline, including method returns } {runtime} {
    compile_and_run [saveas T1528rc1.java {
class T1528rc1 {
    static String blah() { return "foo" + "bar"; }
    public static void main(String[] args) {
       System.out.print("foobar" == blah());
    }
}
    }]
} true

tcltest::test 15.28-runtime-constant-2 { Constant string expressions must be
        compiled inline, including method call qualifiers } {runtime} {
    compile_and_run [saveas T1528rc2.java {
class T1528rc2 {
    public static void main(String[] args) {
       System.out.print("foobar" == ("foo" + "bar").toString());
    }
}
    }]
} true

tcltest::test 15.28-runtime-constant-3 { Constant string expressions must be
        compiled inline, including synchronized monitors } {runtime} {
    compile_and_run [saveas T1528rc3.java {
class T1528rc3 extends Thread {
    private static int i = 0;
    public static void main(String[] args) {
       synchronized ("foo" + "bar") {
           // did we sync on the same object? If the compiler correctly
           // inlined the String expression, and the JVM correctly
           // does mutual exclusion, then we have already locked
           // "foobar", stopping the second thread in its tracks.
           System.out.print("1 ");
           new T1528rc3().start(); // build new thread
           try {
               sleep(500); // give second thread a chance
           } catch (InterruptedException e) {
           }
           if (i == 2)
           System.out.println(" Constant not inlined; wrong object locked ");
           else if (i == 0)
           System.out.println(" Indeterminate - thread never ran ");
           else if (i == 1)
           System.out.print("3 ");
           else
           System.out.println(" Unexpected result ");
       }
    }
    public void run() {
       System.out.print("2 ");
       i = 1;
       synchronized ("foobar") {
           i = 2;
       }
       System.out.print("4");
    }
}
    }]
} {1 2 3 4}

tcltest::test 15.28-runtime-constant-4 { Test of the ldc_w bytecode
        instruction, caused when there are more than 255 entries in the
        constant pool. In particular, this exposed a buggy constant in one
        version of jikes which used sipush for a non-short } {runtime} {
    set class_data "
class T1528rc4 \{
    public static void main(String\[\] args) \{
        int i = 0;\n"
    set count 0
    while {$count < 400} {
        append class_data "\ti += 100$count;\n"
        incr count
    }
    append class_data "\tSystem.out.print(i);
    \}
\}\n"
    compile_and_run [saveas T1528rc4.java $class_data]
} 30989800
