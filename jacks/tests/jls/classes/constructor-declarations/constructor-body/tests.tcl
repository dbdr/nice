# See section 8.8.5.1 for Explicit Constructor Invocations examples

tcltest::test 8.8.5-return-statement-1 { A return statement may be
        used in the body of a constructor if it does not include
        an expression } {
    empty_class T885rs1 "T885rs1() { return; }"
} PASS

tcltest::test 8.8.5-return-statement-2 { A return statement may be
        used in the body of a constructor if it does not include
        an expression } {
    empty_class T885rs2 "T885rs2() { return 1; }"
} FAIL


tcltest::test 8.8.5-example-1 { JLS example } {
    saveas Point.java {
class Point {
           int x, y;
           Point(int x, int y) { this.x = x; this.y = y; }
   }
   class ColoredPoint extends Point {
           static final int WHITE = 0, BLACK = 1;
           int color;
           ColoredPoint(int x, int y) {
                   this(x, y, WHITE);
           }
           ColoredPoint(int x, int y, int color) {
                   super(x, y);
                   this.color = color;
           }
   }
}

    compile Point.java
} PASS

