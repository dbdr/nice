tcltest::test 7.5.3-automatic-1 { All classes have an implicit import
        java.lang.*; } {
    compile [saveas T753a1.java {
class T753a1 extends Object {
    Integer i;
}
}]
} PASS

tcltest::test 7.5.3-automatic-2 { It is not an error to explicitly import
        from java.lang } {
    compile [saveas T753a2.java {
import java.lang.*;
import java.lang.Object;
import java.lang.Integer;
class T753a2 extends Object {
    Integer i;
}
}]
} PASS
