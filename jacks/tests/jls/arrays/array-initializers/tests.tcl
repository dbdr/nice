tcltest::test 10.6-syntax-1 { array initializer may be empty } {
    empty_main T106s1 {
        int[] ia = {};
    }
} PASS

tcltest::test 10.6-syntax-2 { array initializer may contain initializers } {
    empty_main T106s2 {
        int[] ia = { 1 };
    }
} PASS

tcltest::test 10.6-syntax-3 { array initializer may contain initializers } {
    empty_main T106s3 {
        int[] ia = { 1, 2 };
    }
} PASS

tcltest::test 10.6-syntax-4 { array initializer may contain concluding comma } {
    empty_main T106s4 {
        int[] ia = { , };
    }
} PASS

tcltest::test 10.6-syntax-5 { array initializer may contain concluding comma } {
    empty_main T106s5 {
        int[] ia = { 1, };
    }
} PASS

tcltest::test 10.6-syntax-6 { array initializers may nest } {
    empty_main T106i6 {
        int[][] iaa = { {1}, {2} };
    }
} PASS


tcltest::test 10.6-type-1 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t1 {
        short s = 1;
        int[] ia = { s, '1' };
    }
} PASS

tcltest::test 10.6-type-2 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t2 {
        int[] ia = { 1L };
    }
} FAIL

tcltest::test 10.6-type-3 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t3 {
        int[] ia = { new Object() };
    }
} FAIL

tcltest::test 10.6-type-4 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t4 {
        int[] ia = { null };
    }
} FAIL

tcltest::test 10.6-type-5 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t5 {
        int[] ia = { System.out.println() };
    }
} FAIL

tcltest::test 10.6-type-6 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t6 {
        Object[] oa = { 1 };
    }
} FAIL

tcltest::test 10.6-type-7 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t7 {
        Object[] oa = { new Object() };
    }
} PASS

tcltest::test 10.6-type-8 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t8 {
        Object[] oa = { null };
    }
} PASS

tcltest::test 10.6-type-9 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t9 {
        Object[] oa = { System.out.println() };
    }
} FAIL

tcltest::test 10.6-type-10 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t10 {
        int[][] iaa = { {1} };
    }
} PASS

tcltest::test 10.6-type-11 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t11 {
        int[][] iaa = { 1 };
    }
} FAIL

tcltest::test 10.6-type-12 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t12 {
        int[][] iaa = { { {1} } };
    }
} FAIL

tcltest::test 10.6-type-13 { expressions in array initializer must be
        assignment compatible with array type } {
    empty_main T106t13 {
        int[][] iaa = { {1, 2}, null };
    }
} PASS


