tcltest::test 7.4.2-1 {Create a public class in the default package} {
    saveas ClassInDefaultPackage.java \
{
public class ClassInDefaultPackage {}

class ClassInDefaultPackage_Test extends ClassInDefaultPackage {}
}

    compile ClassInDefaultPackage.java
} PASS

tcltest::test 7.4.2-2 {Extend a class in the default package with
        another class in default package} {
    saveas ExtendClassInDefaultPackage.java \
{
class ExtendClassInDefaultPackage extends ClassInDefaultPackage {}
}

    compile ClassInDefaultPackage.java ExtendClassInDefaultPackage.java
} PASS
