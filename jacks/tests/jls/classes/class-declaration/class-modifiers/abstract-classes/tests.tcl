tcltest::test 8.1.1.1-1 { declare an abstract class } {
    compile [saveas AbstractClass.java "public abstract class AbstractClass {}"]
} PASS

tcltest::test 8.1.1.1-2 { Compile time error on attempt to
        create an instance of an abstract class } {
    saveas AbstractClass_CreateInstanceError.java \
{
public class AbstractClass_CreateInstanceError {
    Object o = new AbstractClass();
}
}

    compile AbstractClass.java AbstractClass_CreateInstanceError.java
} FAIL

# These next two could also be covered in 8.4.3.1 (abstract methods)

tcltest::test 8.1.1.1-3 { declare an abstract class with an abstract method } {
    compile [saveas AbstractMethodAbstractClass.java \
{
public abstract class AbstractMethodAbstractClass {
    abstract void foo();
}
}]
} PASS

tcltest::test 8.1.1.1-4 { a non-abstract class can not have an abstract method } {
    compile [saveas AbstractMethodNonAbstractClass.java \
{
public class AbstractMethodNonAbstractClass {
    abstract void foo();
}
}]
} FAIL

tcltest::test 8.1.1.1-5 { An attempt to inherit from an abstract superclass containing
        an abstract method without implementing the abstract method or declaring
        the subclass abstract should generate a compile time error } {
    saveas InheritAbstractClassError.java \
{
abstract class InheritAbstractClassError_Superclass {
    abstract void foo();
}

public class InheritAbstractClassError extends InheritAbstractClassError_Superclass {}
}

    compile InheritAbstractClassError.java
} FAIL

tcltest::test 8.1.1.1-6 { An attempt to inherit from an abstract superclass that
        extends another class that has an abstract method without implementing the abstract
        method or declaring the subclass abstract should generate a compile time error } {
    saveas DoubleInheritAbstractClassError.java \
{
abstract class DoubleInheritAbstractClassError_Superclass1 {
    abstract void foo();
}

abstract class DoubleInheritAbstractClassError_Superclass2 extends
    DoubleInheritAbstractClassError_Superclass1 {}

public class DoubleInheritAbstractClassError extends DoubleInheritAbstractClassError_Superclass2 {}
}

    compile DoubleInheritAbstractClassError.java
} FAIL

tcltest::test 8.1.1.1-7 { inherit from an abstract class with an
        abstract method, the subclass must be marked as abstract } {
    saveas InheritAbstractClass.java \
{
abstract class InheritAbstractClass_Superclass {
    abstract void foo();
}

public abstract class InheritAbstractClass extends InheritAbstractClass_Superclass {}
}

    compile InheritAbstractClass.java
} PASS

tcltest::test 8.1.1.1-8 { Compile time error on attempt to create an instance
        of an abstract class that inherits from and abstract class } {
    saveas InheritAbstractClass_CreateInstanceError.java \
{
public class InheritAbstractClass_CreateInstanceError {
    Object o = new InheritAbstractClass();
}
}

    compile InheritAbstractClass.java InheritAbstractClass_CreateInstanceError.java
} FAIL

tcltest::test 8.1.1.1-9 { It should be possible to inherit from an abstract class
        that defines no abstract methods, the subclass does not need to be abstract } {
    compile [saveas InheritEmptyAbstractClass.java \
{
abstract class InheritEmptyAbstractClass_Superclass {}

public class InheritEmptyAbstractClass extends InheritEmptyAbstractClass_Superclass {}
}]
} PASS

tcltest::test 8.1.1.1-10 { Inherit from an abstract class with no methods that also
        inherits from an abstract class with no methods, subclass does not need to be abstract } {
    saveas DoubleInheritEmptyAbstractClass.java \
{
abstract class DoubleInheritEmptyAbstractClass_Superclass1 {}
abstract class DoubleInheritEmptyAbstractClass_Superclass2 extends
    DoubleInheritEmptyAbstractClass_Superclass1 {}

public class DoubleInheritEmptyAbstractClass extends DoubleInheritEmptyAbstractClass_Superclass2 {}
}

    compile DoubleInheritEmptyAbstractClass.java
} PASS

tcltest::test 8.1.1.1-11 { Implement an abstract method inherited from an abstract superclass } {
  saveas ImplementInheritedAbstractMethod.java \
{
abstract class ImplementInheritedAbstractMethod_Superclass1 {
    abstract void foo();
}

abstract class ImplementInheritedAbstractMethod_Superclass2 extends
    ImplementInheritedAbstractMethod_Superclass1 {}

public class ImplementInheritedAbstractMethod
    extends ImplementInheritedAbstractMethod_Superclass2 {
    void foo() {}
}
}

  compile ImplementInheritedAbstractMethod.java
} PASS

