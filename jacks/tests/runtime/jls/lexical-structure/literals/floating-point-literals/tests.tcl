tcltest::test 3.10.2-runtime-endianness-1 { Detect if endianness
        is backward } {runtime} {
    compile_and_run [saveas T3102re1.java {
class T3102re1 {
    public static void main(String[] args) {
        double d1 = Double.longBitsToDouble(0x3ff8000000000000L);
        double d2 = Double.longBitsToDouble(0x000000003ff80000L);
        // compare d1 and d2, to make sure endianness of longs
        // was not wrong, either
        if (d1 == 1.5 && d1 > d2) {
            System.out.print("OK");
        } else if (d2 == 1.5 || d1 < d2) {
            System.out.print("Wrong endianness");
        } else {
            System.out.print("Unexpected result");
        }
    }
}
    }]
} OK
