tcltest::test 7.5.1-1 { Doing an import of a class that does not exist
        should fail} {
    compile [saveas T7511.java {
import foo.a_class_that_does_not_exist;
public class T7511 {}
}]
} FAIL

tcltest::test 7.5.1-2 { Cannot import from the unnamed package } {
    compile [saveas ClassInDefaultPackage.java {
public class T7512a {}
}] [saveas foo/T7512b.java {
package foo;
import T7512a;
public class T7512b extends T7512a {}
}]
} FAIL

tcltest::test 7.5.1-3 { Importing a non-public class should fail} {
    compile [saveas foo/T7513a.java {
package foo;
class T7513a {}
}] [saveas T7513b.java {
import foo.T7513a;
class T7513b extends T7513a {}
}]
} FAIL

tcltest::test 7.5.1-4 { Cannot import from the unnamed package } {
    compile [saveas ClassInDefaultPackage.java {
public class T7514a {
    public static class Inner {}
}
}] [saveas foo/T7514b.java {
package foo;
import T7514a.Inner;
public class T7514b extends Inner {}
}]
} FAIL

tcltest::test 7.5.1-canonical-1 { A single-type import must give the
        canonical name of a class } {
    compile [saveas p1/T751c1a.java {
package p1;
public class T751c1a {
    public static class Inner {}
}
}] [saveas p1/T751c1b.java {
package p1;
public class T751c1b extends T751c1a {}
}] [saveas T751c1c.java {
import p1.T751c1b.Inner;
class T751c1c extends Inner {}
}]
} FAIL

tcltest::test 7.5.1-canonical-2 { Imports use the canonical name, even if a
        prefix name exists in the current package or prior import } {
    compile [saveas T751c2/Sub/T751c2a.java {
package T751c2.Sub;
public class T751c2a {}
}] [saveas T751c2/T751c2.java {
package T751c2;
import T751c2.Sub.T751c2a;
public class T751c2 extends T751c2a {}
}]
} PASS

tcltest::test 7.5.1-accessible-1 { A single-type import must name a public
        type unless the type exists in the same package } {
    compile [saveas p1/T751a1a.java {
package p1;
class T751a1a {
    interface Inner {}
}
}] [saveas p1/T751a1b.java {
package p1;
import p1.T751a1a.Inner;
class T751a1b implements Inner {}
}]
} PASS

tcltest::test 7.5.1-accessible-2 { A single-type import must name a public
        type unless the type exists in the same package } {
    compile [saveas p1/T751a2a.java {
package p1;
class T751a2a {
    private interface Inner {}
}
}] [saveas T751a2b.java {
import p1.T751a2a.Inner;
class T751a2b implements Inner {}
}]
} FAIL

tcltest::test 7.5.1-shadow-1 { A single-type import shadows an otherwise
        accessible top-level type in another compilation unit } {
    compile [saveas p1/T751s1a.java {
package p1;
public class T751s1a {}
}] [saveas p2/T751s1a.java {
package p2;
interface T751s1a {}
}] [saveas p2/T751s1b.java {
package p2;
import p1.T751s1a; // now T751s1a refers to a class, not an interface
class T751s1b extends T751s1a {}
}]
} PASS

tcltest::test 7.5.1-shadow-2 { A single-type import shadows an otherwise
        accessible top-level type in another compilation unit } {
    compile [saveas p1/T751s2a.java {
package p1;
public class T751s2a {}
}] [saveas p2/T751s2a.java {
package p2;
interface T751s2a {}
}] [saveas p3/T751s2b.java {
package p3;
import p2.*;
import p1.T751s2a; // p2.T751s2a is shadowed by p1.T751s2a
class T751s2b extends T751s2a {}
}]
} PASS

tcltest::test 7.5.1-duplicate-1 { Importing the same class twice is legal } {
    compile [saveas p1/T751d1a.java {
package p1;
public class T751d1a {}
}] [saveas T751d1b.java {
import p1.T751d1a;
import p1.T751d1a;
class T751d1b extends T751d1a {}
}]
} PASS

tcltest::test 7.5.1-duplicate-2 { Importing two classes of the same name is
        illegal } {
    compile [saveas p1/T751d2a.java {
package p1;
public class T751d2a {}
}] [saveas p2/T751d2a.java {
package p2;
public class T751d2a {}
}] [saveas T751d2b.java {
import p1.T751d2a;
import p2.T751d2a;
class T751d2b extends T751d2a {}
}]
} FAIL

tcltest::test 7.5.1-duplicate-3 { It is an error to single-type import a
        distinct type with the same name as one in the compilation unit } {
    compile [saveas p1/T751d3a.java {
package p1;
public class T751d3a {}
}] [saveas T751d3a.java {
import p1.T751d3a;
class T751d3a {}
}]
} FAIL

tcltest::test 7.5.1-duplicate-4 { It is legal to import the class being
        declared in a compilation unit } {
    ok_pass_or_warn [compile [saveas p1/T751d4a.java {
package p1;
import p1.T751d4a;
public class T751d4a {}
}]]
} OK

tcltest::test 7.5.1-subpackage-1 { Single-type import cannot import
        subpackages } {
    compile [saveas T751s1 {
import java.util;
class T751s1 extends util.Random {}
}]
} FAIL
