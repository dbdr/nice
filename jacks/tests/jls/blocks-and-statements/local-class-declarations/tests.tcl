tcltest::test 14.3-1 { tests local class declarations in both static and
        instance methods } {
    empty_class TestLCD {
        static Object static_method() {
            abstract class Foo { }
            return new Foo() { };
        }
        Object instance_method() {
            abstract class Foo { }
            return new Foo() { };
        }
    }
} PASS
