# definite assignment - referencing x should fail
tcltest::test 16.1.5-definite-assignment-fail-1 { V is DA before
        a iff V is DA before boolean a?b:c } {
    empty_main T1615daf1 {
	boolean x;
	if (x ? true : false);
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-2 { V is DA before
        b iff V is DA after a when true } {
    empty_main T1615daf2 {
	boolean a = true, x;
	if (a ? x : false);
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-3 { V is DA before
        b iff V is DA after a when true } {
    empty_main T1615daf3 {
	boolean x;
	if (true ? x : false);
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-4 { V is DA before
        c iff V is DA after a when false } {
    empty_main T1615daf4 {
	boolean a = true, x;
	if (a ? true : x);
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-5 { V is DA before
        c iff V is DA after a when false } {
    empty_main T1615daf5 {
	boolean x;
	if (false ? true : x);
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-6 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615daf6 {
	boolean a = true, x;
	if (a ? true : true)
	    a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-7 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615daf7 {
	boolean a = true, x;
	if (a ? x = true : true)
	    a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-8 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615daf8 {
	boolean a = false, x;
	if (a ? true : (x = true))
	    a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-9 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615daf9 {
	boolean a = true, x;
	if (a ? false : false);
	else a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-10 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615daf10 {
	boolean a = true, x;
	if (a ? x = false : false);
	else a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-11 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615daf11 {
	boolean a = false, x;
	if (a ? false : (x = false));
	else a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-12 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615daf12 {
	boolean a = true, x;
	if (a ? true : true);
	a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-13 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615daf13 {
	boolean a = true, x;
	if (a ? x = true : true);
	a = x;
    }
} FAIL

tcltest::test 16.1.5-definite-assignment-fail-14 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615daf14 {
	boolean a = false, x;
	if (a ? true : (x = true));
	a = x;
    }
} FAIL

# definite assignment - referencing x should pass
tcltest::test 16.1.5-definite-assignment-pass-1 { V is DA before
        a iff V is DA before boolean a?b:c } {
    empty_main T1615dap1 {
	boolean x = true;
	if (x ? true : false);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-2 { V is DA before
        b iff V is DA after a when true } {
    empty_main T1615dap2 {
	boolean a = true, x = true;
	if (a ? x : false);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-3 { V is DA before
        b iff V is DA after a when true } {
    empty_main T1615dap3 {
	boolean x = true;
	if (true ? x : false);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-4 { V is DA before
        b iff V is DA after a when true } {
    empty_main T1615dap4 {
	boolean x;
	if (false ? x : false);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-5 { V is DA before
        b iff V is DA after a when true } {
    empty_main T1615dap5 {
	boolean x;
	if ((x = true) ? x : false);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-6 { V is DA before
        c iff V is DA after a when false } {
    empty_main T1615dap6 {
	boolean a = true, x = true;
	if (a ? true : x);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-7 { V is DA before
        c iff V is DA after a when false } {
    empty_main T1615dap7 {
	boolean x = true;
	if (false ? true : x);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-8 { V is DA before
        c iff V is DA after a when false } {
    empty_main T1615dap8 {
	boolean x;
	if (true ? true : x);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-9 { V is DA before
        c iff V is DA after a when false } {
    empty_main T1615dap9 {
	boolean x;
	if ((x = false) ? true : x);
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-10 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap10 {
	boolean a = true, x = true;
	if (a ? true : true)
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-11 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap11 {
	boolean a = true, x;
	if (a ? false : (x = true))
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-12 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap12 {
	boolean a, x;
	if (true ? false : (x = true))
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-13 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap13 {
	boolean a = false, x;
	if (a ? x = true : false)
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-14 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap14 {
	boolean a, x;
	if (false ? x = true : false)
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-15 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap15 {
	boolean a = true, x;
	if (a ? x = true : (x = true))
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-16 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap16 {
	boolean a, x;
	if (true ? x = true : (x = true))
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-17 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap17 {
	boolean a = true, x;
	if (false ? x = true : (x = true))
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-18 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap18 {
	boolean a, x;
	if (true ? x = true : true)
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-19 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap19 {
	boolean a, x;
	if (false ? true : (x = true))
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-20 { V is DA after
        boolean a?b:c when true iff V is DA after b when true
        and V is DA after c when true } {
    empty_main T1615dap20 {
	boolean a, x;
	if ((x = true) ? true : true)
	    a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-21 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap21 {
	boolean a = true, x = true;
	if (a ? false : false);
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-22 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap22 {
	boolean a = true, x;
	if (a ? true : (x = false));
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-23 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap23 {
	boolean a, x;
	if (true ? true : (x = false));
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-24 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap24 {
	boolean a = false, x;
	if (a ? x = false : true);
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-25 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap25 {
	boolean a, x;
	if (false ? x = false : true);
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-26 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap26 {
	boolean a = true, x;
	if (a ? x = false : (x = false));
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-27 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap27 {
	boolean a, x;
	if (true ? x = false : (x = false));
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-28 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap28 {
	boolean a = true, x;
	if (false ? x = false : (x = false));
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-29 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap29 {
	boolean a, x;
	if (true ? x = false : false);
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-30 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap30 {
	boolean a, x;
	if (false ? false : (x = false));
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-31 { V is DA after
        boolean a?b:c when false iff V is DA after b when false
        and V is DA after c when false } {
    empty_main T1615dap31 {
	boolean a, x;
	if ((x = false) ? false : false);
	else a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-32 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615dap32 {
	boolean a = true, x = true;
	if (a ? true : true);
	a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-33 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615dap33 {
	boolean a, x;
	if ((x = true) ? true : true);
	a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-34 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615dap34 {
	boolean a = true, x;
	if (a ? x = true : (x = true));
	a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-35 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615dap35 {
	boolean a, x;
	if (true ? x = true : false);
	a = x;
    }
} PASS

tcltest::test 16.1.5-definite-assignment-pass-36 { V is DA after
        boolean a?b:c iff V is DA after boolean a?b:c when true
        and V is DA after boolean a?b:c when false } {
    empty_main T1615dap36 {
	boolean a, x;
	if (false ? true : (x = true));
	a = x;
    }
} PASS

# definite unassignment - assigning x should fail
tcltest::test 16.1.5-definite-unassignment-fail-1 { V is DU before
        a iff V is DU before boolean a?b:c } {
    empty_main T1615duf1 {
	final boolean x;
	x = true;
	if ((x = true) ? true : false);
    }
} FAIL

tcltest::test 16.1.5-definite-unassignment-fail-2 { V is DU before
        b iff V is DU after a when true } {
    empty_main T1615duf2 {
	final boolean x;
	boolean a = x = true;
	if (a ? x = true : false);
    }
} FAIL

tcltest::test 16.1.5-definite-unassignment-fail-3 { V is DU before
        b iff V is DU after a when true } {
    empty_main T1615duf3 {
	final boolean x;
	x = true;
	if (true ? x = true : false);
    }
} FAIL

tcltest::test 16.1.5-definite-unassignment-fail-4 { V is DU before
        b iff V is DU after a when true } {
    empty_main T1615duf4 {
	final boolean x;
	if ((x = false) ? x = true : false);
    }
} FAIL

tcltest::test 16.1.5-definite-unassignment-fail-5 { V is DU before
        c iff V is DU after a when false } {
    empty_main T1615duf5 {
	final boolean x;
	boolean a = x = true;
	if (a ? true : (x = true));
    }
} FAIL

tcltest::test 16.1.5-definite-unassignment-fail-6 { V is DU before
        c iff V is DU after a when false } {
    empty_main T1615duf6 {
	final boolean x;
	x = true;
	if (false ? true : (x = true));
    }
} FAIL

tcltest::test 16.1.5-definite-unassignment-fail-7 { V is DU before
        c iff V is DU after a when false } {
    empty_main T1615duf7 {
	final boolean x;
	if ((x = true) ? true : (x = true));
    }
} FAIL
# TODO: more of these

# definite unassignment - assigning x should pass
tcltest::test 16.1.5-definite-unassignment-pass-1 { V is DU before
        a iff V is DU before boolean a?b:c } {
    empty_main T1615dup1 {
	final boolean x;
	if ((x = true) ? true : false);
    }
} PASS

tcltest::test 16.1.5-definite-unassignment-pass-2 { V is DU before
        b iff V is DU after a when true } {
    empty_main T1615dup2 {
	final boolean x;
	boolean a = true;
	if (a ? x = true : false);
    }
} PASS

tcltest::test 16.1.5-definite-unassignment-pass-3 { V is DU before
        b iff V is DU after a when true } {
    empty_main T1615dup3 {
	final boolean x;
	if (true ? x = true : false);
    }
} PASS

tcltest::test 16.1.5-definite-unassignment-pass-4 { V is DU before
        b iff V is DU after a when true } {
    empty_main T1615dup4 {
	final boolean x;
	x = true;
	if (false ? x = true : false);
    }
} PASS

tcltest::test 16.1.5-definite-unassignment-pass-5 { V is DU before
        c iff V is DU after a when false } {
    empty_main T1615dup5 {
	final boolean x;
	boolean a = true;
	if (a ? true : (x = true));
    }
} PASS

tcltest::test 16.1.5-definite-unassignment-pass-6 { V is DU before
        c iff V is DU after a when false } {
    empty_main T1615dup6 {
	final boolean x;
	if (false ? true : (x = true));
    }
} PASS

tcltest::test 16.1.5-definite-unassignment-pass-7 { V is DU before
        c iff V is DU after a when false } {
    empty_main T1615dup7 {
	final boolean x;
	x = true;
	if (true ? true : (x = true));
    }
} PASS
# TODO: more of these

# not-assignable - assigning x should fail, even though it is in dead code
tcltest::test 16.1.5-not-assignable-1 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_main T1615na1 {
	final boolean x = true; // constant
	if (true ? true : (x = true));
    }
} FAIL

tcltest::test 16.1.5-not-assignable-2 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_main T1615na2 {
	final boolean x = true; // constant
	if (false ? x = true : false);
    }
} FAIL

tcltest::test 16.1.5-not-assignable-3 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_main T1615na3 {
	final boolean x = Boolean.TRUE.booleanValue(); // non-constant
	if (true ? true : (x = true));
    }
} FAIL

tcltest::test 16.1.5-not-assignable-4 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_main T1615na4 {
	final boolean x = Boolean.TRUE.booleanValue(); // non-constant
	if (false ? x = true : false);
    }
} FAIL

tcltest::test 16.1.5-not-assignable-5 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_class T1615na5 {
	void foo(final boolean x) {
	    if (true ? true : (x = true));
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-6 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_class T1615na6 {
	void foo(final boolean x) {
	    if (false ? x = true : false);
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-7 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_main T1615na7 {
	try {
	    throw new Exception();
	} catch (final Exception x) {
	    if (true ? true : ((x = null) == null));
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-8 { DU only applies to blank
        finals; initialized fields cannot be assigned } {
    empty_main T1615na8 {
	try {
	    throw new Exception();
	} catch (final Exception x) {
	    if (false ? (x = null) == null : false);
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-9 { Blank final fields cannot be
        assigned outside constructor/initializer. } {
    empty_class T1615na9 {
	static final boolean x;
	static { x = true; }
	void foo() {
	    if (true ? true : (x = true));
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-10 { Blank final fields cannot be
        assigned outside constructor/initializer. } {
    empty_class T1615na10 {
	static final boolean x;
	static { x = true; }
	void foo() {
	    if (false ? x = true : true);
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-11 { Blank final fields cannot be
        assigned outside constructor/initializer. } {
    empty_class T1615na11 {
	final boolean x;
	{ x = true; }
	void foo() {
	    if (true ? true : (x = true));
	}
    }
} FAIL

tcltest::test 16.1.5-not-assignable-12 { Blank final fields cannot be
        assigned outside constructor/initializer. } {
    empty_class T1615na12 {
	final boolean x;
	{ x = true; }
	void foo() {
	    if (false ? x = true : true);
	}
    }
} FAIL

#V is [un]assigned after a ? b : c when true iff V is [un]assigned after b when true and V is [un]assigned after c when true. 
#V is [un]assigned after a ? b : c when false iff V is [un]assigned after b when false and V is [un]assigned after c when false. 
#V is [un]assigned after a ? b : c iff V is [un]assigned after a ? b : c when true and V is [un]assigned after a ? b : c when false. 
