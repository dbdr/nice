tcltest::test 7.5.4-example-1 { Example in the JLS: imported names are not
        in scope in later imports } {
    compile [saveas Vector/Mosquito.java {
package Vector;
public class Mosquito {}
}] [saveas T754e1.java {
import java.util.Vector;
import Vector.Mosquito;
class T754e1 {
    public static void main(String[] args) {
        System.out.println(new Vector().getClass());
        System.out.println(new Mosquito().getClass());
    }
}
}]
} PASS
