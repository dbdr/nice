tcltest::test 15.12.2.1-jls-example-1 { example code,
        should fail since two(int) is private } {

    saveas T151221je1.java {
class Doubler {
    static int two() { return two(1); }
    private static int two(int i) { return 2*i; }
}
class Test extends Doubler {    
    public static long two(long j) {return j+j; }
    public static void main(String[] args) {
        System.out.println(two(3));
        System.out.println(Doubler.two(3)); // compile-time error
    }
}
}

   compile T151221je1.java
} FAIL


tcltest::test 15.12.2.1-jls-example-2 { example code,
         The type of the literal 37 is int, and int
         cannot be converted to byte by method invocation
         conversion } {

    saveas T151221je2.java {
class ColoredPoint {
    int x, y;
    byte color;
    void setColor(byte color) { this.color = color; }
}
class Test {
    public static void main(String[] args) {
        ColoredPoint cp = new ColoredPoint();
        byte color = 37;
        cp.setColor(color);
        cp.setColor(37);  // compile-time error
    }
}
}
    compile T151221je2.java
} FAIL






tcltest::test 15.12.2.1-accessibility-constructor-1 {
        A constructor must be applicable and accessible,
        in this case the constructor is private so it
        is not accessible even though it is applicable } {
    saveas T151221ac1.java {
class T151221ac1 {
    public T151221ac1(String s) {}
    private T151221ac1(Integer i) {}
}
class T151221ac1_Test {
    Object o = new T151221ac1(null);
}
}

    compile T151221ac1.java
} PASS

tcltest::test 15.12.2.1-accessibility-constructor-2 {
        A private constructor is not accessible } {

    saveas T151221ac2.java {
class T151221ac2 {
    public T151221ac2(String x, String y) {}
    private T151221ac2(String x, char[] y) {}
}
class T151221ac2_Test {
    Object o = new T151221ac2("hi", null);
}
}

    compile T151221ac2.java
} PASS

tcltest::test 15.12.2.1-accessibility-method-1 {
        A method must be applicable and accessible,
        in this case the method is private so it
        is not accessible even though it is applicable } {

    saveas T151221am1.java {
class T151221am1 {
    public int foo(String s) { return 0; }
    private int foo(Integer i) { return 0; }
}
class T151221am1_Test {
    int i = new T151221am1().foo(null);
}
}

    compile T151221am1.java
} PASS


