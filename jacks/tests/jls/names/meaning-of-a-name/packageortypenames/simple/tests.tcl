tcltest::test 6.5.4.1-simple-1 { If a simple package-or-type name is in the
        scope of a type, it is reclassified as a type name } {
    compile [saveas p1/T6541s1a.java {
package p1;
public class T6541s1a {}
}] [saveas T6541s1b.java {
class T6541s1b {
    static class p1 {
        interface T6541s1a {}
    }
    // p1 is a type, not a package, so p1.T6541s1a is an interface, not a class
    class Sub implements p1.T6541s1a {}
}
}]
} PASS

tcltest::test 6.5.4.1-simple-2 { If a simple package-or-type name is in the
        scope of a type, it is reclassified as a type name } {
    compile [saveas p1/T6541s2a.java {
package p1;
public class T6541s2a {}
}] [saveas p2/T6541s2b.java {
package p2;
public class T6541s2b {
    public static class p1 {
        public interface T6541s2a {}
    }
}
}] [saveas T6541s2c.java {
class T6541s1c extends p2.T6541s2b {
    // p1 is an inherited type, not a package, so p1.T6541s2a is an
    class Sub implements p1.T6541s2a {} // interface, not a class
}
}]
} PASS

tcltest::test 6.5.4.1-simple-3 { If a simple package-or-type name is in the
        scope of a type, it is reclassified as a type name } {
    compile [saveas T6541s3.java {
// no types are in scope during an import statement, and there is no
import T6541s3.*; // package by this name
class T6541s3 {
    class Inner {}
}
}]
} FAIL

tcltest::test 6.5.4.1-simple-4 { If a simple package-or-type name is not in
        the scope of a type, it is reclassified as a package name } {
    compile [saveas p1/T6541s4a.java {
package p1;
class T6541s4a {}
class T6541s4b extends p1.T6541s4a {}
}]
} PASS

tcltest::test 6.5.4.1-simple-5 { If a simple package-or-type name is not in
        the scope of a type, it is reclassified as a package name; an error
        will eventually occur if the package is not observable } {
    compile [saveas T6541s5.java {
// badpackage is reclassified as a package, but it does not exist
class T6541s5 extends badpackage.Type {}
}]
} FAIL

tcltest::test 6.5.4.1-simple-6 { If a simple package-or-type name is not in
the scope of a type, it is reclassified as a package name } {
    compile [saveas p1/T6541s6a.java {
package p1;
public class T6541s6a {
    public static class p2 {
        public class T6541s6b {}
    }
}
}] [saveas p2/T6541s6b.java {
package p2;
public interface T6541s6b {}
}] [saveas T6541s6c.java {
// the inherited class p2 is not in scope until the body of class T6541s6c,
// so p2 refers to the package, and p2.T6541s6b is an interface, not a class
class T6541s6c extends p1.T6541s6a implements p2.T6541s6b {}
}]
} PASS

tcltest::test 6.5.4.1-simple-7 { If a simple package-or-type name is not in
the scope of a type, it is reclassified as a package name } {
    compile [saveas p1/T6541s7a.java {
package p1;
public interface T6541s7a {
    public class p2 {
        public interface T6541s7b {}
    }
}
}] [saveas p2/T6541s7b.java {
package p2;
public class T6541s7b {}
}] [saveas T6541s7c.java {
// the inherited interface p2 is not in scope until the body of class T6541s7c,
// so p2 refers to the package, and p2.T6541s7b is a class, not an interface
class T6541s7c extends p2.T6541s7b implements p1.T6541s7a {}
}]
} PASS
