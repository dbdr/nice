tcltest::test 7.4.1-1 {compiler should not generate error when
        compiling a class with a package statement that defines a package
        that can not be located on the CLASSPATH.} {
    compile [saveas ClassInNewPackage.java \
{
package some_package_that_is_not_known_at_compile_time;

public class ClassInNewPackage {}
}]
} PASS

