tcltest::test 16.2.9-definite-unassign-pass-1 { v is DU after the while if it
        is DU before every break which exits the while } {
    empty_main T1629dup1 {
	final int i;
	boolean b = true;
	while (true) {
	    if (b)
	        break;
	    try {
		i = 1;
		break; // doesn't exit while
	    } finally {
		return;
	    }
	}
	i = 2;
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-2 { v is DU before the condition
        if it is DU before every continue which exits the statement } {
    empty_main T1629dup2 {
	final int i;
	while (true) {
	    try {
		i = 2;
		continue; // doesn't continue loop
	    } finally {
		return;
	    }
	}
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-3 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629dup3 {
	final int i;
	while (true)
	    if (false)
	        i = 1;
	// the fact that i is not DU before the loop doesn't matter
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-4 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629dup4 {
	final int i;
	while (true) {
	    i = 1;
	    break;
	}
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-5 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629dup5 {
	final boolean b;
	while ((b = true) && false);
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-6 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629dup6 {
	final boolean b;
	while (b = true)
	    break;
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-7 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629dup7 {
	final int i;
	boolean b = true;
	while (b && false)
	    i = 1;
    }
} PASS

tcltest::test 16.2.9-definite-unassign-pass-8 { v is DU after while(e)S; iff
        v is DU after e when false and v is DU before every exiting break } {
    empty_main T1629dup8 {
	final boolean b;
	b = true;
	while (b || true);
	b = false;
    }
} PASS

tcltest::test 16.2.9-definite-unassign-fail-1 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629duf1 {
	final int i;
	while (true)
	    i = 1;
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-2 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629duf2 {
	final int i;
	while (true) {
	    i = 1;
	    continue;
	}
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-3 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629duf3 {
	final boolean b;
	while (b = false);
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-4 { v may be reachably assigned
        in loop only once unless loop can't repeat } {
    empty_main T1629duf4 {
	final boolean b;
	while (b = false)
	    continue;
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-5 { v is DU after while(e)S; iff
        v is DU after e when false and v is DU before every exiting break } {
    empty_main T1629duf5 {
	final int i;
	while (true) {
	    if (false)
                i = 1;
	    break;
	}
	i = 1;
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-6 { v is DU after while(e)S; iff
        v is DU after e when false and v is DU before every exiting break } {
    empty_main T1629duf6 {
	final int i;
	boolean b = true;
	while (b && false)
	    if (false)
                i = 1;
	i = 1;
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-7 { v is DU after while(e)S; iff
        v is DU after e when false and v is DU before every exiting break } {
    empty_main T1629duf7 {
	final int i;
	while (true)
	    if (true)
	        break;
	    else
                i = 1;
	i = 1;
    }
} FAIL

tcltest::test 16.2.9-definite-unassign-fail-8 { v is DU before S iff V is DU
        after e when true } {
    empty_main T1629duf8 {
	final int i;
	i = 1;
	while (true)
	    i = 2;
    }
} FAIL

tcltest::test 16.2.9-definite-assign-pass-1 { v is DA after while(e)S; iff
        v is DA after e when false and v is DA before every exiting break } {
    empty_main T1629dap1 {
	int i;
	boolean b = true;
	while (b || true);
	int j = i;
    }
} PASS

tcltest::test 16.2.9-definite-assign-pass-2 { v is DA after while(e)S; iff
        v is DA after e when false and v is DA before every exiting break } {
    empty_main T1629dap2 {
	int i;
	while (true)
	    try {
		break; // not an exiting break;
	    } finally {
		i = 1;
		break;
	    }
        int j = i;
    }
} PASS

tcltest::test 16.2.9-definite-assign-pass-3 { v is DA after while(e)S; iff
        v is DA after e when false and v is DA before every exiting break } {
    empty_main T1629dap3 {
	boolean b;
	while (b = false);
	boolean x = b;
    }
} PASS

tcltest::test 16.2.9-definite-assign-fail-1 { v is DA after while(e)S; iff
        v is DA after e when false and v is DA before every exiting break } {
    empty_main T1629daf1 {
	int i;
	boolean b = false;
	while (b = !b)
	    i = 1;
	int j = i;
    }
} FAIL

tcltest::test 16.2.9-definite-assign-fail-2 { v is DA after while(e)S; iff
        v is DA after e when false and v is DA before every exiting break } {
    empty_main T1629daf2 {
	int i;
	while (true)
	    break;
	int j = i;
    }
} FAIL

tcltest::test 16.2.9-definite-assign-fail-3 { v is DA after while(e)S; iff
        v is DA after e when false and v is DA before every exiting break } {
    empty_main T1629daf3 {
	int i;
	boolean b;
	while (b = true)
	    if (false)
	        break;
	int j = i;
    }
} FAIL

