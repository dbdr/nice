tcltest::test 8.8-simpletypename-1 { SimpleTypeName in the
        ConstructorDeclarator must be the simple name of the
        class that contains the constructor declaration } {
    empty_class T88stn1 "T88stn1() {}"
} PASS

tcltest::test 8.8-simpletypename-2 { SimpleTypeName in the
        ConstructorDeclarator must be the simple name of the
        class that contains the constructor declaration } {
    empty_class T88stn2 "not_ctor() {}"
} FAIL

tcltest::test 8.8-simpletypename-3 { SimpleTypeName in the
        ConstructorDeclarator must be the simple name of the
        class that contains the constructor declaration } {
    empty_class T88stn3 {
        class Inner {
            not_ctor() {}
        }
    }
} FAIL

tcltest::test 8.8-simpletypename-4 { SimpleTypeName in the
        ConstructorDeclarator must be the simple name of the
        class that contains the constructor declaration } {
    empty_main T88stn4 {
        class Local {
            Local() {} // trigger jikes bug 179
            not_ctor() {}
        }
    }
} FAIL

tcltest::test 8.8-example-1 { JLS example } {
    empty_class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }
} PASS
