tcltest::test 16.1.8-definite-unassign-fail-1 { ++ and -- cannot be applied to
        final variables } {
    empty_main T1618duf1 {
	final int i;
	if (false)
	    i++;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-2 { ++ and -- cannot be applied to
        final variables } {
    empty_main T1618duf2 {
	final int i;
	if (false)
	    i--;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-3 { ++ and -- cannot be applied to
        final variables } {
    empty_main T1618duf3 {
	final int i;
	if (false)
	    ++i;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-4 { ++ and -- cannot be applied to
        final variables } {
    empty_main T1618duf4 {
	final int i;
	if (false)
	    --i;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-5 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf5 {
	final int i;
	int[] j = {0};
	j[i = 0]++;
	i = 2;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-6 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf6 {
	final int i;
	int[] j = {0};
	j[i = 0]--;
	i = 2;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-7 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf7 {
	final int i;
	int[] j = {0};
	++j[i = 0];
	i = 2;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-8 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf8 {
	final int i;
	int[] j = {0};
	--j[i = 0];
	i = 2;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-9 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf9 {
	final int[] j;
	(j = new int[] {0})[0]++;
	j = null;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-10 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf10 {
	final int[] j;
	(j = new int[] {0})[0]--;
	j = null;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-11 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf11 {
	final int[] j;
	++(j = new int[] {0})[0];
	j = null;
    }
} FAIL

tcltest::test 16.1.8-definite-unassign-fail-12 { v is DU after a++ iff v is
        DU after a } {
    empty_main T1618duf12 {
	final int[] j;
	--(j = new int[] {0})[0];
	j = null;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-pass-1 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap1 {
	int i;
	if (false) {
            i++;
	    int j = i;
	}
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-2 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap2 {
	int i;
	if (false) {
            i--;
	    int j = i;
	}
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-3 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap3 {
	int i;
	if (false) {
            ++i;
	    int j = i;
	}
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-4 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap4 {
	int i;
	if (false) {
            --i;
	    int j = i;
	}
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-5 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap5 {
	int i, j[] = {0};
	j[i = 0]++;
	int k = i;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-6 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap6 {
	int i, j[] = {0};
	j[i = 0]--;
	int k = i;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-7 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap7 {
	int i, j[] = {0};
	++j[i = 0];
	int k = i;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-8 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap8 {
	int i, j[] = {0};
	--j[i = 0];
	int k = i;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-9 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap9 {
	int[] j;
	(j = new int[] {0})[0]++;
	Object o = j;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-10 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap10 {
	int[] j;
	(j = new int[] {0})[0]--;
	Object o = j;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-11 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap11 {
	int[] j;
	++(j = new int[] {0})[0];
	Object o = j;
    }
} PASS

tcltest::test 16.1.8-definite-assign-pass-12 { v is DA after a++ iff a is v
        or v is DA after a } {
    empty_main T1618dap12 {
	int[] j;
	--(j = new int[] {0})[0];
	Object o = j;
    }
} PASS

tcltest::test 16.1.8-definite-assign-fail-1 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf1 {
	int i;
	i++;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-2 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf2 {
	int i;
	i--;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-3 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf3 {
	int i;
	++i;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-4 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf4 {
	int i;
	--i;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-5 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf5 {
	int i;
	(new int[] {0})[i]++;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-6 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf6 {
	int i;
	(new int[] {0})[i]--;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-7 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf7 {
	int i;
	++(new int[] {0})[i];
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-8 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf8 {
	int i;
	--(new int[] {0})[i];
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-9 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf9 {
	int[] i;
	i[0]++;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-10 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf10 {
	int[] i;
	i[0]--;
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-11 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf11 {
	int[] i;
	++i[0];
    }
} FAIL

tcltest::test 16.1.8-definite-assign-fail-12 { v is DA before a iff v is DA
        before a++ } {
    empty_main T1618daf12 {
	int[] i;
	--i[0];
    }
} FAIL

