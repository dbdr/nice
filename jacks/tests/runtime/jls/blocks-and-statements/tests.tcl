tcltest::test 14-runtime-jump-1 { Generic test of large
        goto_w (can occur in for, if, while, do,
        switch, try, break, and continue) } {runtime} {
    set class_data "
class T14rj1 \{
    public static void main(String\[\] args) \{
        int i = 1;
        for (int j = 0; j < 2; j++) \{\n"
    set count 0
    while {$count < 3500} {
        append class_data "\t    i = 366 * i % 534;\n"
        incr count
    }
    append class_data "\t\}
        if (i == 210)
            System.out.print(\"OK\");
        else
            System.out.print(\"FAIL\");
    \}
\}\n"
    compile_and_run [saveas T14rj1.java $class_data]
} OK

tcltest::test 14-runtime-jump-2 { Generic test of large
        goto_w (can occur in for, if, while, do,
        switch, try, break, and continue) } {runtime} {
    set class_data "
class T14rj2 \{
    public static void main(String\[\] args) \{
        int i = 1;
        int j = 0;
        Object o = null;
        while (o == null) \{
            if (++j == 2) o = \"\";\n"
    set count 0
    while {$count < 3500} {
        append class_data "\t    i = 366 * i % 534;\n"
        incr count
    }
    append class_data "\t\}
        if (i == 210)
            System.out.print(\"OK\");
        else
            System.out.print(\"FAIL\");
    \}
\}\n"
    compile_and_run [saveas T14rj2.java $class_data]
} OK

