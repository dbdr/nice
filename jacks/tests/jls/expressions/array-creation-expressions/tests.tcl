tcltest::test 15.10-initializer-syntax-1 { array initializer may be empty } {
    empty_main T1510is1 {
        int[] ia = new int[] {};
    }
} PASS

tcltest::test 15.10-initializer-syntax-2 { array initializer may contain
        initializers } {
    empty_main T1510is2 {
        int[] ia = new int[] { 1 };
    }
} PASS

tcltest::test 15.10-initializer-syntax-3 { array initializer may contain
        initializers } {
    empty_main T1510is3 {
        int[] ia = new int[] { 1, 2 };
    }
} PASS

tcltest::test 15.10-initializer-syntax-4 { array initializer may contain
        concluding comma } {
    empty_main T1510is4 {
        int[] ia = new int[] { , };
    }
} PASS

tcltest::test 15.10-initializer-syntax-5 { array initializer may contain
        concluding comma } {
    empty_main T1510is5 {
        int[] ia = new int[] { 1, };
    }
} PASS

tcltest::test 15.10-initializer-syntax-6 { array initializers may nest } {
    empty_main T1510is6 {
        int[][] iaa = new int[][] { {1}, {2} };
    }
} PASS

tcltest::test 15.10-initializer-syntax-7 { array bounds determined
        automatically when initialized } {
    empty_main T1510is7 {
	Object o = new int[1] {0};
    }
} FAIL

tcltest::test 15.10-initializer-type-1 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it1 {
        short s = 1;
        int[] ia = new int[] { s, '1' };
    }
} PASS

tcltest::test 15.10-initializer-type-2 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it2 {
        int[] ia = new int[] { 1L };
    }
} FAIL

tcltest::test 15.10-initializer-type-3 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it3 {
        int[] ia = new int[] { new Object() };
    }
} FAIL

tcltest::test 15.10-initializer-type-4 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it4 {
        int[] ia = new int[] { null };
    }
} FAIL

tcltest::test 15.10-initializer-type-5 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it5 {
        int[] ia = new int[] { System.out.println() };
    }
} FAIL

tcltest::test 15.10-initializer-type-6 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it6 {
        Object[] oa = new Object[] { 1 };
    }
} FAIL

tcltest::test 15.10-initializer-type-7 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it7 {
        Object[] oa = new Object[] { new Object() };
    }
} PASS

tcltest::test 15.10-initializer-type-8 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it8 {
        Object[] oa = new Object[] { null };
    }
} PASS

tcltest::test 15.10-initializer-type-9 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it9 {
        Object[] oa = new Object[] { System.out.println() };
    }
} FAIL

tcltest::test 15.10-initializer-type-10 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it10 {
        int[][] iaa = new int[][] { {1} };
    }
} PASS

tcltest::test 15.10-initializer-type-11 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it11 {
        int[][] iaa = new int[][] { 1 };
    }
} FAIL

tcltest::test 15.10-initializer-type-12 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it12 {
        int[][] iaa = new int[][] { { {1} } };
    }
} FAIL

tcltest::test 15.10-initializer-type-13 { expressions in array initializer
        must be assignment compatible with array type } {
    empty_main T1510it13 {
        int[][] iaa = new int[][] { {1, 2}, null };
    }
} PASS

tcltest::test 15.10-uninitialized-1 { At least one dimension must be
        specified } {
    empty_main T1510u1 {
	Object o = new int[][];
    }
} FAIL

tcltest::test 15.10-uninitialized-2 { Some dimensions may to be specified } {
    empty_main T1510u2 {
	Object o = new int[1][];
    }
} PASS

tcltest::test 15.10-uninitialized-3 { All dimensions may to be specified } {
    empty_main T1510u3 {
	Object o = new int[1][2];
    }
} PASS

tcltest::test 15.10-uninitialized-4 { Trailing dimensions may be specified
        only when leading ones are } {
    empty_main T1510u4 {
	Object o = new int[][2];
    }
} FAIL

tcltest::test 15.10-uninitialized-5 { negative constant dimensions are
        stupid, but legal } {
    ok_pass_or_warn [empty_main T1510u5 {
	Object o = new int[-1];
    }]
} OK

