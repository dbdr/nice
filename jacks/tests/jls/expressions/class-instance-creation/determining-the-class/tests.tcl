tcltest::test 15.9.1-unqualified-anonymous-1 { The ClassOrInterfaceType must
        be accessible } {
    empty_main T1591ua1 {
        new java.lang.Object(){};
        new T1591ua1(){};
    }
} PASS

tcltest::test 15.9.1-unqualified-anonymous-2 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua2_1.java {
package p1;
public class T1591ua2_1 {}
    }] [saveas p2/T1591ua2_2.java {
package p2;
import p1.*;
class T1591ua2_2 extends p1.T1591ua2_1 {
    Object o1 = new p1.T1591ua2_1(){};
    Object o2 = new T1591ua2_1(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-3 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua3_1.java {
package p1;
class T1591ua3_1 {}
    }] [saveas p2/T1591ua3_2.java {
package p2;
import p1.*;
class T1591ua3_2 extends p1.T1591ua3_1 {
    Object o1 = new p1.T1591ua3_1(){};
    Object o2 = new T1591ua3_1(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-4 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua4_1.java {
package p1;
public class T1591ua4_1 {
    public class Inner{}
}
    }] [saveas p2/T1591ua4_2.java {
package p2;
import p1.*;
class T1591ua4_2 extends p1.T1591ua4_1 {
    Object o1 = new p1.T1591ua4_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-5 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua5_1.java {
package p1;
public class T1591ua5_1 {
    protected class Inner{}
}
    }] [saveas p2/T1591ua5_2.java {
package p2;
import p1.*;
class T1591ua5_2 extends p1.T1591ua5_1 {
    Object o1 = new p1.T1591ua5_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-6 { The ClassOrInterfaceType must
        be accessible, the inner class is has package access so it
        is not accessible from another package } {
    compile [saveas p1/T1591ua6_1.java {
package p1;
public class T1591ua6_1 {
    class Inner{}
}
    }] [saveas p2/T1591ua6_2.java {
package p2;
import p1.*;
class T1591ua6_2 extends p1.T1591ua6_1 {
    Object o1 = new p1.T1591ua6_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-7 { The ClassOrInterfaceType must
        be accessible, this inner class is private } {
    compile [saveas p1/T1591ua7_1.java {
package p1;
public class T1591ua7_1 {
    private class Inner{}
}
    }] [saveas p2/T1591ua7_2.java {
package p2;
import p1.*;
class T1591ua7_2 extends p1.T1591ua7_1 {
    Object o1 = new p1.T1591ua7_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-8 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591ua8_1.java {
class T1591ua8_1 {
    protected class Inner{}
}
class T1591ua8_2 extends T1591ua8_1 {
    Object o1 = new T1591ua8_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-9 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591ua9_1.java {
class T1591ua9_1 {
    class Inner{}
}
class T1591ua9_2 extends T1591ua9_1 {
    Object o1 = new T1591ua9_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-10 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591ua10_1.java {
class T1591ua10_1 {
    private class Inner{}
}
class T1591ua10_2 extends T1591ua10_1 {
    Object o1 = new T1591ua10_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-11 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591ua11.java {
interface I1591ua11 {}
class T1591ua11 {
    Object o1 = new java.lang.Cloneable(){};
    Object o2 = new I1591ua11(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-12 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua12_1.java {
package p1;
public interface T1591ua12_1 {}
    }] [saveas p2/T1591ua12_2.java {
package p2;
import p1.*;
class T1591ua12_2 implements p1.T1591ua12_1 {
    Object o1 = new p1.T1591ua12_1(){};
    Object o2 = new T1591ua12_1(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-13 { The ClassOrInterfaceType must
        be accessible, this interface has package access } {
    compile [saveas p1/T1591ua13_1.java {
package p1;
interface T1591ua13_1 {}
    }] [saveas p2/T1591ua13_2.java {
package p2;
import p1.*;
class T1591ua13_2 implements p1.T1591ua13_1 {
    Object o1 = new p1.T1591ua13_1(){};
    Object o2 = new T1591ua13_1(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-14 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua14_1.java {
package p1;
public class T1591ua14_1 {
    public interface Inner{}
}
    }] [saveas p2/T1591ua14_2.java {
package p2;
import p1.*;
class T1591ua14_2 extends p1.T1591ua14_1 {
    Object o1 = new p1.T1591ua14_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-15 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua15_1.java {
package p1;
public class T1591ua15_1 {
    protected interface Inner{}
}
    }] [saveas p2/T1591ua15_2.java {
package p2;
import p1.*;
class T1591ua15_2 extends p1.T1591ua15_1 {
    Object o1 = new p1.T1591ua15_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-16 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua16_1.java {
package p1;
public class T1591ua16_1 {
    interface Inner{}
}
    }] [saveas p2/T1591ua16_2.java {
package p2;
import p1.*;
class T1591ua16_2 extends p1.T1591ua16_1 {
    Object o1 = new p1.T1591ua16_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-17 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591ua17_1.java {
package p1;
public class T1591ua17_1 {
    private interface Inner{}
}
    }] [saveas p2/T1591ua17_2.java {
package p2;
import p1.*;
class T1591ua17_2 extends p1.T1591ua17_1 {
    Object o1 = new p1.T1591ua17_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-18 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591ua18_1.java {
class T1591ua18_1 {
    protected interface Inner{}
}
class T1591ua18_2 extends T1591ua18_1 {
    Object o1 = new T1591ua18_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-19 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591ua19_1.java {
class T1591ua19_1 {
    interface Inner{}
}
class T1591ua19_2 extends T1591ua19_1 {
    Object o1 = new T1591ua19_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-anonymous-20 { The ClassOrInterfaceType must
        be accessible, this inner class has private access } {
    compile [saveas T1591ua20_1.java {
class T1591ua20_1 {
    private interface Inner{}
}
class T1591ua20_2 extends T1591ua20_1 {
    Object o1 = new T1591ua20_1.Inner(){};
    Object o2 = new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-21 { Anonymous classes cannot
        extend final class } {
    empty_class T1591ua21 {
        final class Inner{}
        Object o1 = new T1591ua21.Inner(){};
        Object o2 = new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-22 { Anonymous classes may
        extend abstract class } {
    empty_class T1591ua22 {
        abstract class Inner{}
        Object o1 = new T1591ua22.Inner(){};
        Object o2 = new Inner(){};
    }
} PASS

tcltest::test 15.9.1-unqualified-anonymous-23 { The name may not be ambiguous,
        in this case Inner is ambiguous even though one is an inner class and
        cannot be constructed without an enclosing instance } {
    compile [saveas T1591ua23_1.java {
class T1591ua23_1 {
    class Inner{}
}
interface T1591ua23_2 {
    class Inner{}
}
class T1591ua23_3 extends T1591ua23_1 implements T1591ua23_2 {
    static Object o = new T1591ua23_3.Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-anonymous-24 { The name may not be ambiguous,
        accessibility means that only one Inner is inherited } {
    compile [saveas p1/T1591ua24_1.java {
package p1;
public class T1591ua24_1 {
    static class Inner{}
}
    }] [saveas p2/T1591ua24_2.java {
package p2;
import p1.*;
interface T1591ua24_2 {
    class Inner{}
}
class T1591ua24_3 extends T1591ua24_1 implements T1591ua24_2 {
    static Object o = new T1591ua24_3.Inner(){};
}
    }]
} PASS

# qualified anonymous creations

tcltest::test 15.9.1-qualified-anonymous-1 { The simple name must be an
        accessible inner class of the qualifier } {
    empty_class T1591qa1 {
        class Inner{}
        Object o = new T1591qa1().new Inner(){};
    }
} PASS

tcltest::test 15.9.1-qualified-anonymous-2 { Anonymous classes cannot
        extend final class } {
    empty_class T1591qa2 {
        final class Inner{}
        Object o = new T1591qa2().new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-3 { Anonymous classes may
        extend abstract class } {
    empty_class T1591qa3 {
        abstract class Inner{}
        Object o = new T1591qa3().new Inner(){};
    }
} PASS

tcltest::test 15.9.1-qualified-anonymous-4 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qa4_1.java {
package p1;
public class T1591qa4_1 {
    public class Inner{}
}
    }] [saveas p2/T1591qa4_2.java {
package p2;
import p1.*;
class T1591qa4_2 extends p1.T1591qa4_1 {
    Object o = new p1.T1591qa4_1().new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-qualified-anonymous-5 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qa5_1.java {
package p1;
public class T1591qa5_1 {
    protected class Inner{}
}
    }] [saveas p2/T1591qa5_2.java {
package p2;
import p1.*;
class T1591qa5_2 extends p1.T1591qa5_1 {
    Object o = new p1.T1591qa5_1().new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-qualified-anonymous-6 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qa6_1.java {
package p1;
public class T1591qa6_1 {
    class Inner{}
}
    }] [saveas p2/T1591qa6_2.java {
package p2;
import p1.*;
class T1591qa6_2 extends p1.T1591qa6_1 {
    Object o = new p1.T1591qa6_1().new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-anonymous-7 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qa7_1.java {
package p1;
public class T1591qa7_1 {
    private class Inner{}
}
    }] [saveas p2/T1591qa7_2.java {
package p2;
import p1.*;
class T1591qa7_2 extends p1.T1591qa7_1 {
    Object o = new p1.T1591qa7_1().new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-anonymous-8 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas T1591qa8_1.java {
class T1591qa8_1 {
    protected class Inner{}
}
class T1591qa8_2 extends T1591qa8_1 {
    Object o = new T1591qa8_1().new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-qualified-anonymous-9 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas T1591qa9_1.java {
class T1591qa9_1 {
    class Inner{}
}
class T1591qa9_2 extends T1591qa9_1 {
    Object o = new T1591qa9_1().new Inner(){};
}
    }]
} PASS

tcltest::test 15.9.1-qualified-anonymous-10 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas T1591qa10_1.java {
class T1591qa10_1 {
    private class Inner{}
}
class T1591qa10_2 extends T1591qa10_1 {
    Object o = new T1591qa10_1().new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-anonymous-11 { The qualified name must be simple } {
    empty_class T1591qa11 {
        class Inner{}
        Object o = new T1591qa11().new T1591qa11.Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-12 { The qualified name must be an
        inner class } {
    empty_class T1591qa12 {
        static class Inner{}
        Object o = new T1591qa12().new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-13 { The qualified name must be an
        inner class } {
    empty_class T1591qa13 {
        interface Inner{}
        Object o = new T1591qa13().new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-14 { The qualified name must be an
        inner class } {
    empty_class T1591qa14 {
        Object o = new T1591qa14().new Object(){};
    }
} FAIL

# tests 15, 16, and 17 are known javac 1.3 bugs
tcltest::test 15.9.1-qualified-anonymous-15 { The anonymous class extends
        the inner class of the type of the qualifier } {
    empty_class T1591qa15 {
        class Inner{}
        Object o = new T1591qa15(){}.new Inner(){};
    }
} PASS

tcltest::test 15.9.1-qualified-anonymous-16 { The anonymous class extends
        the inner class of the type of the qualifier } {
    empty_class T1591qa16 {
        Object o = new T1591qa16(){
            class Inner{}
        }.new Inner(){};
    }
} PASS

tcltest::test 15.9.1-qualified-anonymous-17 { The anonymous class extends
        the inner class of the type of the qualifier } {
    empty_class T1591qa17 {
        class Inner {
            static final int foo = 1;
        }
        Object o = new T1591qa17(){
            class Inner {
                static final int foo = 2;
            }
        }.new Inner(){
            {
                switch (1) {
                    case 0:
                    case ((foo == 2) ? 1 : 0):
                }
            }
        };
    }
} PASS

tcltest::test 15.9.1-qualified-anonymous-18 { The name may not be ambiguous,
        even though only one is an inner class since member classes of
        interfaces are static and do not have enclosing instance } {
    compile [saveas T1591qa18_1.java {
class T1591qa18_1 {
    class Inner{}
}
interface T1591qa18_2 {
    class Inner{}
}
class T1591qa18_3 extends T1591qa18_1 implements T1591qa18_2 {
    Object o = new T1591qa18_3().new Inner(){};
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-anonymous-19 { The qualifier must be a reference } {
    empty_class T1591qa19 {
        class Inner{}
        int foo() { return 1; }
        Object o = foo().new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-20 { The qualifier must be a reference } {
    empty_class T1591qa20 {
        class Inner{}
        Object o = null.new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-21 { The qualifier must be a reference } {
    empty_class T1591qa21 {
        class Inner{}
        Object o = System.out.println().new Inner(){};
    }
} FAIL

tcltest::test 15.9.1-qualified-anonymous-22 { The qualified name must be an
        inner class of the qualifier } {
    compile [saveas T1591qa22_1.java {
class T1591qa22_1 {}
class T1591qa22_2 extends T1591qa22_1 {
    class Inner{}
    Object o = new T1591qa22_1().new Inner(){};
}
    }]
} FAIL


# concrete unqualified creation

tcltest::test 15.9.1-unqualified-concrete-1 { The ClassOrInterfaceType must
        be accessible } {
    empty_main T1591uc1 {
        new java.lang.Object();
        new T1591uc1();
    }
} PASS

tcltest::test 15.9.1-unqualified-concrete-2 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591uc2_1.java {
package p1;
public class T1591uc2_1 {}
    }] [saveas p2/T1591uc2_2.java {
package p2;
import p1.*;
class T1591uc2_2 extends p1.T1591uc2_1 {
    Object o1 = new p1.T1591uc2_1();
    Object o2 = new T1591uc2_1();
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-concrete-3 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591uc3_1.java {
package p1;
class T1591uc3_1 {}
    }] [saveas p2/T1591uc3_2.java {
package p2;
import p1.*;
class T1591uc3_2 extends p1.T1591uc3_1 {
    Object o1 = new p1.T1591uc3_1();
    Object o2 = new T1591uc3_1();
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-concrete-4 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591uc4_1.java {
package p1;
public class T1591uc4_1 {
    public class Inner{}
}
    }] [saveas p2/T1591uc4_2.java {
package p2;
import p1.*;
class T1591uc4_2 extends p1.T1591uc4_1 {
    Object o1 = new p1.T1591uc4_1.Inner();
    Object o2 = new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-concrete-5 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591uc5_1.java {
package p1;
public class T1591uc5_1 {
    protected class Inner{
        public Inner(){}
    }
}
    }] [saveas p2/T1591uc5_2.java {
package p2;
import p1.*;
class T1591uc5_2 extends p1.T1591uc5_1 {
    Object o1 = new p1.T1591uc5_1.Inner();
    Object o2 = new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-concrete-6 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591uc6_1.java {
package p1;
public class T1591uc6_1 {
    class Inner{}
}
    }] [saveas p2/T1591uc6_2.java {
package p2;
import p1.*;
class T1591uc6_2 extends p1.T1591uc6_1 {
    Object o1 = new p1.T1591uc6_1.Inner();
    Object o2 = new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-concrete-7 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas p1/T1591uc7_1.java {
package p1;
public class T1591uc7_1 {
    private class Inner{}
}
    }] [saveas p2/T1591uc7_2.java {
package p2;
import p1.*;
class T1591uc7_2 extends p1.T1591uc7_1 {
    Object o1 = new p1.T1591uc7_1.Inner();
    Object o2 = new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-concrete-8 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591uc8_1.java {
class T1591uc8_1 {
    protected class Inner{}
}
class T1591uc8_2 extends T1591uc8_1 {
    Object o1 = new T1591uc8_1.Inner();
    Object o2 = new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-concrete-9 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591uc9_1.java {
class T1591uc9_1 {
    class Inner{}
}
class T1591uc9_2 extends T1591uc9_1 {
    Object o1 = new T1591uc9_1.Inner();
    Object o2 = new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-unqualified-concrete-10 { The ClassOrInterfaceType must
        be accessible } {
    compile [saveas T1591uc10_1.java {
class T1591uc10_1 {
    private class Inner{}
}
class T1591uc10_2 extends T1591uc10_1 {
    Object o1 = new T1591uc10_1.Inner();
    Object o2 = new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-concrete-11 { Interface instances cannot be
        created } {
    compile [saveas T1591uc11.java {
interface I1591uc11 {}
class T1591uc11 {
    Object o = new I1591uc11();
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-concrete-12 { Concrete classes may be final } {
    empty_class T1591uc12 {
        final class Inner{}
        Object o1 = new T1591uc12.Inner();
        Object o2 = new Inner();
    }
} PASS

tcltest::test 15.9.1-unqualified-concrete-13 { Concrete classes may not be
        abstract } {
    empty_class T1591uc13 {
        abstract class Inner{}
        Object o1 = new T1591uc13.Inner();
        Object o2 = new Inner();
    }
} FAIL

tcltest::test 15.9.1-unqualified-concrete-14 { The name may not be ambiguous,
        Inner is ambiguous, even though one is an inner class and
        cannot be constructed without an enclosing instance } {
    compile [saveas T1591uc14_1.java {
class T1591uc14_1 {
    class Inner{}
}
interface T1591uc14_2 {
    class Inner{}
}
class T1591uc14_3 extends T1591uc14_1 implements T1591uc14_2 {
    static Object o = new T1591uc14_3.Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-unqualified-concrete-15 { The name may not be ambiguous,
        accessibility means that only one Inner is inherited } {
    compile [saveas p1/T1591uc15_1.java {
package p1;
public class T1591uc15_1 {
    static class Inner{}
}
    }] [saveas p2/T1591uc15_2.java {
package p2;
import p1.*;
interface T1591uc15_2 {
    class Inner{}
}
class T1591uc15_3 extends T1591uc15_1 implements T1591uc15_2 {
    static Object o = new T1591uc15_3.Inner();
}
    }]
} PASS

# qualified concrete creations

tcltest::test 15.9.1-qualified-concrete-1 { The simple name must be an
        accessible inner class of the qualifier } {
    empty_class T1591qc1 {
        class Inner{}
        Object o = new T1591qc1().new Inner();
    }
} PASS

tcltest::test 15.9.1-qualified-concrete-2 { Concrete classes may be final } {
    empty_class T1591qc2 {
        final class Inner{}
        Object o = new T1591qc2().new Inner();
    }
} PASS

tcltest::test 15.9.1-qualified-concrete-3 { Concrete classes may not be
        abstract } {
    empty_class T1591qc3 {
        abstract class Inner{}
        Object o = new T1591qc3().new Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-4 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qc4_1.java {
package p1;
public class T1591qc4_1 {
    public class Inner{}
}
    }] [saveas p2/T1591qc4_2.java {
package p2;
import p1.*;
class T1591qc4_2 extends p1.T1591qc4_1 {
    Object o = new p1.T1591qc4_1().new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-qualified-concrete-5 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qc5_1.java {
package p1;
public class T1591qc5_1 {
    protected class Inner{
        public Inner(){}
    }
}
    }] [saveas p2/T1591qc5_2.java {
package p2;
import p1.*;
class T1591qc5_2 extends p1.T1591qc5_1 {
    Object o = new p1.T1591qc5_1().new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-qualified-concrete-6 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qc6_1.java {
package p1;
public class T1591qc6_1 {
    class Inner{}
}
    }] [saveas p2/T1591qc6_2.java {
package p2;
import p1.*;
class T1591qc6_2 extends p1.T1591qc6_1 {
    Object o = new p1.T1591qc6_1().new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-concrete-7 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas p1/T1591qc7_1.java {
package p1;
public class T1591qc7_1 {
    private class Inner{}
}
    }] [saveas p2/T1591qc7_2.java {
package p2;
import p1.*;
class T1591qc7_2 extends p1.T1591qc7_1 {
    Object o = new p1.T1591qc7_1().new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-concrete-8 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas T1591qc8_1.java {
class T1591qc8_1 {
    protected class Inner{}
}
class T1591qc8_2 extends T1591qc8_1 {
    Object o = new T1591qc8_1().new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-qualified-concrete-9 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas T1591qc9_1.java {
class T1591qc9_1 {
    class Inner{}
}
class T1591qc9_2 extends T1591qc9_1 {
    Object o = new T1591qc9_1().new Inner();
}
    }]
} PASS

tcltest::test 15.9.1-qualified-concrete-10 { The simple name must be an
        accessible inner class of the qualifier } {
    compile [saveas T1591qc10_1.java {
class T1591qc10_1 {
    private class Inner{}
}
class T1591qc10_2 extends T1591qc10_1 {
    Object o = new T1591qc10_1().new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-concrete-11 { The qualified name must be simple } {
    empty_class T1591qc11 {
        class Inner{}
        Object o = new T1591qc11().new T1591qc11.Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-12 { The qualified name must be an
        inner class } {
    empty_class T1591qc12 {
        static class Inner{}
        Object o = new T1591qc12().new Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-13 { The qualified name must be an
        inner class } {
    empty_class T1591qc13 {
        interface Inner{}
        Object o = new T1591qc13().new Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-14 { The qualified name must be an
        inner class } {
    empty_class T1591qc14 {
        Object o = new T1591qc14().new Object();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-15 { The concrete class extends
        the inner class of the type of the qualifier } {
    empty_class T1591qc15 {
        Object o = new T1591qc15(){
            class Inner{}
        }.new Inner();
    }
} PASS

tcltest::test 15.9.1-qualified-concrete-16 { The name may not be ambiguous,
         Inner is ambiguous, even though only one is an inner class member
         classes of interfaces are static, and do not have enclosing instance } {
    compile [saveas T1591qc16_1.java {
class T1591qc16_1 {
    class Inner{}
}
interface T1591qc16_2 {
    class Inner{}
}
class T1591qc16_3 extends T1591qc16_1 implements T1591qc16_2 {
    Object o = new T1591qc16_3().new Inner();
}
    }]
} FAIL

tcltest::test 15.9.1-qualified-concrete-17 { The qualifier must be a reference } {
    empty_class T1591qc17 {
        class Inner{}
        int foo() { return 1; }
        Object o = foo().new Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-18 { The qualifier must be a reference } {
    empty_class T1591qc18 {
        class Inner{}
        Object o = null.new Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-19 { The qualifier must be a reference } {
    empty_class T1591qc19 {
        class Inner{}
        Object o = System.out.println().new Inner();
    }
} FAIL

tcltest::test 15.9.1-qualified-concrete-20 { The qualified name must be an
        inner class of the qualifier } {
    compile [saveas T1591qa20_1.java {
class T1591qa20_1 {}
class T1591qa20_2 extends T1591qa20_1 {
    class Inner{}
    Object o = new T1591qa20_1().new Inner();
}
    }]
} FAIL

