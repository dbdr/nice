tcltest::test 6.4.4-1 { An array object should be able to access methods
        defined in the java.lang.Object class } {
    compile [saveas ArrayIsObject.java \
{
public class ArrayIsObject {
    public static void main(String[] args) {
        int len = args.length;
        System.out.println(len);
        Class c = args.getClass();
        System.out.println(c);
        String s = args.toString();
        System.out.println(s);
    }
}
}]
} PASS

tcltest::test 6.4.4-2 { the length member of an array is an int } {
    compile [saveas ArrayLengthIsInt.java \
{
public class ArrayLengthIsInt {
    void foo() {
        int[] foo = new int[3];
        int val = foo.length;
    }
}
}]
} PASS

tcltest::test 6.4.4-3 { compile error on attempt to change array length member } {
    compile [saveas ArrayLengthIsFinal.java \
{
public class ArrayLengthIsFinal {
    void foo() {
        int[] foo = new int[3];
        foo.length = 4;
    }
}
}]
} FAIL
