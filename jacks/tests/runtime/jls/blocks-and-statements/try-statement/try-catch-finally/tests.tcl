tcltest::test 14.19.2-runtime-try-1 { runtime try
        catch finally test with synchronize statement } {runtime} {
    compile_and_run [saveas T14192rt1.java {
class T14192rt1 {
    public static void main(String [] args) {
        Object o = new Object();
        try {
            synchronized (o) {
                System.out.print("O");
                return;
            }
        }
        finally {
            try {
                raise();
            }
            catch (Exception e) {
                System.out.print("K");
            }
        }
    }
    public static void raise() throws Exception {
        throw new Exception();
    }
}
    }]
} OK


tcltest::test 14.19.2-runtime-jump-1 { test of jsr_w } {runtime} {
    set class_data "
class T14192rj1 \{
    public static void main(String\[\] args) \{
        int i = 1;
        try \{
            if (i == 0)
                throw new Exception();
            return;
        \} catch (Exception e) \{\n"
    set count 0
    while {$count < 3500} {
        append class_data "\t    i = 366 * i % 534;\n"
        incr count
    }
    append class_data "\t\} finally \{
            if (i == 1)
                System.out.print(\"OK\");
            else
                System.out.print(\"FAIL\");
        \}
    \}
\}\n"
    compile_and_run [saveas T14192rj1.java $class_data]
} OK
