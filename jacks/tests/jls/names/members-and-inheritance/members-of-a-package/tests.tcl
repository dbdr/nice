tcltest::test 6.4.1-class-member-1 { A class declared in a package
        is a member of that package } {

    saveas pkg/T641cm1.java {
package pkg;
public class T641cm1 {}
}

    saveas T641cm1_Test.java {
public class T641cm1_Test {
    Object o = new pkg.T641cm1();
}
}

    compile -classpath . pkg/T641cm1.java T641cm1_Test.java
} PASS

