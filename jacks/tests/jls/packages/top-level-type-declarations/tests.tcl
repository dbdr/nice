tcltest::test 7.6-syntax-1 { ; is a valid top-level TypeDeclaration } {
    compile [saveas T76syntax1.java {;}]
} PASS

tcltest::test 7.6-syntax-2 { top-level TypeDeclaration } {
    compile [saveas T76syntax2.java {
class T76syntax2 {}
}]
} PASS

tcltest::test 7.6-syntax-3 { top-level TypeDeclaration } {
    compile [saveas T76syntax3.java {
interface T76syntax3 {}
}]
} PASS


tcltest::test 7.6-scope-1 { TypeDeclaration within a package must
        have a unique Identifier } {
    compile [saveas T76scope1.java {
class Point {}
interface Point {}
}]
} FAIL

tcltest::test 7.6-scope-2 { TypeDeclaration cannot conflict with
        single-type-imports } {
    compile [saveas T76scope2.java {
import java.util.Vector;
class Vector {}
    }]
} FAIL

tcltest::test 7.6-scope-3 {  TypeDeclaration may shadow types
        in general imports } {
    compile [saveas T76scope3.java {
import java.util.*;
class Vector {}
    }]
} PASS

tcltest::test 7.6-scope-4 { TypeDeclaration may have forward
        references within a compilation unit } {
    compile [saveas T76scope4.java {
class first {
    second a;
}
class second {
    first b;
}
    }]
} PASS


tcltest::test 7.6-name-1 { Test fully qualified toplevel type name } {
    compile [saveas T76name1pkg/pclass.java {
package T76name1pkg;
public class pclass {}
    }] [saveas T76name1.java {
class T76name1 extends T76name1pkg.pclass {}
}]
} PASS

tcltest::test 7.6-name-2 { Test fully qualified toplevel type name } {
    compile [saveas T76name2pkg/pinterface.java {
package T76name2pkg;
public interface pinterface {}
    }] [saveas T76name2.java {
interface T76name2 extends T76name2pkg.pinterface {}
}]
} PASS

tcltest::test 7.6-name-3 { TypeDeclaration need not be related
        to the file name if class is not public } {
    compile [saveas 76name3.java {
class Unrelated {}
}]
} PASS



# optional

tcltest::test 7.6-optional-restrictions-1  { Multiple classes may reside
        within a compilation unit, if secondary classes are not public and
        not referenced in other files } {
    saveas one.java {
class one {
    three three;
}
class three extends two {}
}

    saveas two.java {
class two extends one {}
}

    compile two.java
} PASS

tcltest::test 7.6-modifiers-1 { Top-level types may not be static } {
    compile [saveas T76modifiers1.java {static class T76modifiers1 {}}]
} FAIL

tcltest::test 7.6-modifiers-2 { Top-level types may not be protected } {
    compile [saveas T76modifiers2.java {protected class T76modifiers2 {}}]
} FAIL

tcltest::test 7.6-modifiers-3 { Top-level types may not be private } {
    compile [saveas T76modifiers3.java {private class T76modifiers3 {}}]
} FAIL

tcltest::test 7.6-unnamed-scope-1 { Top-level types are not in scope (not available
        by simple name) in import statements, so nothing can be imported
        from the unnamed package } {
    saveas T76us1.java {public class T76us1 {}}
    saveas p/T76us1_1.java {
package p;
import T76us1;
class T76us1_1 {
    T76us1 t;
}
    }
    compile T76us1.java p/T76us1_1.java
} FAIL

tcltest::test 7.6-unnamed-scope-2 { Top-level types are not in scope (not available
        by simple name) in import statements, so nothing can be imported
        from the unnamed package } {
    saveas T76us2.java {
public class T76us2 {
    public static class Inner {}
}
    }
    saveas p/T76us2_1.java {
package p;
import T76us2.Inner;
class T76us2_1 {
    Inner t;
}
    }
    compile T76us2.java p/T76us2_1.java
} FAIL

tcltest::test 7.6-unnamed-scope-3 { Top-level types are not in scope (not available
        by simple name) in import statements, so nothing can be imported
        from the unnamed package } {
    saveas T76us3.java {
public class T76us3 {
    public static class Inner {}
}
    }
    saveas p/T76us3_1.java {
package p;
import T76us3.*;
class T76us3_1 {
    Inner t;
}
    }
    compile T76us3.java p/T76us3_1.java
} FAIL
