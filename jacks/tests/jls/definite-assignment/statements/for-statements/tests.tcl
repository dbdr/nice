# Note that bullet 16.2.11.d should read:
# V is DA before the condition part of the for statement iff all of:
# - V is DA after the initialization part of the for statement.
# - Assuming V is DA before the condition part of the for statement, V is
#   DA after the incrementation part of the for statement.

tcltest::test 16.2.11-definite-unassign-pass-1 { v is DU after the for if it
        is DU before every break which exits the for } {
    empty_main T16211dup1 {
	final int i;
	boolean b = true;
	for ( ; ; ) {
	    if (b)
	        break;
	    try {
		i = 1;
		break; // doesn't exit for
	    } finally {
		return;
	    }
	}
	i = 2;
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-2 { v is DU before the condition
        if it is DU before every continue which exits the statement } {
    empty_main T16211dup2 {
	final int i;
	for ( ; ; ) {
	    try {
		i = 2;
		continue; // doesn't continue loop
	    } finally {
		return;
	    }
	}
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-3 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211dup3 {
	final int i;
	for ( ; ; )
	    if (false)
	        i = 1; // assignment not reachable
	// the fact that i is not DU before the loop doesn't matter
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-4 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211dup4 {
	for (final int i; ; ) {
	    i = 1;
	    break;
	}
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-5 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211dup5 {
	for (final boolean b; (b = true) && false; );
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-6 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211dup6 {
	for (final boolean b; b = true; )
	    break;
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-7 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211dup7 {
	for (final boolean b; true; b = true)
	    break;
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-8 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211dup8 {
	boolean b = true;
	for (final int i; b && false; )
	    i = 1;
    }
} PASS

tcltest::test 16.2.11-definite-unassign-pass-9 { v is DU after a for statement
        iff v is DU after the (possibly implicit) condition when false and v
        is DU before every exiting break } {
    empty_main T16211dup9 {
	final boolean b;
	for (b = true; b || true; );
	b = false;
    }
} PASS

tcltest::test 16.2.11-definite-unassign-fail-1 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211duf1 {
	for (final int i; ; )
	    i = 1;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-2 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211duf2 {
	for (final int i; ; ) {
	    i = 1;
	    continue;
	}
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-3 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211duf3 {
	for (final boolean b; b = false; );
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-4 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211duf4 {
	for (final boolean b; b = false; )
	    continue;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-5 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211duf5 {
	for (final boolean b; ; b = false);
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-6 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T16211duf6 {
	for (final boolean b; ; b = false)
	    continue;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-7 { v is DU after a for statement
        iff v is DU after the (possibly implicit) condition when false and v
        is DU before every exiting break } {
    empty_main T16211duf7 {
	final int i;
	for ( ; ; ) {
	    if (false)
                i = 1;
	    break;
	}
	i = 1;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-8 { v is DU after a for statement
        iff v is DU after the (possibly implicit) condition when false and v
        is DU before every exiting break } {
    empty_main T16211duf8 {
	final int i;
	for (boolean b = true; b && false; )
	    if (false)
                i = 1;
	i = 1;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-9 { v is DU after a for statement
        iff v is DU after the (possibly implicit) condition when false and v
        is DU before every exiting break } {
    empty_main T16211duf9 {
	final int i;
	for ( ; ; )
	    if (true)
	        break;
	    else
	        i = 1;
	i = 1;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-10 { v is DU after a for statement
        iff v is DU after the (possibly implicit) condition when false and v
        is DU before every exiting break } {
    empty_main T16211duf10 {
	final int i;
	for ( ; false; i = 1);
	i = 1;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-11 { v is DU after a for statement
        iff v is DU after the (possibly implicit) condition when false and v
        is DU before every exiting break } {
    empty_main T16211duf11 {
	final int i;
	for ( ; ; i = 1)
	    break;
	i = 1;
    }
} FAIL

tcltest::test 16.2.11-definite-unassign-fail-12 { v is DU before S iff V is
        DU after the (possibly implicit) condition when true } {
    empty_main T16211duf12 {
	for (final int i, j = i = 1; ; )
	    i = 2;
    }
} FAIL

tcltest::test 16.2.11-definite-assign-pass-1 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211dap1 {
	int i;
	for (boolean b = true; b || true; );
	int j = i;
    }
} PASS

tcltest::test 16.2.11-definite-assign-pass-2 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211dap2 {
	int i;
	for ( ; ; )
	    try {
		break; // not an exiting break;
	    } finally {
		i = 1;
		break;
	    }
        int j = i;
    }
} PASS

tcltest::test 16.2.11-definite-assign-pass-3 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211dap3 {
	boolean b;
	for ( ; b = false; );
	boolean x = b;
    }
} PASS

tcltest::test 16.2.11-definite-assign-fail-1 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211daf1 {
	int i;
	for (boolean b = false; b = !b; )
	    i = 1;
	int j = i;
    }
} FAIL

tcltest::test 16.2.11-definite-assign-fail-2 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211daf2 {
	int i;
	for ( ; ; )
	    break;
	int j = i;
    }
} FAIL

tcltest::test 16.2.11-definite-assign-fail-3 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211daf3 {
	int i;
	for (boolean b; b = true; )
	    if (false)
	        break;
	int j = i;
    }
} FAIL

tcltest::test 16.2.11-definite-assign-fail-4 { v is DA after a for statement
        iff v is DA after the (possibly implicit) condition when false and v
        is DA before every exiting break } {
    empty_main T16211daf4 {
	int i;
	for ( ; ; i = 1)
	    break;
	int j = i;
    }
} FAIL

