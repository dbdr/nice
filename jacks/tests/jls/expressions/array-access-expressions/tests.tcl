tcltest::test 15.13-anon-1 { According to the grammar in Chapter 18, it
        is also legal to acess the element of an anonymous array } {
    empty_main T1513a1 {
        int i = new int[]{1}[0];
    }
} PASS

tcltest::test 15.13-index-1 { negative constant index is stupid, but legal } {
    ok_pass_or_warn [empty_main T1513i1 {
	int[] i = {};
	i[-1] = 0;
    }]
} OK

tcltest::test 15.13-index-2 { out-of-bound index is legal, even when size
        and index are constant } {
    ok_pass_or_warn [empty_main T1513i2 {
	final int[] i = {};
	i[0] = 0;
    }]
} OK

tcltest::test 15.13-index-3 { long index is illegal } {
    empty_main T1513i3 {
	int[] i = { 1 };
	i[0L] = 0;
    }
} FAIL

tcltest::test 15.13-index-4 { float index is illegal } {
    empty_main T1513i4 {
	int[] i = { 1 };
	i[0f] = 0;
    }
} FAIL

tcltest::test 15.13-index-5 { double index is illegal } {
    empty_main T1513i5 {
	int[] i = { 1 };
	i[0.] = 0;
    }
} FAIL

tcltest::test 15.13-index-6 { null index is illegal } {
    empty_main T1513i6 {
	int[] i = { 1 };
	i[null] = 0;
    }
} FAIL

tcltest::test 15.13-index-7 { boolean index is illegal } {
    empty_main T1513i7 {
	int[] i = { 1 };
	i[false] = 0;
    }
} FAIL

tcltest::test 15.13-index-8 { Object index is illegal } {
    empty_main T1513i8 {
	int[] i = { 1 };
	i[""] = 0;
    }
} FAIL

tcltest::test 15.13-index-9 { void index is illegal } {
    empty_main T1513i9 {
	int[] i = { 1 };
	i[System.out.println()] = 0;
    }
} FAIL

tcltest::test 15.13-index-10 { index is unary promoted to int } {
    empty_main T1513i10 {
	int[] i = { 1 };
	short s = 0;
	byte b = 0;
	i[s] = i[b] = i['\0'] = i[0];
    }
} PASS

tcltest::test 15.13-reference-1 { reference must be array type } {
    empty_main T1513r1 {
	1[0]++;
    }
} FAIL

tcltest::test 15.13-reference-2 { reference must be array type } {
    empty_main T1513r2 {
	1L[0]++;
    }
} FAIL

tcltest::test 15.13-reference-3 { reference must be array type } {
    empty_main T1513r3 {
	1f[0]++;
    }
} FAIL

tcltest::test 15.13-reference-4 { reference must be array type } {
    empty_main T1513r4 {
	1.[0]++;
    }
} FAIL

tcltest::test 15.13-reference-5 { reference must be array type } {
    empty_main T1513r5 {
	'1'[0]++;
    }
} FAIL

tcltest::test 15.13-reference-6 { reference must be array type } {
    empty_main T1513r6 {
	System.out.println()[0]++;
    }
} FAIL

tcltest::test 15.13-reference-6 { reference must be array type } {
    empty_main T1513r6 {
	new Object()[0]++;
    }
} FAIL

tcltest::test 15.13-reference-7 { reference must be array type } {
    empty_main T1513r7 {
	Object o = new int[1];
	o[0]++;
    }
} FAIL

tcltest::test 15.13-reference-8 { reference must be array type } {
    empty_main T1513r8 {
	Object o = new int[1];
	o[0]++;
    }
} FAIL

tcltest::test 15.13-reference-9 { reference must be array type } {
    empty_main T1513r9 {
	null[0]++;
    }
} FAIL

tcltest::test 15.13-reference-10 { reference must be array type } {
    empty_class T1513r10 {
	int[] m() { return null; }
	void n() {
	    m()[0]++;
	}
    }
} PASS

tcltest::test 15.13-reference-11 { reference must be array type } {
    empty_main T1513r11 {
	(new int[1])[0]++;
    }
} PASS

tcltest::test 15.13-reference-12 { reference must be array type } {
    empty_class T1513r12 {
	static int[] i;
	void m() { 
	    i[0]++;
	    this.i[0]++;
	    T1513r12.i[0]++;
	}
    }
} PASS

tcltest::test 15.13-reference-13 { reference must be array type } {
    empty_main T1513r13 {
	int[][] i = { { 1 } };
	i[0][0]++;
	(i[0])[0]--;
    }
} PASS

tcltest::test 15.13-reference-14 { reference must be array type } {
    empty_main T1513r14 {
	int[][] i = { { 1 } };
	i[0][0][0]++;
    }
} FAIL

tcltest::test 15.13-final-1 { in a final array, array members aren't final } {
    empty_main T1513f1 {
	final int[] i = {0};
	i[0]++;
    }
} PASS

tcltest::test 15.13-type-1 { the type of an array access expression is the
        underlying type of the array } {
    empty_main T1513t1 {
	String[] s = {""};
	s[0].length();
	s[0].valueOf(1);
    }
} PASS

tcltest::test 15.13-type-2 { the type of an array access expression is the
        underlying type of the array } {
    empty_main T1513t2 {
	final Object[] s = {""};
	s[0].length();
    }
} FAIL

tcltest::test 15.13-type-3 { the type of an array access expression is the
        underlying type of the array } {
    empty_main T1513t3 {
	final Object[] s = {""};
	s[0].valueOf(1);
    }
} FAIL

