tcltest::test 8.4.5-1 { {} is a valid method body} {
    compile [saveas EmptyBlockMethodBody.java \
{
public class EmptyBlockMethodBody {
    public void foo() {}
}
}]
} PASS

tcltest::test 8.4.5-2 { only a native or abstract method can have no method body } {
    compile [saveas InvalidEmptyMethodBody.java \
{
public class InvalidEmptyMethodBody {
    public void foo() ;
}
}]
} FAIL

tcltest::test 8.4.5-3 { a native method does not have a body } {
    compile [saveas EmptyNativeMethodBody.java \
{
public class EmptyNativeMethodBody {
    public native void foo();
}
}]
} PASS

tcltest::test 8.4.5-4 { a native method can not have a body } {
    compile [saveas InvalidNativeMethodBody.java \
{
public class InvalidNativeMethodBody {
    public native void foo() {}
}
}]
} FAIL

tcltest::test 8.4.5-5 { an abstract method does not have a body } {
    compile [saveas EmptyAbstractMethodBody.java \
{
public abstract class EmptyAbstractMethodBody {
    public abstract void foo();
}
}]
} PASS

tcltest::test 8.4.5-6 { an abstract method can not have a body } {
    compile [saveas InvalidAbstractMethodBody.java \
{
public abstract class InvalidAbstractMethodBody {
    public abstract void foo() {}
}
}]
} FAIL

# These seem debatable, if no return is inside a method block
# that is declared non void, should that be covered in tests
# for section 14.15 (return statement) or here in 8.4.5 (method body) ?

tcltest::test 8.4.5-7 { non void method without a return is invalid } {
    compile [saveas NoReturnInNonVoid.java \
{
public class NoReturnInNonVoid {
    public boolean foo() {}
}
}]
} FAIL

tcltest::test 8.4.5-8 { a void method with a non expression return is valid } {
    compile [saveas NoExpressionReturnInVoid.java \
{
public class NoExpressionReturnInVoid {
    public void foo() { return; }
}
}]
} PASS

tcltest::test 8.4.5-9 { a return with no expression in a non void method is invalid } {
    compile [saveas ConflictingReturnInNonVoid.java \
{
public class ConflictingReturnInNonVoid {
    public boolean foo() {
        int i = 0;
        if (i == 1) {
            return true;
        } else {
            return;
        }
    }
}
}]
} FAIL

tcltest::test 8.4.5-10 { a return with an expression in a void method is invalid } {
    compile [saveas ConflictingReturnInVoid.java \
{
public class ConflictingReturnInVoid {
    public void foo() {
        int i = 0;
        if (i == 1) {
            return true;
        } else {
            return;
        }
    }
}
}]
} FAIL

tcltest::test 8.4.5-11 { a method with an exceptional exit does not require a return ? } {
    compile [saveas ExceptionalReturnInNonVoid.java \
{
public class ExceptionalReturnInNonVoid {
    public boolean foo() { throw new RuntimeException("foo()"); }
}
}]
} PASS