# This next one is mentioned in 8.1.1.1 but is is actually a generic return type conflict.

tcltest::test 8.1.1.1-12 { A compile time error should be generated in the case that
        an abstract class has two methods with the same signature but different return
        types, this would create an abstract class that could not be sub-classed } {
    saveas InterfaceSignatureConflict.java \
{
interface InterfaceSignatureConflict_Inter {
    void foo();
}

public abstract class InterfaceSignatureConflict implements InterfaceSignatureConflict_Inter {
    public abstract int foo();
}
}

    compile InterfaceSignatureConflict.java
} FAIL

tcltest::test 8.1.1.1-default-abstract-1 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da1a.java {
package p1;
public abstract class T8111da1a {
  abstract void m();
}
}
    saveas p2/T8111da1b.java {
package p2;
class T8111da1b extends p1.T8111da1a {}
}
    compile p1/T8111da1a.java p2/T8111da1b.java
} FAIL

tcltest::test 8.1.1.1-default-abstract-2 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da2a.java {
package p1;
public abstract class T8111da2a {
  abstract void m();
}
}
    saveas p2/T8111da2b.java {
package p2;
abstract class T8111da2b extends p1.T8111da2a {}
}
    compile p1/T8111da2a.java p2/T8111da2b.java
} PASS

tcltest::test 8.1.1.1-default-abstract-3 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da3a.java {
package p1;
public abstract class T8111da3a {
  abstract void m();
}
}
    saveas p2/T8111da3b.java {
package p2;
abstract class T8111da3b extends p1.T8111da3a {}
class T8111da3c extends T8111da3b {}
}
    compile p1/T8111da3a.java p2/T8111da3b.java
} FAIL

tcltest::test 8.1.1.1-default-abstract-4 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da4a.java {
package p1;
public abstract class T8111da4a {
  abstract void m();
}
}
    saveas p2/T8111da4b.java {
package p2;
abstract class T8111da4b extends p1.T8111da4a {}
abstract class T8111da4c extends T8111da4b {}
}
    compile p1/T8111da4a.java p2/T8111da4b.java
} PASS

tcltest::test 8.1.1.1-default-abstract-5 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da5a.java {
package p1;
public abstract class T8111da5a {
  abstract void m();
}
}
    saveas p2/T8111da5b.java {
package p2;
public abstract class T8111da5b extends p1.T8111da5a {}
}
    saveas p1/T8111da5c.java {
package p1;
public class T8111da5c extends p2.T8111da5b {}
}
    compile p1/T8111da5a.java p2/T8111da5b.java p1/T8111da5c.java
} FAIL

tcltest::test 8.1.1.1-default-abstract-6 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da6a.java {
package p1;
public abstract class T8111da6a {
  abstract void m();
}
}
    saveas p2/T8111da6b.java {
package p2;
public abstract class T8111da6b extends p1.T8111da6a {}
}
    saveas p1/T8111da6c.java {
package p1;
public abstract class T8111da6c extends p2.T8111da6b {}
}
    compile p1/T8111da6a.java p2/T8111da6b.java p1/T8111da6c.java
} PASS

tcltest::test 8.1.1.1-default-abstract-7 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da7b.java {
package p1;
abstract class T8111da7a {
  abstract void m();
}
public abstract class T8111da7b extends T8111da7a {}
}
    saveas p2/T8111da7c.java {
package p2;
abstract class T8111da7c extends p1.T8111da7b {}
class T8111da7d extends T8111da7c {}
}
    compile p1/T8111da7b.java p2/T8111da7c.java
} FAIL

tcltest::test 8.1.1.1-default-abstract-8 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da8b.java {
package p1;
abstract class T8111da8a {
  abstract void m();
}
public abstract class T8111da8b extends T8111da8a {}
}
    saveas p2/T8111da8c.java {
package p2;
abstract class T8111da8c extends p1.T8111da8b {}
abstract class T8111da8d extends T8111da8c {}
}
    compile p1/T8111da8b.java p2/T8111da8c.java
} PASS

tcltest::test 8.1.1.1-default-abstract-9 { Default access abstract methods are
        not inherited across class boundaries, but do affect whether the
        class must be abstract } {
    saveas p1/T8111da9a.java {
package p1;
public abstract class T8111da9a {
  abstract void m();
}
}
    saveas p2/T8111da9b.java {
package p2;
public abstract class T8111da9b extends p1.T8111da9a {}
class T8111da9d extends p1.T8111da9c {}
}
    saveas p1/T8111da9c.java {
package p1;
public abstract class T8111da9c extends p2.T8111da9b {
    void m() {} // overrides a.m(), even though a.m() is not inherited!
}
}
    compile p1/T8111da9a.java p2/T8111da9b.java p1/T8111da9c.java
} PASS
