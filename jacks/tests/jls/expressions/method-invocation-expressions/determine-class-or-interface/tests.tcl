tcltest::test 15.12.1-syntax-1 { The type in the qualified super is not
        obscured } {
    empty_class T15121s1 {
        int T15121s1; // obscure the class name from normal expressions
        String s = T15121s1.super.toString();
    }
} PASS

tcltest::test 15.12.1-syntax-2 { The type in qualified super may not be a
        primitive type } {
    empty_class T15121s2 {
        Object o = int.super.toString();
    }
} FAIL

tcltest::test 15.12.1-syntax-3 { The type in qualified super may not be an
        array type } {
    empty_class T15121s3 {
        Object o = int[].super.toString();
    }
} FAIL

tcltest::test 15.12.1-syntax-4 { The type in qualified super may not be an
        array type } {
    empty_class T15121s4 {
        Object o = T15121s4[].super.toString();
    }
} FAIL

tcltest::test 15.12.1-syntax-5 { The type in qualified super may not be
        null } {
    empty_class T15121s5 {
        Object o = null.super.toString();
    }
} FAIL

tcltest::test 15.12.1-syntax-6 { The type in qualified super may not be
        void } {
    empty_class T15121s6 {
        Object o = void.super.toString();
    }
} FAIL

#TODO: Add tests for remainder of rules.
