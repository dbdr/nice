tcltest::test 7.5.2-1 {import *; is not a valid import statement} {
    compile [saveas T7521.java \
{
import *;
public class T7521 {}
}]
} FAIL

tcltest::test 7.5.2-unnamed-1 { Imports cannot occur from the unnamed
        package } {
    compile [saveas T752u1a.java {
public class T752u1a {
    public static class Inner {}
}
}] [saveas p1/T752u1b.java {
package p1;
import T752u1a.*;
class T752u1b extends Inner {}
}]
} FAIL

tcltest::test 7.5.2-canonical-1 { A type-import-on-demand must use canonical
        package or type names } {
    compile [saveas p1/T752c1a.java {
package p1;
public class T752c1a {
    public static class Middle {
        public static class Inner {}
    }
}
}] [saveas p1/T752c1b.java {
package p1;
public class T752c1b extends T752c1a {}
}] [saveas T752c1c.java {
import p1.T752c1b.Middle.*; // not canonical
class T752c1c extends Inner {}
}]
} FAIL

tcltest::test 7.5.2-canonical-2 { Imports use the canonical name, even if a
        prefix name exists in the current package or prior import } {
    compile [saveas T752c2/Sub/T752c2a.java {
package T752c2.Sub;
public class T752c2a {}
}] [saveas T752c2/T752c2.java {
package T752c2;
import T752c2.Sub.*;
public class T752c2 extends T752c2a {}
}]
} PASS

# This test needs clarification from Sun...
tcltest::test 7.5.2-canonical-3 { Imports on demand work on a class which does
        not have member types, such that nothing is imported; contrast this
        with single-type imports which require the imported type exist } {
    compile [saveas p1/T752c3a.java {
package p1;
public class T752c3a {}
}] [saveas T752c3b.java {
import p1.T752c3a.*; // nothing imported
class T752c3b {}
}]
} PASS

tcltest::test 7.5.2-canonical-4 { Imports use the canonical name, hence local
        and anonymous classes (which do not have a canonical name) cannot
        be imported } {
    compile [saveas p1/T752c4a.java {
package p1;
public class T752c4a {
    static Object o = new Object() {};
}
}] [saveas T752c4b.java {
import p1.*;
class T752c4b {
    // Compilers are not required to use T752c4a$1 as the anonymous class
    // name, but it is a common choice
    Object o = new T752c4a$1();
}
}]
} FAIL

tcltest::test 7.5.2-accessible-1 { It is an error for an import statement to
        mention an inaccessible type or package } {
    compile [saveas T752a1.java {
import non_existant_package.*;
class T752a1 {}
}]
} FAIL

tcltest::test 7.5.2-accessible-2 { It is an error for an import statement to
        mention an inaccessible type or package } {
    compile [saveas p1/T752a2a.java {
package p1;
class T752a2a {
    public static class Inner {}
}
}] [saveas T752a2b.java {
import p1.T752a2a.*;
class T752a2b extends Inner {}
}]
} FAIL

tcltest::test 7.5.2-accessible-3 { It is an error for an import statement to
        mention an inaccessible type or package } {
    compile [saveas p1/T752a3a.java {
package p1;
class T752a3a {
    interface Inner {}
}
}] [saveas p1/T752a3b.java {
package p1;
import p1.T752a3a.*;
class T752a3b implements Inner {}
}]
} PASS

tcltest::test 7.5.2-accessible-4 { Only the accessible types are imported
        by import-on-demand } {
    compile [saveas p1/T752a4a.java {
package p1;
class T752a4a {}
}] [saveas p2/T752a4a.java {
package p2;
public class T752a4a {}
}] [saveas T752a4b.java {
import p1.*;
import p2.*;
class T752a4b extends T752a4a {}
}]
} PASS

tcltest::test 7.5.2-accessible-5 { Only the accessible types are imported
        by import-on-demand } {
    compile [saveas p1/T752a5a.java {
package p1;
import p1.T752a5a.*; // does not import Inner
class T752a5a {
    private class Inner {}
}
class T752a5b extends Inner {}
}]
} FAIL

tcltest::test 7.5.2-duplicate-1 { It is legal to have duplicate imports } {
    compile [saveas p1/T752d1a.java {
package p1;
public class T752d1a {}
}] [saveas T752d1b.java {
import p1.*;
import p1.*;
class T752d1b extends T752d1a {}
}]
} PASS

tcltest::test 7.5.2-duplicate-2 { It is legal to import the current package } {
    compile [saveas p1/T752d2.java {
package p1;
import p1.*;
class T752d2 {}
}]
} PASS

tcltest::test 7.5.2-duplicate-3 { It is legal to import a type declared in
        the same compilation unit } {
    compile [saveas p1/T752d3a.java {
package p1;
import p1.T752d3a.*;
class T752d3a {
    static class Inner {}
}
class T752d3b extends Inner {}
}]
} PASS

tcltest::test 7.5.2-duplicate-4 { It is legal to import the same simple name
        on demand from multiple packages, if no ambiguous reference is made
        to that simple name } {
    compile [saveas p1/T752d4a.java {
package p1;
public class T752d4a {}
}] [saveas p2/T752d4a.java {
package p2;
public class T752d4a {}
}] [saveas T752d4b.java {
import p1.*;
import p2.*;
class T752d4b {}
}]
} PASS

tcltest::test 7.5.2-inherited-1 { Type import on demand does not include
        inherited member types, just declared ones } {
    compile [saveas p1/T752i1a.java {
package p1;
import p1.T752i1b.*; // does not import T752i1a.Inner
class T752i1a {
    class Inner {}
}
class T752i1b extends T752i1a {
    class Extra {}
}
class T752i1c extends Inner {}
}]
} FAIL
