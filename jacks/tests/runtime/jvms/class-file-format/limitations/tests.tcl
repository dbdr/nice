tcltest::test 4.10-jvms-runtime-1 { Check that all the jvms tests that should
        compile actually produce valid class files } {jvm runtime} {
# First, compile 4.10-jvms-4
    set buff ""
    set interfaces 7
    set over 10000
    for {set j 0} {$j < $interfaces} {incr j} {
	append buff "interface T410jvms4_${j} \{\n"
	for {set i 0} {$i < $over} {incr i} {
	    append buff "\tint v${j}_${i} = 1;\n"
	}
	append buff "\}\n"
    }

    append buff "public class T410jvms4 implements T410jvms4_0"
    for {set j 1} {$j < $interfaces} {incr j} {
        append buff ",\n\tT410jvms4_${j}"
    }

    append buff " \{\n\t\tint v = 1;\n\}\n"

    compile [saveas T410jvms4.java $buff]

# Next, compile 4.10-jvms-6
    set buff ""
    set interfaces 7
    set over 10000
    for {set j 0} {$j < $interfaces} {incr j} {
	append buff "interface T410jvms6_${j} \{\n"
	for {set i 0} {$i < $over} {incr i} {
	    append buff "\tint m${j}_${i}();\n"
	}
	append buff "\}\n"
    }

    append buff "public abstract class T410jvms6 implements T410jvms6_0"
    for {set j 1} {$j < $interfaces} {incr j} {
        append buff ",\n\tT410jvms6_${j}"
    }

    append buff " \{\n\t\tvoid m() \{\}\n\}\n"

    compile [saveas T410jvms6.java $buff]

# Now, check that those tests passed
    compile_and_run [saveas T410jr1.java {
class T410jr1 {
    public static void main(String[] args) {
	try {
	    // Exploit fact that Class.forName causes class initialization,
	    // which runs the verifier to see if the class compiled successfully
	    Class.forName("T410jvms4");
	    Class.forName("T410jvms4_0");
	    Class.forName("T410jvms4_1");
	    Class.forName("T410jvms4_2");
	    Class.forName("T410jvms4_3");
	    Class.forName("T410jvms4_4");
	    Class.forName("T410jvms4_5");
	    Class.forName("T410jvms4_6");
	    System.out.print("4:OK ");
	} catch (ClassNotFoundException e) {
	    System.out.print("4:inconclusive ");
	} catch (Error e) {
	    System.out.print("4:failed(" + e + ") ");
	}
	try {
// Thanks to a nice infinite loop bug in java 1.4.1, do not test this class.
//	    Class.forName("T410jvms6");

	    Class.forName("T410jvms6_0");
	    Class.forName("T410jvms6_1");
	    Class.forName("T410jvms6_2");
	    Class.forName("T410jvms6_3");
	    Class.forName("T410jvms6_4");
	    Class.forName("T410jvms6_5");
	    Class.forName("T410jvms6_6");
	    System.out.print("6:OK ");
	} catch (ClassNotFoundException e) {
	    System.out.print("6:inconclusive ");
	} catch (Error e) {
	    System.out.print("6:failed(" + e + ") ");
	}
    }
}
    }]
} {4:OK 6:OK }
