tcltest::test 6.3-1 { symbol used in field initialization must be defined before it can be used } {
    compile [saveas IllegalForwardReference.java \
{
public class IllegalForwardReference {
  int a = b;
  int b = 0;
}
}]
} FAIL

tcltest::test 6.3-2 { forward reference to a field outside of an initializer is legal } {
    compile [saveas LegalForwardReference.java \
{
public class LegalForwardReference {
  void foo() { a = 1; }
  int a;
}
}]
} PASS

tcltest::test 6.3-3 { use of a class before it is defined is legal } {
    compile [saveas UseOfTypeBeforeDeclaration.java \
{
public class UseOfTypeBeforeDeclaration {
  Object o = new UseOfTypeBeforeDeclaration_Type();
}

class UseOfTypeBeforeDeclaration_Type {
  Object o = new UseOfTypeBeforeDeclaration();
}
}]
} PASS

tcltest::test 6.3-4 { the scope of a class includes every known member of
        the package, so we should be able to make a forward ref } {
    saveas GlobalOne.java \
{
public class GlobalOne {
    void foo() {
	Object o = new GlobalTwo();
    }
}
}

    saveas GlobalTwo.java \
{
public class GlobalTwo {
    void foo() {
	Object o = new GlobalOne();
    }
}
}

    compile GlobalOne.java GlobalTwo.java
} PASS
