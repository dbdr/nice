# These tests cover the assert statement, added in JLS 1.4 by JSR 41.

# definite-assignment-fail - can't refer to the value b, because
# it is not definitely assigned yet
tcltest::test non-jls-jsr41.4-definite-assignment-fail-1 { V is DA before
        Expression1 iff V is DA before the assert } {assert} {
    empty_main T414daf1 {
	boolean b;
	assert b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-2 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414daf2 {
	boolean b, b1 = false;
	assert b1 : b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-3 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414daf3 {
	boolean b;
	assert false : b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-4 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf4 {
	boolean b, b1 = true;
	assert b1;
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-5 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf5 {
	boolean b, b1 = true;
	assert b1 : "";
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-6 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf6 {
	boolean b, b1;
	assert true : "";
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-7 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf7 {
	boolean b, b1;
	assert false : "";
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-8 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf8 {
	boolean b, b1 = true;
	assert b = b1;
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-9 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf9 {
	boolean b, b1;
	assert b = true;
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-10 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf10 {
	boolean b, b1;
	assert b = false;
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-11 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf11 {
	boolean b, b1 = true;
	assert b1 : b = b1;
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-12 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf12 {
	boolean b, b1;
	assert true : b = true;
	b1 = b;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-assignment-fail-13 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414daf13 {
	boolean b, b1;
	assert false : b = false;
	b1 = b;
    }
} FAIL

# definite-assignment-pass - can refer to the value b, because
# it is definitely assigned already
tcltest::test non-jls-jsr41.4-definite-assignment-pass-1 { V is DA before
        Expression1 iff V is DA before the assert } {assert} {
    empty_main T414dap1 {
	boolean b = true;
	assert b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-2 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap2 {
	boolean b;
	assert true : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-3 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap3 {
	boolean b = true, b1 = true;
	assert b1 : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-4 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap4 {
	boolean b = true;
	assert true : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-5 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap5 {
	boolean b = true;
	assert false : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-6 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap6 {
	boolean b, b1 = true;
	assert b = b1 : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-7 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap7 {
	boolean b;
	assert b = true : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-8 { V is DA before
        Expression2 iff V is DA after Expression1 when false } {assert} {
    empty_main T414dap8 {
	boolean b;
	assert b = false : b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-9 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414dap9 {
	boolean b = true, b1 = true;
	assert b1;
	b1 = b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-10 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414dap10 {
	boolean b = true, b1 = true;
	assert b1 : "";
	b1 = b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-11 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414dap11 {
	boolean b = true, b1;
	assert true : "";
	b1 = b;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-assignment-pass-12 { V is DA after
        the assert iff V is DA before the assert } {assert} {
    empty_main T414dap12 {
	boolean b = true, b1;
	assert false : "";
	b1 = b;
    }
} PASS

# definite-unassignment-fail - can't assign to the final b, because
# it is not still definitely unassigned
tcltest::test non-jls-jsr41.4-definite-unassignment-fail-1 { V is DU before
        Expression1 iff V is DU before the assert } {assert} {
    empty_main T414duf1 {
	final boolean b;
	b = true;
	assert b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-2 { V is DU before
        Expression2 iff V is DU after Expression1 when false } {assert} {
    empty_main T414duf2 {
	final boolean b;
	boolean b1 = b = true;
	assert b1 : b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-3 { V is DU before
        Expression2 iff V is DU after Expression1 when false } {assert} {
    empty_main T414duf3 {
	final boolean b;
	b = true;
	assert false : b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-4 { V is DU before
        Expression2 iff V is DU after Expression1 when false } {assert} {
    empty_main T414duf4 {
	final boolean b;
	boolean b1 = true;
	assert b = b1 : b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-5 { V is DU before
        Expression2 iff V is DU after Expression1 when false } {assert} {
    empty_main T414duf5 {
	final boolean b;
	assert b = true : b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-6 { V is DU before
        Expression2 iff V is DU after Expression1 when false } {assert} {
    empty_main T414duf6 {
	final boolean b;
	assert b = false : b = true;
    }
} FAIL

# NOTE: the third bullet of the proposed final draft of JSR 41 is
# incorrect, because it permits
#   final boolean b; b = true; assert false; b = true;
tcltest::test non-jls-jsr41.4-definite-unassignment-fail-7 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf7 {
	final boolean b;
	boolean b1 = b = true;
	assert b1;
	b = b1;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-8 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf8 {
	final boolean b;
	b = true;
	assert true;
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-9 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf9 {
	final boolean b;
	b = true;
	assert false;
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-10 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf10 {
	final boolean b;
	boolean b1 = b = true;
	assert b1 : "";
	b = b1;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-11 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf11 {
	final boolean b;
	b = true;
	assert true : "";
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-12 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf12 {
	final boolean b;
	b = true;
	assert false : "";
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-13 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf13 {
	final boolean b;
	boolean b1 = true;
	assert b = b1;
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-14 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf14 {
	final boolean b;
	assert b = true;
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-15 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf15 {
	final boolean b;
	assert b = false;
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-16 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf16 {
	final boolean b;
	boolean b1 = true;
	assert b = b1 : "";
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-17 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf17 {
	final boolean b;
	assert b = true : "";
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-18 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414duf18 {
	final boolean b;
	assert b = false : "";
	b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-19 { DU only applies
        to blank finals.  Initialized finals cannot be assigned. } {assert} {
    empty_main T414duf19 {
	final boolean b = true; // constant
	assert true : b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-20 { DU only applies
        to blank finals.  Initialized finals cannot be assigned. } {assert} {
    empty_main T414duf20 {
	final boolean b = Boolean.TRUE.booleanValue(); // non-constant
	assert true : b = true;
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-21 { DU only applies
        to blank finals.  Initialized finals cannot be assigned. } {assert} {
    empty_class T414duf21 {
	void foo(final boolean b) {
	    assert true : b = true;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-22 { DU only applies
        to blank finals.  Initialized finals cannot be assigned. } {assert} {
    empty_main T414duf22 {
	try {
	    throw new Exception();
	} catch (final Exception b) {
	    assert true : b = null;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-23 { Blank final fields
        cannot be assigned outside constructor/initializer. } {assert} {
    empty_class T414duf23 {
	static final boolean b;
	static { b = true; }
	void foo() {
	    assert true : b = true;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-fail-24 { Blank final
        fields cannot be assigned outside constructor/initializer. } {assert} {
    empty_class T414duf24 {
	final boolean b;
	{ b = true; }
	void foo() {
	    assert true : b = true;
	}
    }
} FAIL

# definite-unassignment-pass - can assign to the final b, because
# it is still definitely unassigned
tcltest::test non-jls-jsr41.4-definite-unassignment-pass-1 { V is DU before
        Expression1 iff V is DU before the assert } {assert} {
    empty_main T414dup1 {
	final boolean b;
	assert b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-2 { V is DU before
        Expression2 iff V is DU after Expression1 when false } {assert} {
    empty_main T414dup2 {
	final boolean b;
	b = true;
	assert true : b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-3 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup3 {
	final boolean b;
	boolean b1 = true;
	assert b1;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-4 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup4 {
	final boolean b;
	assert true;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-5 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup5 {
	final boolean b;
	assert false;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-6 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup6 {
	final boolean b;
	boolean b1 = true;
	assert b1 : b = true;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-7 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup7 {
	final boolean b;
	assert true : b = true;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-8 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup8 {
	final boolean b;
	assert false : b = true;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-9 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup9 {
	final boolean b;
	assert (b = true) && false;
	b = true;
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-pass-10 { V is DU after
        the assert iff V is DU after Expression1 when true and
        V is DU before the assert } {assert} {
    empty_main T414dup10 {
	final boolean b;
	assert (b = true) && false : "";
	b = true;
    }
} PASS

# definite-unassignment-try - definite assignment of catch and finally
# blocks must consider assert like throw.
# NOTE: The proposed final draft of JSR 41 refers to JLS 16.2.14 regarding
# throw statements, which is buggy because it permits things like:
#  final int i;
#  try { throw new AssertionError(i=1);
#  } catch (AssertionError e) { i=2; }
tcltest::test non-jls-jsr41.4-definite-unassignment-try-1 { V is not DU before
        a catch block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in the try block } {assert} {
    empty_main T414dut1 {
	final boolean b;
	try {
	    assert b = false;
	} catch (AssertionError e) {
	    b = false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-try-2 { V is not DU before
        a finally block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in the try block } {assert} {
    empty_main T414dut2 {
	final boolean b;
	try {
	    assert b = false;
	} finally {
	    b = false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-try-3 { V is not DU before
        a finally block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in any catch block } {assert} {
    empty_main T414dut3 {
	final boolean b;
	try {
	    throw new RuntimeException();
	} catch (RuntimeException e) {
	    assert b = false;
	} finally {
	    b = false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-try-4 { V is not DU before
        a catch block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in the try block } {assert} {
    empty_main T414dut4 {
	final boolean b;
	try {
	    assert false : b = true;
	} catch (AssertionError e) {
	    b = false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-try-5 { V is not DU before
        a finally block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in the try block } {assert} {
    empty_main T414dut5 {
	final boolean b;
	try {
	    assert false : b = true;
	} finally {
	    b = false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-try-6 { V is not DU before
        a finally block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in any catch block } {assert} {
    empty_main T414dut6 {
	final boolean b;
	try {
	    throw new RuntimeException();
	} catch (RuntimeException e) {
	    assert false : b = true;
	} finally {
	    b = false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-definite-unassignment-try-7 { V is not DU before
        a catch block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in the try block } {assert} {
    empty_main T414dut7 {
	final boolean b;
	try {
	    assert true : b = true;
	} catch (AssertionError e) {
	    b = false;
	}
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-try-8 { V is not DU before
        a finally block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in the try block } {assert} {
    empty_main T414dut8 {
	final boolean b;
	try {
	    assert true : b = true;
	} finally {
	    b = false;
	}
    }
} PASS

tcltest::test non-jls-jsr41.4-definite-unassignment-try-9 { V is not DU before
        a finally block unless V is DU after Expression1 when false, and
        after Expression2 if present, for all reachable assert
        statements contained in any catch block } {assert} {
    empty_main T414dut9 {
	final boolean b;
	try {
	    throw new RuntimeException();
	} catch (RuntimeException e) {
	    assert true : b = true;
	} finally {
	    b = false;
	}
    }
} PASS

# reachability
tcltest::test non-jls-jsr41.4-reachability-1 { an assert can complete normally
        iff it is reachable } {assert} {
    empty_main T414r1 {
	System.out.println();
	assert false;
	System.out.println();
    }
} PASS

tcltest::test non-jls-jsr41.4-reachability-2 { an assert can complete normally
        iff it is reachable } {assert} {
    empty_main T414r2 {
	System.out.println();
	assert false : "";
	System.out.println();
    }
} PASS

tcltest::test non-jls-jsr41.4-reachability-3 { an assert can complete normally
        iff it is reachable, but non-void methods cannot complete
        normally } {assert} {
    empty_class T414r3 {
	int foo() {
	    assert false;
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-reachability-4 { an assert can complete normally
        iff it is reachable, but non-void methods cannot complete
        normally } {assert} {
    empty_class T414r4 {
	int foo() {
	    assert false : "";
	}
    }
} FAIL

tcltest::test non-jls-jsr41.4-reachability-5 { an assert must be
        reachable } {assert} {
    empty_main T414r5 {
	return;
	assert false;
    }
} FAIL
