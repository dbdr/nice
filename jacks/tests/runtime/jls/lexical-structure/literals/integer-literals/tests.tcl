tcltest::test 3.10.1-runtime-endianness-1 { Detect if endianness
        is wrong } {runtime} {
    compile_and_run [saveas T3101re1.java {
class T3101re1 {
    public static void main(String[] args) {
        long l1 = 0x1111111122222222L;
        long l2 = 0x2222222211111111L;
        if (l1 < l2) {
            System.out.print("OK");
        } else {
            System.out.print("Wrong endianness");
        }
    }
}
    }]
} OK
