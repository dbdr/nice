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


tcltest::test 14.10-runtime-local-1 { in a switch, local finals should
        behave like blanks, not compile-time constants } {runtime} {
    compile_and_run [saveas T1410l1.java {
class T1410l1 {
    public static void main(String[] args) {
        switch (args.length + 1) {
            case 0: final int i = args.length + 35; break;
            case 1: i = 1;
                System.out.print(i);
        }
    }
}
    }]
} 1

