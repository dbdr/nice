tcltest::test 14.10-runtime-verifier-1 { runtime switch test for values
        between 0x7ffffff0  and 0x7fffffff } {runtime} {
    compile_and_run [saveas T1410v1.java {
class T1410v1 {
  public static void main(String [] args) {
    // prevent compiler optimization of switch by using args.length
    switch (args.length + 0x7ffffff2) {
    case 0x7ffffff0:
    case 0x7ffffff1:
    case 0x7ffffff2:
    case 0x7ffffff3:
      System.out.print("OK");
      break;
    default:
      System.out.print("NOT_OK");
    }
  }
}
    }]
} OK


tcltest::test 14.10-runtime-verifier-2 { runtime switch test exposes bug in
        javac when variable declaration occurs in dead code } {runtime} {
    compile_and_run [saveas T1410v2.java {
class T1410v2 {
    public static void main(String[] args) {
	switch (args.length) {
        case 1:
	    if (true)
                break;
	    int i = 1;
        case 0:
	    i = 2;
	    System.out.print(i + " ");
	}
	System.out.print(args.getClass().getName());
    }
}
    }]
} {2 [Ljava.lang.String;}

tcltest::test 14.10-runtime-fallthrough-1 { runtime test for fallthrough
        of cases } {runtime} {
    compile_and_run [saveas T1410f1.java {
class T1410f1 {
    static void howMany(int k) {
        switch (k) {
            case 1: System.out.print("one ");
            case 2: System.out.print("too ");
            case 3: System.out.println("many");
        }
    }
    public static void main(String[] args) {
        howMany(3);
        howMany(2);
        howMany(1);
    }
}
    }]
} {many
too many
one too many
}

tcltest::test 14.10-runtime-fallthrough-2 { runtime test for break
    stopping fallthrough } {runtime} {
    compile_and_run [saveas T1410f2.java {
class T1410f2 {
    static void howMany(int k) {
        switch (k) {
            case 1: System.out.println("one");
            break;            // exit the switch
            case 2: System.out.println("two");
            break;            // exit the switch
            case 3: System.out.println("many");
            break;            // not needed, but good style
        }
    }
    public static void main(String[] args) {
        howMany(1);
        howMany(2);
        howMany(3);
    }
}
    }]
} {one
two
many
}


tcltest::test 14.10-runtime-break-1 { test optimization of switch statement,
        jikes bug 3113 } {runtime} {
    compile_and_run [saveas T1410b1.java {
class T1410b1 {
    public static void main(String[] args) {
	label:
	switch (args.length) {
	    case 0:
	    break label;
	}
	System.out.print("OK");
    }
}
    }]
} OK

tcltest::test 14.10-runtime-break-2 { test optimization of switch statement,
        jikes bug 3212 } {runtime} {
    compile_and_run -g [saveas T1410b2.java {
class T1410b2 {
    public static void main(String[] args) {
	System.out.print("OK");
	switch (args.length) {
        case 1:
	    int i = 1;
	    break;
	}
    }
}
    }]
} OK

