tcltest::test 6.6.2.2-protected-superctor-1 { A protected superconstructor may
        be always be accessed } {
    compile [saveas T6622ps1a.java {
class T6622ps1a {
    protected T6622ps1a() {}
}
class T6622ps1b extends T6622ps1a {}
}]
} PASS

tcltest::test 6.6.2.2-protected-superctor-2 { A protected superconstructor may
        be always be accessed } {
    compile [saveas p1/T6622ps2a.java {
package p1;
public class T6622ps2a {
    protected T6622ps2a() {}
}
}] [saveas T6622ps2b.java {
class T6622ps2b extends p1.T6622ps2a {}
}]
} PASS

tcltest::test 6.6.2.2-protected-superctor-3 { A protected superconstructor may
        be always be accessed, even by an anonymous class } {
    compile [saveas T6622ps3a.java {
class T6622ps3a {
    protected T6622ps3a() {}
}
class T6622ps3b {
    Object o = new T6622ps3a() {};
}
}]
} PASS

tcltest::test 6.6.2.2-protected-superctor-4 { A protected superconstructor may
        be always be accessed, even by an anonymous class } {
    compile [saveas p1/T6622ps4a.java {
package p1;
public class T6622ps4a {
    protected T6622ps4a() {}
}
}] [saveas T6622ps4b.java {
class T6622ps4b {
    Object o = new p1.T6622ps4a() {};
}
}]
} PASS

tcltest::test 6.6.2.2-protected-creation-1 { A protected constructor may
        be accessed by instance creation in the same package } {
    compile [saveas T6622pc1a.java {
class T6622pc1a {
    protected T6622pc1a() {}
}
class T6622pc1b {
    Object o = new T6622pc1a();
}
}]
} PASS

tcltest::test 6.6.2.2-protected-creation-2 { A protected constructor may
        not be accessed by instance creation outside the same package } {
    compile [saveas p1/T6622pc2a.java {
package p1;
public class T6622pc2a {
    protected T6622pc2a() {}
}
}] [saveas T6622pc2b.java {
class T6622pc2b {
    Object o = new p1.T6622pc2a();
}
}]
} FAIL

tcltest::test 6.6.2.2-protected-creation-3 { A protected constructor may
        not be accessed by instance creation outside the same package,
        even when the access is in a subclass } {
    compile [saveas p1/T6622pc3a.java {
package p1;
public class T6622pc3a {
    protected T6622pc3a() {}
}
}] [saveas T6622pc3b.java {
class T6622pc3b extends p1.T6622pc3a {
    Object o = new p1.T6622pc3a();
}
}]
} FAIL

