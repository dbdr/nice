tcltest::test 14.14-runtime-jump-1 { test of wide jump in break } {runtime} {
    set class_data "
class T1414rj1 \{
    public static void main(String\[\] args) \{
        int i = 1;
        do \{
            if (i == 1)
                break;\n"
    set count 0
    while {$count < 3500} {
        append class_data "\t    i = 366 * i % 534;\n"
        incr count
    }
    append class_data "\t\} while (false);
        if (i == 1)
            System.out.print(\"OK\");
        else
            System.out.print(\"FAIL\");
    \}
\}\n"
    compile_and_run [saveas T1414rj1.java $class_data]
} OK
