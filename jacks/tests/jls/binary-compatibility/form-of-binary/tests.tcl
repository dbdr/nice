tcltest::test 13.1-constructor-1 { Constructors in a non-private non-static
        nested class have an additional first parameter in the binary, for
        the enclosing class } {
    empty_class T131c1a {
	class Inner {}
    }
    delete T131c1a.java
    compile -classpath . [saveas T131c1b.java {
class T131c1b {
    Object o = new T131c1a().new Inner();
}
    }]
} PASS
