tcltest::test 15.14-runtime-1 { Postfix ops have higher precedence
        than prefix } {runtime} {
    compile_and_run [saveas T15141.java {
class T15141 {
    public static void main(String[] args) {
        int a = 1, b = 1;
        System.out.print((-a++ == -1) + " ");
        System.out.print((-(b++) == -1) + " ");
        System.out.print(a == b);
    }
}
    }]
} {true true true}

tcltest::test 15.14-runtime-2 { Postfix ops have higher precedence
        than prefix } {runtime} {
    compile_and_run [saveas T15142.java {
class T15142 {
    public static void main(String[] args) {
        int a = 2, b = 2;
        System.out.print((-a-- == -2) + " ");
        System.out.print((-(b--) == -2) + " ");
        System.out.print(a == b);
    }
}
    }]
} {true true true}

tcltest::test 15.14-runtime-3 { Test postfix operation } {runtime} {
    compile_and_run [saveas T1514r3.java {
class T1514r3 {
    static byte s = 127; // static field
    byte i = 127; // instance field
    public static void main(String[] args) {
        new T1514r3();
    }
    T1514r3() {
        byte[] a = {127}; // array access
        byte l = 127; // local variable
        System.out.print(s++ + " " + i++ + " " + a[0]++ + " " + l++);
        System.out.print(" " + s + " " + i + " " + a[0] + " " + l);
    }
}
    }]
} {127 127 127 127 -128 -128 -128 -128}

tcltest::test 15.14-runtime-4 { Test postfix operation } {runtime} {
    compile_and_run [saveas T1514r4.java {
class T1514r4 {
    static byte s = -128; // static field
    byte i = -128; // instance field
    public static void main(String[] args) {
        new T1514r4();
    }
    T1514r4() {
        byte[] a = {-128}; // array access
        byte l = -128; // local variable
        System.out.print(s-- + " " + i-- + " " + a[0]-- + " " + l--);
        System.out.print(" " + s + " " + i + " " + a[0] + " " + l);
    }
}
    }]
} {-128 -128 -128 -128 127 127 127 127}
