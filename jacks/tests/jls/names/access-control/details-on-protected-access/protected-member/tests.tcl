# test access of static fields
tcltest::test 6.6.2.1-protected-static-field-1 { Access to a protected
        static field is legal in the same package } {
    compile [saveas T6621psf1a.java {
class T6621psf1a {
    protected static int i;
}
class T6621psf1b {
    int j = T6621psf1a.i++;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-field-2 { Access to a protected
        static field is illegal outside the package in a non-subclass } {
    compile [saveas p1/T6621psf2a.java {
package p1;
public class T6621psf2a {
    protected static int i;
}
}] [saveas T6621psf2b.java {
class T6621psf2b {
    int j = p1.T6621psf2a.i++;
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-static-field-3 { Access to a protected
        static field is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621psf3a.java {
package p1;
public class T6621psf3a {
    protected static int i;
}
}] [saveas T6621psf3b.java {
class T6621psf3b extends p1.T6621psf3a {
    int j = p1.T6621psf3a.i++;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-field-4 { Access to a protected
        static field is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621psf4a.java {
package p1;
public class T6621psf4a {
    protected static int i;
}
}] [saveas p1/T6621psf4b.java {
package p1;
public class T6621psf4b extends T6621psf4a {}
}] [saveas T6621psf4c.java {
class T6621psf4c extends p1.T6621psf4a {
    int j = p1.T6621psf4b.i++;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-field-5 { Access to a protected
        static field is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621psf5a.java {
package p1;
public class T6621psf5a {
    protected static int i;
}
}] [saveas p1/T6621psf5c.java {
package p1;
public class T6621psf5c extends p2.T6621psf5b {}
}] [saveas p2/T6621psf5b.java {
package p2;
public class T6621psf5b extends p1.T6621psf5a {
    int j = p1.T6621psf5c.i++;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-field-6 { Access to a protected
        static field is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621psf6a.java {
package p1;
public class T6621psf6a {
    protected static int i;
}
}] [saveas T6621psf6b.java {
class T6621psf6b extends p1.T6621psf6a {
    Object o = new Object() {
        { p1.T6621psf6a.i++; }
    };
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-field-7 { Access to a protected
        static field is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621psf7a.java {
package p1;
public class T6621psf7a {
    protected static int i;
}
}] [saveas p1/T6621psf7b.java {
package p1;
public class T6621psf7b extends T6621psf7a {}
}] [saveas T6621psf7c.java {
class T6621psf7c extends p1.T6621psf7a {
    Object o = new Object() {
        { p1.T6621psf7b.i++; }
    };
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-field-8 { Access to a protected
        static field is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621psf8a.java {
package p1;
public class T6621psf8a {
    protected static int i;
}
}] [saveas p1/T6621psf8c.java {
package p1;
public class T6621psf8c extends p2.T6621psf8b {}
}] [saveas p2/T6621psf8b.java {
package p2;
public class T6621psf8b extends p1.T6621psf8a {
    Object o = new Object() {
        { p1.T6621psf8c.i++; }
    };
}
}]
} PASS

# test access of instance fields
tcltest::test 6.6.2.1-protected-instance-field-1 { Access to a protected
        instance field is legal in the same package } {
    compile [saveas T6621pif1a.java {
class T6621pif1a {
    protected int i;
}
class T6621pif1b {
    int j = new T6621pif1a().i++;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-instance-field-2 { Access to a protected
        instance field is illegal outside the package in a non-subclass } {
    compile [saveas p1/T6621pif2a.java {
package p1;
public class T6621pif2a {
    protected int i;
}
}] [saveas T6621pif2b.java {
class T6621pif2b {
    int j = new p1.T6621pif2a().i++;
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-field-3 { Access to a protected
        instance field is legal outside the package in a subclass only
        if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pif3a.java {
package p1;
public class T6621pif3a {
    protected int i;
}
}] [saveas T6621pif3b.java {
class T6621pif3b extends p1.T6621pif3a {
    int j = new p1.T6621pif3a().i++;
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-field-4 { Access to a protected
        instance field is legal outside the package in a subclass only
        if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pif4a.java {
package p1;
public class T6621pif4a {
    protected int i;
}
}] [saveas p1/T6621pif4b.java {
package p1;
public class T6621pif4b extends T6621pif4a {}
}] [saveas T6621pif4c.java {
class T6621pif4c extends p1.T6621pif4a {
    int j = new p1.T6621pif4b().i++;
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-field-5 { Access to a protected
        instance field is legal outside the package in a subclass only
        if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pif5a.java {
package p1;
public class T6621pif5a {
    protected static int i;
}
}] [saveas p1/T6621pif5c.java {
package p1;
public class T6621pif5c extends p2.T6621pif5b {}
}] [saveas p2/T6621pif5b.java {
package p2;
public class T6621pif5b extends p1.T6621pif5a {
    int j = new p1.T6621pif5c().i++;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-instance-field-6 { Access to a protected
        instance field is legal outside the package in the body of a subclass
        only if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pif6a.java {
package p1;
public class T6621pif6a {
    protected int i;
}
}] [saveas T6621pif6b.java {
class T6621pif6b extends p1.T6621pif6a {
    Object o = new Object() {
        { new p1.T6621pif6a().i++; }
    };
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-field-7 { Access to a protected
        instance field is legal outside the package in the body of a subclass
        only if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pif7a.java {
package p1;
public class T6621pif7a {
    protected int i;
}
}] [saveas p1/T6621pif7b.java {
package p1;
public class T6621pif7b extends T6621pif7a {}
}] [saveas T6621pif7c.java {
class T6621pif7c extends p1.T6621pif7a {
    Object o = new Object() {
        { new p1.T6621pif7b().i++; }
    };
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-field-8 { Access to a protected
        instance field is legal outside the package in the body of a subclass
        only if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pif8a.java {
package p1;
public class T6621pif8a {
    protected int i;
}
}] [saveas p1/T6621pif8c.java {
package p1;
public class T6621pif8c extends p2.T6621pif8b {}
}] [saveas p2/T6621pif8b.java {
package p2;
public class T6621pif8b extends p1.T6621pif8a {
    Object o = new Object() {
        { new p1.T6621pif8c().i++; }
    };
}
}]
} PASS

# test access of static methods
tcltest::test 6.6.2.1-protected-static-method-1 { Access to a protected
        static method is legal in the same package } {
    compile [saveas T6621psm1a.java {
class T6621psm1a {
    protected static void m() {}
}
class T6621psm1b {
    { T6621psm1a.m(); }
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-method-2 { Access to a protected
        static method is illegal outside the package in a non-subclass } {
    compile [saveas p1/T6621psm2a.java {
package p1;
public class T6621psm2a {
    protected static void m() {}
}
}] [saveas T6621psm2b.java {
class T6621psm2b {
    { p1.T6621psm2a.m(); }
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-static-method-3 { Access to a protected
        static method is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621psm3a.java {
package p1;
public class T6621psm3a {
    protected static void m() {}
}
}] [saveas T6621psm3b.java {
class T6621psm3b extends p1.T6621psm3a {
    { p1.T6621psm3a.m(); }
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-method-4 { Access to a protected
        static method is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621psm4a.java {
package p1;
public class T6621psm4a {
    protected static void m() {}
}
}] [saveas p1/T6621psm4b.java {
package p1;
public class T6621psm4b extends T6621psm4a {}
}] [saveas T6621psm4c.java {
class T6621psm4c extends p1.T6621psm4a {
    { p1.T6621psm4b.m(); }
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-method-5 { Access to a protected
        static method is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621psm5a.java {
package p1;
public class T6621psm5a {
    protected static void m() {}
}
}] [saveas p1/T6621psm5c.java {
package p1;
public class T6621psm5c extends p2.T6621psm5b {}
}] [saveas p2/T6621psm5b.java {
package p2;
public class T6621psm5b extends p1.T6621psm5a {
    { p1.T6621psm5c.m(); }
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-method-6 { Access to a protected
        static method is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621psm6a.java {
package p1;
public class T6621psm6a {
    protected static void m() {}
}
}] [saveas T6621psm6b.java {
class T6621psm6b extends p1.T6621psm6a {
    Object o = new Object() {
        { p1.T6621psm6a.m(); }
    };
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-method-7 { Access to a protected
        static method is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621psm7a.java {
package p1;
public class T6621psm7a {
    protected static void m() {}
}
}] [saveas p1/T6621psm7b.java {
package p1;
public class T6621psm7b extends T6621psm7a {}
}] [saveas T6621psm7c.java {
class T6621psm7c extends p1.T6621psm7a {
    Object o = new Object() {
        { p1.T6621psm7b.m(); }
    };
}
}]
} PASS

tcltest::test 6.6.2.1-protected-static-method-8 { Access to a protected
        static method is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621psm8a.java {
package p1;
public class T6621psm8a {
    protected static void m() {}
}
}] [saveas p1/T6621psm8c.java {
package p1;
public class T6621psm8c extends p2.T6621psm8b {}
}] [saveas p2/T6621psm8b.java {
package p2;
public class T6621psm8b extends p1.T6621psm8a {
    Object o = new Object() {
        { p1.T6621psm8c.m(); }
    };
}
}]
} PASS

# test access of instance methods
tcltest::test 6.6.2.1-protected-instance-method-1 { Access to a protected
        instance method is legal in the same package } {
    compile [saveas T6621pim1a.java {
class T6621pim1a {
    protected void m() {}
}
class T6621pim1b {
    { new T6621pim1a().m(); }
}
}]
} PASS

tcltest::test 6.6.2.1-protected-instance-method-2 { Access to a protected
        instance method is illegal outside the package in a non-subclass } {
    compile [saveas p1/T6621pim2a.java {
package p1;
public class T6621pim2a {
    protected void m() {}
}
}] [saveas T6621pim2b.java {
class T6621pim2b {
    { new p1.T6621pim2a().m(); }
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-method-3 { Access to a protected
        instance method is legal outside the package in a subclass only
        if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pim3a.java {
package p1;
public class T6621pim3a {
    protected void m() {}
}
}] [saveas T6621pim3b.java {
class T6621pim3b extends p1.T6621pim3a {
    { new p1.T6621pim3a().m(); }
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-method-4 { Access to a protected
        instance method is legal outside the package in a subclass only
        if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pim4a.java {
package p1;
public class T6621pim4a {
    protected void m() {}
}
}] [saveas p1/T6621pim4b.java {
package p1;
public class T6621pim4b extends T6621pim4a {}
}] [saveas T6621pim4c.java {
class T6621pim4c extends p1.T6621pim4a {
    { new p1.T6621pim4b().m(); }
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-method-5 { Access to a protected
        instance method is legal outside the package in a subclass only
        if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pim5a.java {
package p1;
public class T6621pim5a {
    protected static void m() {}
}
}] [saveas p1/T6621pim5c.java {
package p1;
public class T6621pim5c extends p2.T6621pim5b {}
}] [saveas p2/T6621pim5b.java {
package p2;
public class T6621pim5b extends p1.T6621pim5a {
    { new p1.T6621pim5c().m(); }
}
}]
} PASS

tcltest::test 6.6.2.1-protected-instance-method-6 { Access to a protected
        instance method is legal outside the package in the body of a subclass
        only if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pim6a.java {
package p1;
public class T6621pim6a {
    protected void m() {}
}
}] [saveas T6621pim6b.java {
class T6621pim6b extends p1.T6621pim6a {
    Object o = new Object() {
        { new p1.T6621pim6a().m(); }
    };
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-method-7 { Access to a protected
        instance method is legal outside the package in the body of a subclass
        only if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pim7a.java {
package p1;
public class T6621pim7a {
    protected void m() {}
}
}] [saveas p1/T6621pim7b.java {
package p1;
public class T6621pim7b extends T6621pim7a {}
}] [saveas T6621pim7c.java {
class T6621pim7c extends p1.T6621pim7a {
    Object o = new Object() {
        { new p1.T6621pim7b().m(); }
    };
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-instance-method-8 { Access to a protected
        instance method is legal outside the package in the body of a subclass
        only if the qualifier is the type the access occurs in } {
    compile [saveas p1/T6621pim8a.java {
package p1;
public class T6621pim8a {
    protected void m() {}
}
}] [saveas p1/T6621pim8c.java {
package p1;
public class T6621pim8c extends p2.T6621pim8b {}
}] [saveas p2/T6621pim8b.java {
package p2;
public class T6621pim8b extends p1.T6621pim8a {
    Object o = new Object() {
        { new p1.T6621pim8c().m(); }
    };
}
}]
} PASS

# test access of member types
tcltest::test 6.6.2.1-protected-type-1 { Access to a protected
        member type is legal in the same package } {
    compile [saveas T6621pt1a.java {
class T6621pt1a {
    protected class Inner {};
}
class T6621pt1b {
    Class c = T6621pt1a.Inner.class;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-type-2 { Access to a protected
        member type is illegal outside the package in a non-subclass } {
    compile [saveas p1/T6621pt2a.java {
package p1;
public class T6621pt2a {
    protected class Inner {}
}
}] [saveas T6621pt2b.java {
class T6621pt2b {
    Class c = p1.T6621pt2a.Inner.class;
}
}]
} FAIL

tcltest::test 6.6.2.1-protected-type-3 { Access to a protected
        member type is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621pt3a.java {
package p1;
public class T6621pt3a {
    protected class Inner {}
}
}] [saveas T6621pt3b.java {
class T6621pt3b extends p1.T6621pt3a {
    Class c = p1.T6621pt3a.Inner.class;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-type-4 { Access to a protected
        member type is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621pt4a.java {
package p1;
public class T6621pt4a {
    protected class Inner {}
}
}] [saveas p1/T6621pt4b.java {
package p1;
public class T6621pt4b extends T6621pt4a {}
}] [saveas T6621pt4c.java {
class T6621pt4c extends p1.T6621pt4a {
    Class c = p1.T6621pt4b.Inner.class;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-type-5 { Access to a protected
        member type is legal outside the package in a subclass, regardless
        of the type of the qualifier } {
    compile [saveas p1/T6621pt5a.java {
package p1;
public class T6621pt5a {
    protected class Inner {}
}
}] [saveas p1/T6621pt5c.java {
package p1;
public class T6621pt5c extends p2.T6621pt5b {}
}] [saveas p2/T6621pt5b.java {
package p2;
public class T6621pt5b extends p1.T6621pt5a {
    Class c = p1.T6621pt5c.Inner.class;
}
}]
} PASS

tcltest::test 6.6.2.1-protected-type-6 { Access to a protected
        member type is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621pt6a.java {
package p1;
public class T6621pt6a {
    protected class Inner {}
}
}] [saveas T6621pt6b.java {
class T6621pt6b extends p1.T6621pt6a {
    Object o = new Object() {
        { Class c = p1.T6621pt6a.Inner.class; }
    };
}
}]
} PASS

tcltest::test 6.6.2.1-protected-type-7 { Access to a protected
        member type is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621pt7a.java {
package p1;
public class T6621pt7a {
    protected class Inner {}
}
}] [saveas p1/T6621pt7b.java {
package p1;
public class T6621pt7b extends T6621pt7a {}
}] [saveas T6621pt7c.java {
class T6621pt7c extends p1.T6621pt7a {
    Object o = new Object() {
        { Class c = p1.T6621pt7b.Inner.class; }
    };
}
}]
} PASS

tcltest::test 6.6.2.1-protected-type-8 { Access to a protected
        member type is legal outside the package in the body of a subclass,
        regardless of the type of the qualifier } {
    compile [saveas p1/T6621pt8a.java {
package p1;
public class T6621pt8a {
    protected class Inner {}
}
}] [saveas p1/T6621pt8c.java {
package p1;
public class T6621pt8c extends p2.T6621pt8b {}
}] [saveas p2/T6621pt8b.java {
package p2;
public class T6621pt8b extends p1.T6621pt8a {
    Object o = new Object() {
        { Class c = p1.T6621pt8c.Inner.class; }
    };
}
}]
} PASS

