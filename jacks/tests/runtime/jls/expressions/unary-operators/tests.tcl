tcltest::test 15.15-runtime-1 { Prefix ops have higher precedence than
        multiplicative } {runtime} {
    empty_main T15151 {
        int a = 1; int b = 1;
        System.out.print((++a*a == 4) + " ");
        System.out.print(((++b)*b == 4) + " ");
        System.out.print(a == b);
    } true
} {true true true}

tcltest::test 15.15-runtime-2 { Prefix ops have higher precedence
        than multiplicative } {runtime} {
    compile_and_run [saveas T15152.java {
class T15152 {
    public static void main(String[] args) {
        int a = 3, b = 3;
        System.out.print((--a*a == 4) + " ");
        System.out.print(((--b)*b == 4) + " ");
        System.out.print(a == b);
    }
}
    }]
} {true true true}

tcltest::test 15.15-runtime-3 { Test prefix operation } {runtime} {
    compile_and_run [saveas T1515r3.java {
class T1515r3 {
    static byte s = 127; // static field
    byte i = 127; // instance field
    public static void main(String[] args) {
        new T1515r3();
    }
    T1515r3() {
        byte[] a = {127}; // array access
        byte l = 127; // local variable
        System.out.print(++s + " " + ++i + " " + ++a[0] + " " + ++l);
        System.out.print(" " + s + " " + i + " " + a[0] + " " + l);
    }
}
    }]
} {-128 -128 -128 -128 -128 -128 -128 -128}

tcltest::test 15.15-runtime-4 { Test prefix operation } {runtime} {
    compile_and_run [saveas T1515r4.java {
class T1515r4 {
    static byte s = -128; // static field
    byte i = -128; // instance field
    public static void main(String[] args) {
        new T1515r4();
    }
    T1515r4() {
        byte[] a = {-128}; // array access
        byte l = -128; // local variable
        System.out.print(--s + " " + --i + " " + --a[0] + " " + --l);
        System.out.print(" " + s + " " + i + " " + a[0] + " " + l);
    }
}
    }]
} {127 127 127 127 127 127 127 127}
